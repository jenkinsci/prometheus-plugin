package org.jenkinsci.plugins.prometheus.collectors.builds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

public class BuildCompletionListener extends RunListener<Run<?,?>> {    
    private static BuildCompletionListener _Listener;
    private Lock lock;
    private List<Run<?,?>> runStack;

    public interface CloseableIterator<T> extends Iterator<T>, AutoCloseable {
        void close();
    }

    protected BuildCompletionListener(){
        runStack = Collections.synchronizedList(new ArrayList<>());
        lock = new ReentrantLock();
    }

    @Extension
    public static BuildCompletionListener getInstance(){
        if(_Listener == null){
            _Listener = new BuildCompletionListener();
        }
        return _Listener;
    }

    public void onCompleted(Run<?,?> run, TaskListener listener){
        push(run);
    }

    public synchronized void push(Run<?,?> run){
        lock.lock();
        try{
            runStack.add(run);
        }
        finally{
            lock.unlock();
        }
    }
    public synchronized CloseableIterator<Run<?,?>> iterator(){
        lock.lock();
        return new CloseableIterator<Run<?,?>>() {
            private Iterator<Run<?,?>> iterator = runStack.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Run<?,?> next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                iterator.remove();
            }

            public void close() {
                runStack.clear();
                lock.unlock();
            }
        };
    }
}
