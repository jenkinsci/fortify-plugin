/*******************************************************************************
 * Copyright 2019-2023 Open Text.
 * 
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://opensource.org/licenses/MIT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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