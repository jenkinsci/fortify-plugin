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

import java.util.ArrayList;
import java.util.List;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.fortify.plugin.jenkins.Messages;
import com.fortify.plugin.jenkins.steps.Validators;

import hudson.Extension;
import hudson.plugins.gradle.GradleInstallation;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

public class GradleScanType extends ProjectScanType {
	private boolean useWrapper;
	private boolean skipBuild;
	private String gradleTasks;
	private String gradleOptions;
	private String gradleInstallationName;

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

	public String getGradleInstallationName() {
		return gradleInstallationName;
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

	@DataBoundSetter
	public void setGradleInstallationName(String gradleInstallationName) {
		this.gradleInstallationName = gradleInstallationName;
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

		public ListBoxModel doFillGradleInstallationNameItems(String value) {
			GradleInstallation[] installations = getInstallations();
			ListBoxModel result = new ListBoxModel();
			result.add("<Default>");
			for (GradleInstallation nextGradle : installations) {
				String nextName = nextGradle.getName();
				result.add(nextName);
			}
			return result;
		}

		public GradleInstallation[] getInstallations() {
			List<GradleInstallation> r = new ArrayList<GradleInstallation>();
			for (ToolDescriptor<?> desc : ToolInstallation.all()) {
				for (ToolInstallation inst : desc.getInstallations()) {
					if (inst instanceof GradleInstallation) {
						GradleInstallation nextGradle = (GradleInstallation) inst;
						// we can't test for version compatibility here because we don't know which node it's going to be invoked on
						r.add(nextGradle);
					}
				}
			}
			return r.toArray(new GradleInstallation[r.size()]);
		}
	}
}
