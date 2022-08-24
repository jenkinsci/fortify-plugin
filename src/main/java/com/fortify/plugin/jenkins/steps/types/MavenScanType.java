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

import com.fortify.plugin.jenkins.Messages;

import hudson.Extension;
import hudson.tasks.Maven.MavenInstallation;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.util.ListBoxModel;

public class MavenScanType extends ProjectScanType {
	private String mavenOptions;
	private String mavenInstallationName;

	@DataBoundConstructor
	public MavenScanType() {
	}

	public String getMavenOptions() {
		return mavenOptions;
	}

	public String getMavenInstallationName() {
		return mavenInstallationName;
	}

	@DataBoundSetter
	public void setMavenOptions(String mavenOptions) {
		this.mavenOptions = mavenOptions;
	}

	@DataBoundSetter
	public void setMavenInstallationName(String mavenInstallationName) {
		this.mavenInstallationName = mavenInstallationName;
	}

	@Extension @Symbol("fortifyMaven3")
	public static final class DescriptorImpl extends ProjectScanTypeDescriptor {
		public DescriptorImpl() {
			super(MavenScanType.class);
		}

		@Override
		public String getDisplayName() {
			return Messages.MavenScanType_DisplayName();
		}

		public ListBoxModel doFillMavenInstallationNameItems() {
			MavenInstallation[] installations = getInstallations();
			ListBoxModel result = new ListBoxModel();
			result.add("(Default)");
			for (MavenInstallation nextMvn : installations) {
				String nextName = nextMvn.getName();
				result.add(nextName);
			}
			return result;
		}

		public MavenInstallation[] getInstallations() {
			List<MavenInstallation> r = new ArrayList<MavenInstallation>();
			for (ToolDescriptor<?> desc : ToolInstallation.all()) {
				for (ToolInstallation inst : desc.getInstallations()) {
					if (inst instanceof MavenInstallation) {
						MavenInstallation nextMvn = (MavenInstallation) inst;
						// we can't test for version compatibility here because we don't know which node it's going to be invoked on
						r.add(nextMvn);
					}
				}
			}
			return r.toArray(new MavenInstallation[r.size()]);
		}
	}
}
