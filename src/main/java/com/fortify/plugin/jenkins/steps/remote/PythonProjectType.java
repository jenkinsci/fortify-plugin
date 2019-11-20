package com.fortify.plugin.jenkins.steps.remote;

import hudson.Extension;
import hudson.util.ListBoxModel;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class PythonProjectType extends RemoteAnalysisProjectType {
    private String pythonVersion;
    private String pythonRequirementsFile;
    private String pythonVirtualEnv;

    @DataBoundConstructor
    public PythonProjectType() {}

    public String getPythonVersion() {
        return pythonVersion;
    }
    @DataBoundSetter
    public void setPythonVersion(String pythonVersion) {
        this.pythonVersion = pythonVersion;
    }

    public String getPythonRequirementsFile() {
        return pythonRequirementsFile;
    }
    @DataBoundSetter
    public void setPythonRequirementsFile(String pythonRequirementsFile) {
        this.pythonRequirementsFile = pythonRequirementsFile;
    }

    public String getPythonVirtualEnv() {
        return pythonVirtualEnv;
    }
    @DataBoundSetter
    public void setPythonVirtualEnv(String pythonVirtualEnv) {
        this.pythonVirtualEnv = pythonVirtualEnv;
    }

    @Extension @Symbol("fortifyPython")
    public static final class DescriptorImpl extends RemoteAnalysisProjectTypeDescriptor {
        public DescriptorImpl() {
            super(PythonProjectType.class);
        }

        @Override
        public String getDisplayName() {
            return "Python";
        }

        public ListBoxModel doFillPythonVersionItems() {
            ListBoxModel options = new ListBoxModel();
            options.add("2", "2");
            options.add("3", "3");
            return options;
        }

    }
}
