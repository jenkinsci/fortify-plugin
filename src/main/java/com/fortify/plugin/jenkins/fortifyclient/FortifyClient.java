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
package com.fortify.plugin.jenkins.fortifyclient;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fortify.ssc.restclient.model.*;
import org.apache.commons.lang.StringUtils;

import com.fortify.plugin.jenkins.bean.GroupingProfile;
import com.fortify.plugin.jenkins.bean.IssueBean;
import com.fortify.plugin.jenkins.bean.ProjectDataEntry;
import com.fortify.ssc.restclient.ApiException;

/**
 * FortifyClient is basically a wrapper around SSC's REST client API
 *
 */
public class FortifyClient {

	public static interface Command<T> {
		T runWith(FortifyClient client) throws Exception;
	}

	public static class NoReturn {
		public static final NoReturn INSTANCE = new NoReturn();

		private NoReturn() {
		}
	}

	private ApiClientWrapper apiClientWrapper;

	/**
	 * You have to call this init function before performance any operations
	 *
	 * @param uri
	 *            e.g. https://localhost:8180/ssc
	 * @param token
	 *            e.g. the AuditToken
	 */
	public void init(String uri, String token, Integer connectTimeoutSeconds, Integer readTimeoutSeconds,
					 Integer writeTimeoutSeconds) throws ApiException {
		apiClientWrapper = new ApiClientWrapper(uri, token, connectTimeoutSeconds, readTimeoutSeconds, writeTimeoutSeconds);
	}

	public void init(String uri, String token, String proxyHost, int proxyPort, String proxyUsername, String proxyPassword,
					 Integer connectTimeoutSeconds, Integer readTimeoutSeconds, Integer writeTimeoutSeconds) throws ApiException {
		init(uri, token, connectTimeoutSeconds, readTimeoutSeconds, writeTimeoutSeconds);
		apiClientWrapper.setProxy(proxyHost, proxyPort, proxyUsername, proxyPassword);
	}

	/**
	 * Retrieve the application version list from SSC
	 *
	 * @return Map<String, Long>
	 * @throws ApiException
	 */
	public Map<String, Long> getProjectList() throws ApiException {
		Map<String, Long> projectList = new LinkedHashMap<String, Long>();

		Map<String, Map<String, Long>> allProjects = getProjectListEx();
		for (Map.Entry<String, Map<String, Long>> prjEntry : allProjects.entrySet()) {
			Map<String, Long> prjVersions = prjEntry.getValue();
			for (Map.Entry<String, Long> prjVersion : prjVersions.entrySet()) {
				Long versionId = prjVersion.getValue();
				projectList.put(prjEntry.getKey() + " (" + prjVersion.getKey() + ")", versionId);
			}
		}
		return projectList;
	}

	/**
	 * Retrieve the application version list from SSC
	 *
	 * @return Map<String, Map<String, Long>>
	 * @throws ApiException
	 */
	public Map<String, Map<String, Long>> getProjectListEx() throws ApiException {
		List<ProjectVersion> pversions = apiClientWrapper.getApplicationVersions();
		Map<String, Map<String, Long>> projectList = new LinkedHashMap<String, Map<String, Long>>();
		for (ProjectVersion version : pversions) {
			String projectName = version.getProject().getName();
			Map<String, Long> mapVersions = projectList.get(projectName);
			if (null == mapVersions) {
				mapVersions = new LinkedHashMap<String, Long>();
				projectList.put(projectName, mapVersions);
			}
			mapVersions.put(version.getName(), version.getId());
		}

		return projectList;
	}

	/**
	 * Retrieve the issue template list from SSC
	 *
	 * @return map container where template name maps to template id
	 * @throws ApiException
	 */
	public Map<String, String> getProjectTemplateList() throws ApiException {
		List<IssueTemplate> issueTemplates = apiClientWrapper.getIssueTemplates();

		Map<String, String> issueTemplateList = new LinkedHashMap<String, String>();
		for (IssueTemplate issueTemplate : issueTemplates) {
			issueTemplateList.put(issueTemplate.getName(), issueTemplate.getId());
		}
		return issueTemplateList;
	}

