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

public class DotnetSourceScanType extends ProjectScanType {
	private String dotnetFrameworkVersion;
	private String dotnetLibdirs;
	private String dotnetSrcFiles;
	private String dotnetAddOptions;

	@DataBoundConstructor
	public DotnetSourceScanType() {
	}

	public String getDotnetFrameworkVersion() {
		return dotnetFrameworkVersion;
	}

	public String getDotnetLibdirs() {
		return dotnetLibdirs;
	}

	public String getDotnetSrcFiles() {
		return dotnetSrcFiles;
	}

	public String getDotnetAddOptions() {
		return dotnetAddOptions;
	}

	@DataBoundSetter
	public void setDotnetFrameworkVersion(String dotnetFrameworkVersion) {
		this.dotnetFrameworkVersion = dotnetFrameworkVersion;
	}

	@DataBoundSetter
	public void setDotnetLibdirs(String dotnetLibdirs) {
		this.dotnetLibdirs = dotnetLibdirs;
	}

	@DataBoundSetter
	public void setDotnetSrcFiles(String dotnetSrcFiles) {
		this.dotnetSrcFiles = dotnetSrcFiles;
	}

	@DataBoundSetter
	public void setDotnetAddOptions(String dotnetAddOptions) {
		this.dotnetAddOptions = dotnetAddOptions;
	}

	@Extension @Symbol("fortifyDotnetSrc")
	public static final class DescriptorImpl extends ProjectScanTypeDescriptor {
		public DescriptorImpl() {
			super(DotnetSourceScanType.class);
		}

		@Override
		public String getDisplayName() {
			return Messages.DotnetSourceScanType_DisplayName();
		}

		public FormValidation doCheckDotnetFrameworkVersion(@QueryParameter String value) {
			FormValidation testEmpty = Validators.checkFieldNotEmpty(value);
			if (testEmpty == FormValidation.ok()) {
				return Validators.checkValidVersionNumber(value);
			} else {
				return testEmpty;
			}
		}

		public FormValidation doCheckDotnetSrcFiles(@QueryParameter String value) {
			return Validators.checkFieldNotEmpty(value);
		}
	}
}
