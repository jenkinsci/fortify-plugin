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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;

import com.fortify.plugin.jenkins.FortifyPlugin;
import com.fortify.plugin.jenkins.PathUtils;

public class IssueBean implements Comparable {
	public static final String ATTRIBUTE_VALUE_NONE = "<none>";

	private Long projectVersionId;
	private Long issueId;
	private String issueInstanceId;
	private String projectName;
	private String projectVersionName;

	private String sourceFilePath;
	private String filePath;
	private String lineNumber;
	private String packageName;
	private String className;
	private String function;
	private String groupName;
	private String category;
	private String mappedCategory;
	private String type;
	private String subType;
	private String confidence;
	private String severity;
	private String engineType;
	private String assignedUser;

	public IssueBean() {
	}

	public static String escapeHtmlTags(String unsafeString) {
		if (unsafeString != null) {
			unsafeString = unsafeString.replaceAll("<", "&lt;");
			unsafeString = unsafeString.replaceAll(">", "&gt;");
		}
		return unsafeString;
	}

	public String getDisplayName() {
		StringBuffer label = new StringBuffer();
		String shortfilename = PathUtils.getShortFileName(getFilePath());
		if (StringUtils.isBlank(shortfilename)) {
			shortfilename = ATTRIBUTE_VALUE_NONE;
		}
		label.append(shortfilename).append(':').append(lineNumber);
		return escapeHtmlTags(label.toString());
	}

	public String getDisplayTypeName() {
		StringBuffer label = new StringBuffer();
		label.append(type);
		if (StringUtils.isNotBlank(subType)) {
			label.append(':').append(subType);
		}
		return escapeHtmlTags(label.toString());
	}

	public String getDisplayMappedCategory() {
		return escapeHtmlTags(mappedCategory);
	}

	public String getDisplayPath() {
		return escapeHtmlTags(PathUtils.getPath(getFilePath()));
	}

	public String getURL() {
		try {
			return FortifyPlugin.DESCRIPTOR.getUrl() + "/html/ssc/version/" + projectVersionId + "/fix/" + issueId
					+ "/?projectName=" + URLEncoder.encode(projectName, "UTF-8") + "&projectVersionName="
					+ URLEncoder.encode(projectVersionName, "UTF-8") + "&issue=" + issueInstanceId + "&engineType="
					+ URLEncoder.encode(engineType, "UTF-8");

		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	public Long getProjectVersionId() {
		return projectVersionId;
	}

	public Long getIssueId() {
		return issueId;
	}

	public String getIssueInstanceId() {
		return issueInstanceId;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getProjectVersionName() {
		return projectVersionName;
	}

	public String getFilePath() {
		return filePath;
	}

	public String getLineNumber() {
		return lineNumber;
	}

	public boolean isNew() {
		return true;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getClassName() {
		return className;
	}

	public String getFunction() {
		return function;
	}

	public String getSourceFilePath() {
		return sourceFilePath;
	}

	public String getGroupName() {
		return groupName;
	}

	public String getAssignedUser() {
		return assignedUser;
	}

	public String getCategory() {
		return category;
	}

	public String getType() {
		return type;
	}

	public String getConfidence() {
		return confidence;
	}

	public String getSeverity() {
		return severity;
	}

	public String getSubType() {
		return subType;
	}

	public String getMappedCategory() {
		return mappedCategory;
	}

	public String getEngineType() {
		return engineType;
	}

	public void setProjectVersionId(Long projectVersionId) {
		this.projectVersionId = projectVersionId;
	}

	public void setIssueId(Long issueId) {
		this.issueId = issueId;
	}

	public void setIssueInstanceId(String issueInstanceId) {
		this.issueInstanceId = issueInstanceId;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public void setProjectVersionName(String projectVersionName) {
		this.projectVersionName = projectVersionName;
	}

	public void setSourceFilePath(String sourceFilePath) {
		this.sourceFilePath = sourceFilePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setMappedCategory(String mappedCategory) {
		this.mappedCategory = mappedCategory;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public void setConfidence(String confidence) {
		this.confidence = confidence;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public void setEngineType(String engineType) {
		this.engineType = engineType;
	}

	public void setAssignedUser(String assignedUser) {
		this.assignedUser = assignedUser;
	}

	@Override
	public int compareTo(Object o) {
		IssueBean otherBean = (IssueBean) o;
		return otherBean.getIssueInstanceId().compareTo(getIssueInstanceId());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IssueBean) {
			return ((IssueBean)obj).getIssueInstanceId().equals(this.getIssueInstanceId());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getIssueInstanceId().hashCode();
	}
}
