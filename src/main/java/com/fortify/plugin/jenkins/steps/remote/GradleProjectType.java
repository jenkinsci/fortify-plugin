package com.fortify.plugin.jenkins.steps.remote;

import hudson.Extension;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class GradleProjectType extends RemoteAnalysisProjectType {
    private boolean includeTests;
    private String buildFile;
    private boolean skipBuild;

    @DataBoundConstructor
    public GradleProjectType() {}

    public boolean getIncludeTests() { return includeTests; }
    public String getBuildFile() { return buildFile; }
    public boolean getSkipBuild() { return skipBuild; }

    @DataBoundSetter
    public void setIncludeTests(boolean includeTests) { this.includeTests = includeTests; }
    @DataBoundSetter
    public void setBuildFile(String buildFile) { this.buildFile = buildFile; }
    @DataBoundSetter
    public void setSkipBuild(boolean skipBuild) { this.skipBuild = skipBuild; }

    @Extension @Symbol("fortifyGradle")
    public static final class DescriptorImpl extends RemoteAnalysisProjectTypeDescriptor {
        public DescriptorImpl() {
            super(GradleProjectType.class);
        }

        @Override
        public String getDisplayName() {
            return "Gradle";
        }

    }

    public static final RemoteAnalysisProjectTypeDescriptor DESCRIPTOR = new DescriptorImpl();
}
