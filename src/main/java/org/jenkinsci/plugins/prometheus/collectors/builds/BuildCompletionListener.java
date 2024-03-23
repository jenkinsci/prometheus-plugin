package org.jenkinsci.plugins.prometheus.collectors.builds;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Listens to builds that have been completed and stores them in a list.
 * The JobCollector reads items in the list when it performs a scrape and 
 * publishes the data.
 * Class extends https://javadoc.jenkins.io/hudson/model/listeners/RunListener.html
 */
public class BuildCompletionListener extends RunListener<Run<?,?>> { 
    // static instance of the class to use as a singleton.  
    private static BuildCompletionListener _Listener;

    // Lock to synchronize iteration and adding to the collection
    private final Lock lock;

    // Holds the list o runs in queue.
    private final List<Run<?,?>> runStack;

    // Iterable that defines a close method (allows us to use try resource) block
    // in JobCollector.java
    public interface CloseableIterator<T> extends Iterator<T>, AutoCloseable {
        void close();
    }

    // Protected so no one can create their own copy of the class.
    protected BuildCompletionListener(){
        runStack = Collections.synchronizedList(new ArrayList<>());
        lock = new ReentrantLock();
    }

    @Override
    public void unregister() {
        super.unregister();
        try {
            lock.lock();
            this.runStack.clear();
        } finally {
            lock.unlock();
        }
    }

    /*
     * Extension tells Jenkins to register this class as a RunListener and to use
     * this method in order to retrieve an instance of the class. It is a singleton,
     * so we can get the same reference registered in Jenkins in another class.
     */
    @Extension
    public synchronized static BuildCompletionListener getInstance(){
        if(_Listener == null){
            _Listener = new BuildCompletionListener();
        }
        return _Listener;
    }

    /*
     * Fires on completion of a job.
     */
    public void onCompleted(Run<?,?> run, @NonNull TaskListener listener){
        push(run);
    }

    /*
     * Pushes a run onto the list
     */
    private synchronized void push(Run<?,?> run){
        // Acquire lock
        lock.lock();

        // Try to add the run to the list. If something goes wrong, make sure
        // we still unlock the lock!
        try{
            runStack.add(run);
        }
        finally{
            lock.unlock();
        }
    }

    /*
     * Returns a closeable iterator
     */
    public synchronized CloseableIterator<Run<?,?>> iterator(){
        // acquire lock before iterating
        lock.lock();
        return new CloseableIterator<>() {
            // Get iterator from the list
            private final Iterator<Run<?, ?>> iterator = runStack.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Run<?, ?> next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                iterator.remove();
            }

            // When we close the iterator, clear the list right before we unlock.
            // This ensures we don't see the same job twice if iterator is called again.
            public void close() {
                runStack.clear();
                lock.unlock();
            }
        };
    }

    List<Run<?, ?>> getRunStack() {
        return runStack;
    }
}
