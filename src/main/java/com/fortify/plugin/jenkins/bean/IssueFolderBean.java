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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import com.fortify.plugin.jenkins.FortifyPlugin;

public class IssueFolderBean implements Comparable, Serializable {
	private static final long serialVersionUID = 9056328734887354382L;

	public static final String ATTRIBUTE_VALUE_ALL = "All";

	public static final String NAME_CRITICAL = "Critical";
	public static final String NAME_HIGH = "High";
	public static final String NAME_MEDIUM = "Medium";
	public static final String NAME_LOW = "Low";

	// for SCA below 5.8
	public static final String NAME_HOT = "Hot";
	public static final String NAME_WARNING = "Warning";
	public static final String NAME_INFO = "Info";

	private final String id;
	private final String projectName;
	private final String projectVersion;
	private final String name;
	private final String description;
	private final String color;
	private int issueCount; // total number of issues in this folder
	private int issueNewCount; // quantity of "New" issues in this folder (generally issueCount >=
								// issueNewCount >= 0)
	private String url;

	public IssueFolderBean(String id, String projectName, String projectVersion, List<String> attributes) {
		this.id = id;
		this.projectName = projectName;
		this.projectVersion = projectVersion;
		this.name = attributes.get(0);
		this.description = attributes.get(1);
		this.color = attributes.get(2);
		try {
			this.issueCount = Integer.parseInt(attributes.get(3));
			this.issueNewCount = Integer.parseInt(attributes.get(4));
		} catch (NumberFormatException e) {
			// ignore
		}

		try {
			this.url = FortifyPlugin.DESCRIPTOR.getUrl() + "/html/ssc/html/index.jsp" + "?projectName="
					+ URLEncoder.encode(projectName, "UTF-8") + "&projectVersionName="
				+ URLEncoder.encode(projectVersion, "UTF-8") + "&folder=" + id;
		} catch (UnsupportedEncodingException e) {
			// leave empty
			this.url = FortifyPlugin.DESCRIPTOR.getUrl() + "/html/ssc/html/index.jsp";
		}
	}

	public String getId() {
		return id;
	}

	public String getURL() {
		return url;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getProjectVersion() {
		return projectVersion;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getColor() {
		return color;
	}

	public int getIssueCount() {
		return issueCount;
	}

	public int getIssueNewCount() {
		return issueNewCount;
	}

	public boolean isEmpty() {
		return issueCount == 0;
	}

	@Override
	public int compareTo(Object o) {
		IssueFolderBean otherBean = (IssueFolderBean) o;
		return otherBean.getId().compareTo(getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IssueFolderBean) {
			return ((IssueFolderBean)obj).getId().equals(this.getId());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}
}
