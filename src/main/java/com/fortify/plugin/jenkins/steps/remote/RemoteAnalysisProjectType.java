package com.fortify.plugin.jenkins.steps.remote;

import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.List;

public abstract class RemoteAnalysisProjectType implements ExtensionPoint, Describable<RemoteAnalysisProjectType> {
    @Override
    public Descriptor<RemoteAnalysisProjectType> getDescriptor() {
        return Jenkins.get().getDescriptorOrDie(getClass());
    }

    public List<RemoteAnalysisProjectTypeDescriptor> getRemoteAnalysisProjectTypeDescriptors() {
        return Jenkins.get().getDescriptorList(RemoteAnalysisProjectType.class);
    }

    public abstract static class RemoteAnalysisProjectTypeDescriptor extends Descriptor<RemoteAnalysisProjectType> {
        public RemoteAnalysisProjectTypeDescriptor(Class<? extends RemoteAnalysisProjectType> clazz) {
            super(clazz);
        }

        @Override
        abstract public String getDisplayName();
    }
}
