package com.fortify.plugin.jenkins.steps.remote;

import com.fortify.plugin.jenkins.steps.Validators;
import hudson.Extension;
import hudson.util.FormValidation;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

public class MSBuildProjectType extends RemoteAnalysisProjectType {
    private String dotnetProject;
    private boolean excludeDisabledProjects;

    @DataBoundConstructor
    public MSBuildProjectType() {
    }

    public String getDotnetProject() {
        return dotnetProject;
    }

    public boolean isExcludeDisabledProjects() {
        return excludeDisabledProjects;
    }

    @DataBoundSetter
    public void setDotnetProject(String dotnetProject) {
        this.dotnetProject = dotnetProject;
    }

    @DataBoundSetter
    public void setExcludeDisabledProjects(boolean excludeDisabledProjects) {
        this.excludeDisabledProjects = excludeDisabledProjects;
    }

    @Extension
    @Symbol("fortifyMSBuild")
    public static final class DescriptorImpl extends RemoteAnalysisProjectTypeDescriptor {
        public DescriptorImpl() {
            super(MSBuildProjectType.class);
        }

        @Override
        public String getDisplayName() {
            return ".NET MSBuild";
        }

        public FormValidation doCheckDotnetProject(@QueryParameter String value) {
            return Validators.checkFieldNotEmpty(value);
        }
    }

    public static final RemoteAnalysisProjectTypeDescriptor DESCRIPTOR = new MSBuildProjectType.DescriptorImpl();
}