	/**
	 * Retrieve the CloudScan Pool list from SSC
	 *
	 * @return map container where pool name maps to pool uuid
	 * @throws ApiException
	 */
	public Map<String, String> getCloudScanPoolList() throws ApiException {
		List<CloudPool> csPools = apiClientWrapper.getCloudScanPools();

		Map<String, String> csPoolList = new LinkedHashMap<String, String>();
		for (CloudPool cloudPool : csPools) {
			csPoolList.put(cloudPool.getName(), cloudPool.getUuid());
		}
		return csPoolList;
	}

	/**
	 * Upload an FPR to SSC server
	 *
	 * @param fpr
	 *            the FPR file to be uploaded
	 * @param appVersionId
	 *            the SSC application version ID
	 * @throws ApiException
	 */
	public Long uploadFPR(File fpr, Long appVersionId) throws ApiException {
		return apiClientWrapper.uploadFpr(fpr, appVersionId);
	}

	public Artifact getArtifactInfo(Long artifactId) throws ApiException {
		return apiClientWrapper.getArtifactInfo(artifactId);
	}

	/**
	 * Create new or retrieve existing application version on SSC
	 *
	 * @param projectName
	 *            name of the new application
	 * @param projectVersionName
	 *            version of the new application version
	 * @param projectTemplateName
	 *            name of the template used for application creation, may be
	 *            <code>null</code>
	 * @param attributeNamesAndValues
	 *            attributes for the new application
	 * @param log
	 *            logger
	 * @return Long Application version Id of the created application version
	 * @throws ApiException
	 */
	public Long createProject(String projectName, String projectVersionName, String projectTemplateName,
							  Map<String, String> attributeNamesAndValues, PrintWriter log) throws IOException, ApiException {

		ProjectCreationService pcs = new ProjectCreationService(log, apiClientWrapper);
		ProjectDataEntry projectData = new ProjectDataEntry(projectName, projectVersionName, projectTemplateName,
				attributeNamesAndValues);

		return pcs.createProject(projectData);
	}

	/**
	 * Returns all issues in the specified folder with their attributes.
	 *
	 * @param projectVersionId
	 *            id of the application version to audit
	 * @return Map<String, IssueBean>
	 * @throws ApiException
	 */
	public Map<String, IssueBean> getIssuesByFolderId(Long projectVersionId, String folderId, int startPage,
													  int pageSize, String filterSet, String groupingName, String sortOrder, Boolean ShowOnlyNewIssues,
													  Boolean sortDownNotUp, PrintWriter log) throws ApiException {

		Map<String, IssueBean> result = new LinkedHashMap<String, IssueBean>();
		String filter = "FOLDER:" + folderId;
		List<ProjectVersionIssue> issues = apiClientWrapper.getIssuesForAppVersion(projectVersionId,
				startPage * pageSize, pageSize, filter, null, null);

		for (ProjectVersionIssue issue : issues) {
			IssueBean issueBean = new IssueBean();
			issueBean.setProjectVersionId(projectVersionId);
			issueBean.setIssueId(issue.getId());
			issueBean.setIssueInstanceId(issue.getIssueInstanceId());
			issueBean.setPackageName(null); /* package */
			issueBean.setClassName(null); /* className */
			issueBean.setFunction(null); /* function */
			issueBean.setSourceFilePath(issue.getFullFileName()); /* sourceFilePath */
			issueBean.setFilePath(issue.getFullFileName());
			/* filePath */;
			issueBean.setLineNumber(String.valueOf(issue.getLineNumber()));
			issueBean.setGroupName(issue.getIssueName());
			issueBean.setAssignedUser(null); /* assigned user */
			issueBean.setCategory(issue.getIssueName()); /* category */
			issueBean.setType(issue.getIssueName()); /* type */
			issueBean.setConfidence(String.valueOf(issue.getConfidence()));
			issueBean.setSeverity(String.valueOf(issue.getSeverity()));
			issueBean.setSubType(null); /* subType */
			issueBean.setMappedCategory(issue.getIssueName()); // this is what's displayed in the last column of Fortify
			// Assessment based on Group By selection
			issueBean.setEngineType(issue.getEngineType());
			result.put(issue.getIssueInstanceId(), issueBean);
		}

		return result;
	}

