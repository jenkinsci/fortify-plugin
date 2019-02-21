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

import java.util.List;

public class GroupingValueBean implements Comparable {

	public static final String ID_EXPLOITABLE = "Exploitable";
	public static final String ID_SUSPICIOUS = "Suspicious";
	public static final String ID_BAD_PRACTICE = "Bad Practice";
	public static final String ID_RELIABILITY = "Reliability Issue";
	public static final String ID_NOT_AN_ISSUE = "Not an Issue";

	public static final String ID_NEW_ISSUES = "NEW";

	public static final String GROUPING_TYPE_NEWISSUES = "New Issue";
	public static final String GROUPING_TYPE_ANALYSIS = "Analysis";

	private final String id;
	private final String folderId;
	private final String name;
	private final int totalCount;
	private final int visibleCount;
	private final int auditedCount;

	public GroupingValueBean(String id, String folderId, List<String> attributes) {
		this.id = id.trim();
		this.folderId = folderId;
		this.name = attributes.get(0).trim();
		this.totalCount = Integer.valueOf(attributes.get(1));
		this.visibleCount = Integer.valueOf(attributes.get(1));
		this.auditedCount = Integer.valueOf(attributes.get(1));
	}

	public String getId() {
		return id;
	}

	public String getFolderId() {
		return folderId;
	}

	public String getName() {
		return name;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public int getVisibleCount() {
		return visibleCount;
	}

	public int getAuditedCount() {
		return auditedCount;
	}

	@Override
	public int compareTo(Object o) {
		GroupingValueBean otherBean = (GroupingValueBean) o;
		return otherBean.getId().compareTo(getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GroupingValueBean) {
			return ((GroupingValueBean)obj).getId().equals(this.getId());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return getId().hashCode();
	}
}
