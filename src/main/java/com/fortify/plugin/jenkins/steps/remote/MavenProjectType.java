package com.fortify.plugin.jenkins.steps.remote;

import hudson.Extension;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class MavenProjectType extends RemoteAnalysisProjectType {
    private boolean includeTests;
    private String buildFile;

    @DataBoundConstructor
    public MavenProjectType() {}

    public boolean getIncludeTests() { return includeTests; }
    public String getBuildFile() { return buildFile; }

    @DataBoundSetter
    public void setIncludeTests(boolean includeTests) { this.includeTests = includeTests; }
    @DataBoundSetter
    public void setBuildFile(String buildFile) { this.buildFile = buildFile; }

    @Extension @Symbol("fortifyMaven")
    public static final class DescriptorImpl extends RemoteAnalysisProjectTypeDescriptor {
        public DescriptorImpl() {
            super(MavenProjectType.class);
        }

        @Override
        public String getDisplayName() {
            return "Maven";
        }

    }
}