	public Map<String, List<String>> getGroupingValues(Long projectVersionId, String folderId, String filterSet,
													   String searchCondition, String groupingName, String groupingType, PrintWriter log) throws ApiException {

		Map<String, List<String>> result = new LinkedHashMap<String, List<String>>();

		groupingType = groupingType == null ? getGroupingType(projectVersionId, groupingName, log) : groupingType;

		List<ProjectVersionIssueGroup> issueGroups = apiClientWrapper.getIssueGroupsForAvs(projectVersionId,
				searchCondition, folderId, filterSet, groupingType);
		if (issueGroups != null) {
			for (ProjectVersionIssueGroup issueGroup : issueGroups) {
				List<String> attributes = new ArrayList<String>();
				attributes.add(issueGroup.getName());
				attributes.add(String.valueOf(issueGroup.getTotalCount()));
				attributes.add(String.valueOf(issueGroup.getVisibleCount()));
				attributes.add(String.valueOf(issueGroup.getAuditedCount()));
				attributes.add(issueGroup.getCleanName()); // need this when id is a number for Analysis tag value or -1
				// for "Not Set"
				result.put(issueGroup.getId(), attributes);
			}
		}

		return result;
	}

	/**
	 * Returns all issues matched specified search condition with their attributes.
	 *
	 * @param projectVersionId
	 *            id of the application version to audit
	 * @return Map<String, List<String>> map of attribute id -> list of attributes:
	 *         package, className, function, sourceFilePath, filePath, lineNumber,
	 *         url, groupName, assignedUser, category, type, confidence, severity
	 * @throws ApiException
	 */
	public Map<String, List<String>> getGroupingValues(Long projectVersionId, String folderId, String filterSet,
													   String searchCondition, String groupingName, PrintWriter log) throws ApiException {

		return getGroupingValues(projectVersionId, folderId, filterSet, searchCondition, groupingName, null, log);
	}

	/**
	 * Returns all enabled folder ids with their attributes.
	 *
	 * @param versionId
	 *            id of the application version to audit
	 * @return Map<String, List<String>> map of folder id -> list of attributes:
	 *         name, description, color, totalIssueCount
	 * @throws ApiException
	 */
	public Map<String, List<String>> getFolderIdToAttributesList(Long versionId, String filterSetGuid, PrintWriter log)
			throws ApiException {
		Map<String, List<String>> result = new LinkedHashMap<String, List<String>>();
		FilterSet defaultFilterSet = apiClientWrapper.getDefaultFilterSetForAppVersion(versionId);

		if (StringUtils.isEmpty(filterSetGuid)) {
			filterSetGuid = defaultFilterSet.getGuid();
		}

		List<Folder> folders = apiClientWrapper.getFoldersForAppVersion(versionId); // superset of folders (e.g.
		// Critical, High, Medium, Low,
		// Likely, Possible,...)
		List<FolderDto> folderDtoList = defaultFilterSet.getFolders(); // folders specific for defaultFilterSet (e.g.
		// Critical, High, Medium, Low)

		List<ProjectVersionIssueGroup> issueGroupFolders = apiClientWrapper.getIssueGroupFolders(versionId,
				filterSetGuid);
		int allTotalCount = 0;
		int allNewIssuesCount = 0;
		for (FolderDto folderDto : folderDtoList) {
			for (Folder folder : folders) {
				// filter out folders that are not specified in defaultFilterSet
				if (folderDto.getGuid().equals(folder.getGuid())) {
					List<String> attributes = new ArrayList<String>();
					attributes.add(folder.getName());
					attributes.add(folder.getDescription());
					attributes.add(folder.getColor());
					int totalCount = getIssueGroupCountForFolder(issueGroupFolders, folder.getName());
					allTotalCount += totalCount;
					attributes.add(String.valueOf(totalCount));
					int newIssuesCount = getNewIssueCountForFolder(versionId, folder.getGuid(), filterSetGuid);
					allNewIssuesCount += newIssuesCount;
					attributes.add(String.valueOf(newIssuesCount));
					result.put(folder.getGuid(), attributes);
				}
			}
		}

		addAllFolderInfo(result, allTotalCount, allNewIssuesCount); // add info for "All" folder to result

		return result;
	}

