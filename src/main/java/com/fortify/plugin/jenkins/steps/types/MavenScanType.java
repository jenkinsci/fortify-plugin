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

import com.fortify.plugin.jenkins.Messages;

import hudson.Extension;

public class MavenScanType extends ProjectScanType {
	private String mavenOptions;
	private boolean skipBuild;

	@DataBoundConstructor
	public MavenScanType() {
	}

	public String getMavenOptions() {
		return mavenOptions;
	}

	public boolean getSkipBuild() {
		return skipBuild;
	}

	@DataBoundSetter
	public void setMavenOptions(String mavenOptions) {
		this.mavenOptions = mavenOptions;
	}

	@DataBoundSetter
	public void setSkipBuild(boolean skipBuild) {
		this.skipBuild = skipBuild;
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
	}
}
