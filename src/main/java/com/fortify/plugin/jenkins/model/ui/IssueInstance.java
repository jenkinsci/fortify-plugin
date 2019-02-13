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
package com.fortify.plugin.jenkins.model.ui;

public class IssueInstance extends IssueBase {

	protected ClassInfo classInfo;
	protected InstanceInfo instanceInfo;
	protected String groupName;

	/**
	 * Gets the value of the classInfo property.
	 * 
	 * @return possible object is {@link ClassInfo }
	 * 
	 */
	public ClassInfo getClassInfo() {
		return classInfo;
	}

	/**
	 * Sets the value of the classInfo property.
	 * 
	 * @param value
	 *            allowed object is {@link ClassInfo }
	 * 
	 */
	public void setClassInfo(ClassInfo value) {
		this.classInfo = value;
	}

	/**
	 * Gets the value of the instanceInfo property.
	 * 
	 * @return possible object is {@link InstanceInfo }
	 * 
	 */
	public InstanceInfo getInstanceInfo() {
		return instanceInfo;
	}

	/**
	 * Sets the value of the instanceInfo property.
	 * 
	 * @param value
	 *            allowed object is {@link InstanceInfo }
	 * 
	 */
	public void setInstanceInfo(InstanceInfo value) {
		this.instanceInfo = value;
	}

	/**
	 * Gets the value of the groupName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * Sets the value of the groupName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setGroupName(String value) {
		this.groupName = value;
	}

}