	/**
	 * Retrieves application version id from SSC by given application name and application version name.
	 *
	 * @param appName
	 * @param appVersionName
	 * @return application version id
	 * @throws ApiException
	 */
	public Long getProjectVersionId(String appName, String appVersionName) throws ApiException {
		final Long applicationId = apiClientWrapper.getApplicationId(appName);
		return apiClientWrapper.getVersionForApplication(applicationId, appVersionName);
	}

	private void addAllFolderInfo(Map<String, List<String>> result, int allTotalCount, int allNewIssuesCount) {
		List<String> attributes = new ArrayList<String>();
		attributes.add("All");
		attributes.add("");
		attributes.add("80A958");
		attributes.add(String.valueOf(allTotalCount));
		attributes.add(String.valueOf(allNewIssuesCount));
		result.put("f599639d-f500-e046-2fd1-d82b5e9b26b4", attributes);
	}

	private int getIssueGroupCountForFolder(List<ProjectVersionIssueGroup> issueGroupFolders, String folderId) {
		for (ProjectVersionIssueGroup issueGroupFolder : issueGroupFolders) {
			if (issueGroupFolder.getCleanName().equals(folderId)) {
				return issueGroupFolder.getTotalCount();
			}
		}

		return 0;
	}

	private int getNewIssueCountForFolder(Long versionId, String folderId, String filterset) throws ApiException {
		ProjectVersionIssueGroup issueGroupFolder = null;

		issueGroupFolder = apiClientWrapper.getNewIssueGroupForAv(versionId, folderId, filterset);
		if (issueGroupFolder != null) {
			return issueGroupFolder.getTotalCount();
		}

		return 0;
	}

	public List<GroupingProfile> getGroupingProfiles(Long versionId, String filterSet, PrintWriter log)
			throws ApiException {
		List<GroupingProfile> groupingProfiles = new ArrayList<GroupingProfile>();
		try {
			List<IssueSelector> issueSelectors = apiClientWrapper.getGroupBySetForAppVersion(versionId);
			if (issueSelectors != null) {
				for (IssueSelector issueSelector : issueSelectors) {
					String name = issueSelector.getDisplayName();
					GroupingProfile groupingProfile = new GroupingProfile();
					groupingProfile.setName(name);
					groupingProfile.setGroupingTypeString("true:" + name);
					groupingProfiles.add(groupingProfile);
				}
			}
		} catch (ApiException ae) {
			log.println("Error retrieving Grouping Profiles for app version " + versionId);
			throw (ae);
		}

		return groupingProfiles;
	}

	private String getGroupingType(Long projectVersionId, String groupName, PrintWriter log) {
		String groupByGuid = "";
		try {
			List<IssueSelector> issueSelectors = apiClientWrapper.getGroupBySetForAppVersion(projectVersionId);
			if (issueSelectors != null) {
				for (IssueSelector issueSelector : issueSelectors) {
					if (issueSelector.getDisplayName().equals(groupName)) {
						groupByGuid = issueSelector.getGuid();
						break;
					}
				}
			}
		} catch (ApiException ae) {
			log.println("Error retrieving Grouping Types for app version " + projectVersionId + " and groupName "
					+ groupName);
		}

		return groupByGuid;
	}
}
