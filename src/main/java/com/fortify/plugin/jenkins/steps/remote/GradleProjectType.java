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
