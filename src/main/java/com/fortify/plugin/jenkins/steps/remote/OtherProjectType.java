package com.fortify.plugin.jenkins.steps.remote;

import hudson.Extension;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class OtherProjectType extends RemoteAnalysisProjectType {
    @DataBoundConstructor
    public OtherProjectType() {}

    @Extension
    @Symbol("fortifyOther")
    public static final class DescriptorImpl extends RemoteAnalysisProjectTypeDescriptor {
        public DescriptorImpl() {
            super(OtherProjectType.class);
        }

        @Override
        public String getDisplayName() {
            return "Other";
        }
    }

    public static final RemoteAnalysisProjectTypeDescriptor DESCRIPTOR = new DescriptorImpl();
}
