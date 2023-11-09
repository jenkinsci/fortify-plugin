/*******************************************************************************
 * Copyright 2019 - 2023 Open Text. 
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
package com.fortify.plugin.jenkins.steps.types;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.fortify.plugin.jenkins.Messages;
import com.fortify.plugin.jenkins.steps.Validators;

import hudson.Extension;
import hudson.util.FormValidation;

public class MsbuildScanType extends ProjectScanType {
	private String dotnetProject;
	private String dotnetAddOptions;

	@DataBoundConstructor
	public MsbuildScanType() {
	}

	public String getDotnetProject() {
		return dotnetProject;
	}

	public String getDotnetAddOptions() {
		return dotnetAddOptions;
	}

	@DataBoundSetter
	public void setDotnetProject(String dotnetProject) {
		this.dotnetProject = dotnetProject;
	}

	@DataBoundSetter
	public void setDotnetAddOptions(String dotnetAddOptions) {
		this.dotnetAddOptions = dotnetAddOptions;
	}

	@Extension @Symbol("fortifyMSBuild")
	public static final class DescriptorImpl extends ProjectScanTypeDescriptor {
		public DescriptorImpl() {
			super(MsbuildScanType.class);
		}

		@Override
		public String getDisplayName() {
			return Messages.MsbuildScanType_DisplayName();
		}

		public FormValidation doCheckDotnetProject(@QueryParameter String value) {
			return Validators.checkFieldNotEmpty(value);
		}
	}
}
