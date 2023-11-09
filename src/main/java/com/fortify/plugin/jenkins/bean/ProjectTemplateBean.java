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
package com.fortify.plugin.jenkins.bean;

public class ProjectTemplateBean implements Comparable {
	private String name;
	private String id;

	public ProjectTemplateBean() {
	}

	public ProjectTemplateBean(String name, String id) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int compareTo(Object otherBean) {
		ProjectTemplateBean otherBean2 = (ProjectTemplateBean) otherBean;
		return this.getName().compareTo(otherBean2.getName());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ProjectTemplateBean) {
			return ((ProjectTemplateBean)obj).getId().equals(this.getId());
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}
}
