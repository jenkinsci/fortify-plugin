/*******************************************************************************
 * (c) Copyright 2019 Micro Focus or one of its affiliates. 
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

public class GradleScanType extends ProjectScanType {
	private boolean useWrapper;
	private boolean skipBuild;
	private String gradleTasks;
	private String gradleOptions;

	@DataBoundConstructor
	public GradleScanType() {
	}

	public boolean getUseWrapper() {
		return useWrapper;
	}

	public boolean getSkipBuild() {
		return skipBuild;
	}

	public String getGradleTasks() {
		return gradleTasks;
	}

	public String getGradleOptions() {
		return gradleOptions;
	}

	@DataBoundSetter
	public void setUseWrapper(boolean useWrapper) {
		this.useWrapper = useWrapper;
	}

	@DataBoundSetter
	public void setSkipBuild(boolean skipBuild) {
		this.skipBuild = skipBuild;
	}

	@DataBoundSetter
	public void setGradleTasks(String gradleTasks) {
		this.gradleTasks = gradleTasks;
	}

	@DataBoundSetter
	public void setGradleOptions(String gradleOptions) {
		this.gradleOptions = gradleOptions;
	}

	@Extension @Symbol("fortifyGradle")
	public static final class DescriptorImpl extends ProjectScanTypeDescriptor {
		public DescriptorImpl() {
			super(GradleScanType.class);
		}

		@Override
		public String getDisplayName() {
			return Messages.GradleScanType_DisplayName();
		}

		public FormValidation doCheckGradleTasks(@QueryParameter String value) {
			return Validators.checkFieldNotEmpty(value);
		}
	}
}
