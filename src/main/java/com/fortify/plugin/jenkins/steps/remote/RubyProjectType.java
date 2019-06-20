package com.fortify.plugin.jenkins.steps.remote;

import hudson.Extension;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class RubyProjectType extends RemoteAnalysisProjectType {
    @DataBoundConstructor
    public RubyProjectType() {}

    @Extension
    @Symbol("fortifyRuby")
    public static final class DescriptorImpl extends RemoteAnalysisProjectTypeDescriptor {
        public DescriptorImpl() {
            super(RubyProjectType.class);
        }

        @Override
        public String getDisplayName() {
            return "Ruby";
        }
    }

    public static final RemoteAnalysisProjectTypeDescriptor DESCRIPTOR = new DescriptorImpl();
}
