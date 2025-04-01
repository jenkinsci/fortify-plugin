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
import org.apache.commons.lang.StringUtils;

public class DotNetProjectType extends RemoteAnalysisProjectType {

    private String dotnetProject;
    private boolean excludeDisabledProjects;

    @DataBoundConstructor
    public DotNetProjectType() {
    }

    public String getDotnetProject() {
        return dotnetProject;
    }

    public boolean isExcludeDisabledProjects() {
        return excludeDisabledProjects;
    }

    @DataBoundSetter
    public void setDotnetProject(String dotnetProject) {
        this.dotnetProject = StringUtils.isBlank(dotnetProject) ? null : dotnetProject;
    }

    @DataBoundSetter
    public void setExcludeDisabledProjects(boolean excludeDisabledProjects) {
        this.excludeDisabledProjects = excludeDisabledProjects;
    }

    @Extension
    @Symbol("fortifyDotNet")
    public static final class DescriptorImpl extends RemoteAnalysisProjectTypeDescriptor {
        public DescriptorImpl() {
            super(DotNetProjectType.class);
        }

        @Override
        public String getDisplayName() {
            return "dotnet";
        }

    }

    public static final RemoteAnalysisProjectTypeDescriptor DESCRIPTOR = new DotNetProjectType.DescriptorImpl();
}