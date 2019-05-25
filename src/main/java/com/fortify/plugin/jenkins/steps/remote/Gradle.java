package com.fortify.plugin.jenkins.steps.remote;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class Gradle extends RemoteAnalysisProjectType {
    private boolean includeTests;
    private String buildFile;

    @DataBoundConstructor
    public Gradle() {}

    public boolean getIncludeTests() { return includeTests; }
    public String getBuildFile() { return buildFile; }

    @DataBoundSetter
    public void setIncludeTests(boolean includeTests) { this.includeTests = includeTests; }
    @DataBoundSetter
    public void setBuildFile(String buildFile) { this.buildFile = buildFile; }

    @Extension
    public static final class DescriptorImpl extends RemoteAnalysisProjectTypeDescriptor {
        public DescriptorImpl() {
            super(Gradle.class);
        }

        @Override
        public String getDisplayName() {
            return "Gradle";
        }

    }

    public static final RemoteAnalysisProjectTypeDescriptor DESCRIPTOR = new DescriptorImpl();
}
