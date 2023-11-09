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

public class AdvancedScanType extends ProjectScanType {
	private String advOptions;

	@DataBoundConstructor
	public AdvancedScanType() {
	}

	public String getAdvOptions() {
		return advOptions;
	}

	@DataBoundSetter
	public void setAdvOptions(String advOptions) {
		this.advOptions = advOptions;
	}

	@Extension @Symbol("fortifyAdvanced")
	public static final class DescriptorImpl extends ProjectScanTypeDescriptor {
		public DescriptorImpl() {
			super(AdvancedScanType.class);
		}

		@Override
		public String getDisplayName() {
			return Messages.AdvancedScanType_DisplayName();
		}

		public FormValidation doCheckAdvOptions(@QueryParameter String value) {
			return Validators.checkFieldNotEmpty(value);
		}
	}
}
