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

public class GroupingBean implements Comparable {

	private String groupingName;
	private String groupingID;

	public String getGroupingName() {
		return groupingName;
	}

	public void setGroupingName(String groupingName) {
		this.groupingName = groupingName;
	}

	public String getGroupingID() {
		return groupingID;
	}

	public void setGroupingID(String groupingID) {
		this.groupingID = groupingID;
	}

	@Override
	public int compareTo(Object otherBean) {
		GroupingBean otherBean2 = (GroupingBean) otherBean;
		return this.getGroupingName().compareTo(otherBean2.getGroupingName());
	}
}
