package com.fortify.plugin.jenkins.steps.remote;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

public class Ruby extends RemoteAnalysisProjectType {

    @DataBoundConstructor
    public Ruby() {}

    @Extension
    public static final class DescriptorImpl extends RemoteAnalysisProjectTypeDescriptor {
        public DescriptorImpl() {
            super(Ruby.class);
        }

        @Override
        public String getDisplayName() {
                return "Ruby";
            }

    }

}
