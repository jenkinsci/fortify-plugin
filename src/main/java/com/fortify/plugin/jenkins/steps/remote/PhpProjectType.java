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

public class PhpProjectType extends RemoteAnalysisProjectType {
    private String phpVersion;

    @DataBoundConstructor
    public PhpProjectType() {}

    public String getPhpVersion() { return phpVersion; }

    @DataBoundSetter
    public void setPhpVersion(String phpVersion) { this.phpVersion = phpVersion; }

    @Extension @Symbol("fortifyPHP")
    public static final class DescriptorImpl extends RemoteAnalysisProjectTypeDescriptor {
        public DescriptorImpl() {
            super(PhpProjectType.class);
        }

        @Override
        public String getDisplayName() {
            return "PHP";
        }

    }
}
