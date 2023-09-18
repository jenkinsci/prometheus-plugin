package org.jenkinsci.plugins.prometheus.config.disabledmetrics;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexDisabledMetric extends Entry {

    private final String regex;

    @DataBoundConstructor
    public RegexDisabledMetric(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }

    @Override
    public Descriptor<Entry> getDescriptor() {
        return new DescriptorImpl();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Entry> {
        @Override
        public String getDisplayName() {
            return "Regex Entry";
        }

    }
}
