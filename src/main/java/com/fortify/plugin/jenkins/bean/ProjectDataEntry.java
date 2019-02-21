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
package com.fortify.plugin.jenkins.bean;

import java.util.Map;

public class ProjectDataEntry {

	public String getProjectName() {
		return myProjectName;
	}

	public String getProjectVersionName() {
		return myProjectVersionName;
	}

	public String getProjectTemplateName() {
		return myProjectTemplateName;
	}

	public Map<String, String> getAttributeNamesAndValues() {
		return myAttributeNamesAndValues;
	}

	public ProjectDataEntry(String projectName, String projectVersionName, String projectTemplateName,
			Map<String, String> attributeNamesAndValues) {
		myProjectName = projectName;
		myProjectVersionName = projectVersionName;
		myProjectTemplateName = projectTemplateName;
		myAttributeNamesAndValues = attributeNamesAndValues;
	}

	private final String myProjectName;
	private final String myProjectVersionName;
	private final String myProjectTemplateName;
	private final Map<String, String> myAttributeNamesAndValues;
}
