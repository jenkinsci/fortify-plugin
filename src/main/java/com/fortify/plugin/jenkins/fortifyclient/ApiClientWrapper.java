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
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.fortify.ssc.restclient.api.*;
import com.fortify.ssc.restclient.model.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import com.fortify.ssc.restclient.ApiClient;
import com.fortify.ssc.restclient.ApiException;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class ApiClientWrapper {
	private static final String AUTH_HEADER_TOKEN = "FortifyToken";
	private ApiClient apiClient;

	public enum AppTypeEnum {
		APPLICATION, APP_VERSION
	}

	public ApiClientWrapper(String uri, String token, Integer connectTimeoutSeconds,
							Integer readTimeoutSeconds, Integer writeTimeoutSeconds) throws ApiException {
		apiClient = new ApiClient();
		apiClient.setBasePath(uri + "/api/v1");
		if (connectTimeoutSeconds != null) {
			apiClient.setConnectTimeout(connectTimeoutSeconds * 1000);
		}
		if (readTimeoutSeconds != null) {
			apiClient.setReadTimeout(readTimeoutSeconds * 1000);
		}
		if (writeTimeoutSeconds != null) {
			apiClient.setWriteTimeout(writeTimeoutSeconds * 1000);
		}
		try {
			apiClient.setApiKeyPrefix(AUTH_HEADER_TOKEN);
			apiClient.setApiKey(Base64.encodeBase64String(token.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			String msg = MessageFormat.format("[ERROR] Error encoding SSC auth token : {0}", token);
			throw new ApiException(msg + e.getLocalizedMessage());
		}
	}

	public void setProxy(String proxyHost, int proxyPort, final String proxyUsername, final String proxyPassword) {
		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
		apiClient.getHttpClient().setProxy(proxy);
		if (!(StringUtils.isEmpty(proxyUsername) && StringUtils.isEmpty(proxyPassword))) {
			Authenticator authenticator = new Authenticator() {
				boolean proxyAuthAttempted = false;

				@Override
				public Request authenticate(Proxy proxy, Response response) {
					return response.request();
				}

				@Override
				public Request authenticateProxy(Proxy proxy, Response response) {
					if (proxyAuthAttempted) {
						return null;
					} else {
						proxyAuthAttempted = true;
					}
					String credential = Credentials.basic(proxyUsername, proxyPassword);
					return response.request().newBuilder().header("Proxy-Authorization", credential).build();
				}
			};
			apiClient.getHttpClient().setAuthenticator(authenticator);
		}
	}

	/**
	 * Returns list of Applications in SSC. Returns empty list of no Applications
	 * are found.
	 *
	 * @return List<Project>
	 * @throws ApiException
	 */
	public List<Project> getApplications() throws ApiException {
		List<Project> appList = new ArrayList<Project>();
		ProjectControllerApi projectControllerApi = new ProjectControllerApi(apiClient);

		ApiResultListProject apiResultListProject = projectControllerApi.listProject(null, 0, -1, null, false, null);
		for (Project app : apiResultListProject.getData()) {
			appList.add(app);
		}
		return appList;
	}

	/**
	 * Returns list of Application Versions in SSC. Returns empty list of no
	 * Applications Versions are found.
	 *
	 * @return List<ProjectVersion>
	 * @throws ApiException
	 */
	public List<ProjectVersion> getApplicationVersions() throws ApiException {
		List<ProjectVersion> appVersionList = new ArrayList<ProjectVersion>();
		ProjectVersionControllerApi projectVersionControllerApi = new ProjectVersionControllerApi(apiClient);

		ApiResultListProjectVersion apiResultListProjectVersion = projectVersionControllerApi.listProjectVersion(null,
				0, -1, null, false, null, false, false, null);
		for (ProjectVersion appVersion : apiResultListProjectVersion.getData()) {
			appVersionList.add(appVersion);
		}
		return appVersionList;
	}

	/**
	 * Returns list of Attribute Definitions in SSC. Returns empty list if no
	 * Attribute Definitions are found.
	 *
	 * @return List<AttributeDefinition>
	 * @throws ApiException
	 */
	public List<AttributeDefinition> getAttributeDefinitions() throws ApiException {
		List<AttributeDefinition> attrDefinitionList = new ArrayList<AttributeDefinition>();
		AttributeDefinitionControllerApi attrDefinitionControllerApi = new AttributeDefinitionControllerApi(apiClient);
		ApiResultListAttributeDefinition apiResultListAttrDefinition = attrDefinitionControllerApi
				.listAttributeDefinition(null, Integer.valueOf(0), Integer.MAX_VALUE, null, null);
		for (AttributeDefinition attrDef : apiResultListAttrDefinition.getData()) {
			attrDefinitionList.add(attrDef);
		}
		return attrDefinitionList;
	}

	/**
	 * Returns list of Issue Templates in SSC. Returns empty list if no Issue
	 * Templates are found.
	 *
	 * @return List<IssueTemplate>
	 * @throws ApiException
	 */
	public List<IssueTemplate> getIssueTemplates() throws ApiException {
		List<IssueTemplate> issueTemplateList = new ArrayList<IssueTemplate>();
		IssueTemplateControllerApi issueTemplateControllerApi = new IssueTemplateControllerApi(apiClient);
		ApiResultListIssueTemplate apiResultListIssueTemplate = issueTemplateControllerApi.listIssueTemplate(null,
				Integer.valueOf(0), Integer.MAX_VALUE, null, null);
		for (IssueTemplate issueTemplate : apiResultListIssueTemplate.getData()) {
			issueTemplateList.add(issueTemplate);
		}
		return issueTemplateList;
	}

	/**
	 * Returns a list of CloudScan Pools in SSC. Returns an empty list if no pools are found.
	 * @return List<CloudPool>
	 * @throws ApiException
	 */
	public List<CloudPool> getCloudScanPools() throws ApiException {
		List<CloudPool> cloudPoolList = new ArrayList<>();
		CloudPoolControllerApi cloudPoolControllerApi = new CloudPoolControllerApi(apiClient);
		ApiResultListCloudPool apiResultListCloudPool = cloudPoolControllerApi.listCloudPool("name,uuid",
				Integer.valueOf(0), Integer.MAX_VALUE, null, false, null);
		for (CloudPool cloudPool : apiResultListCloudPool.getData()) {
			cloudPoolList.add(cloudPool);
		}

		return cloudPoolList;
	}

	public List<ProjectVersionIssueGroup> getIssueGroupsForAvs(Long avId, String searchCondition, String folderId,
															   String filterSet, String groupingType) throws ApiException {
		List<ProjectVersionIssueGroup> issueGroups = new ArrayList<ProjectVersionIssueGroup>();
		IssueGroupOfProjectVersionControllerApi issueGroupControllerApi = new IssueGroupOfProjectVersionControllerApi(
				apiClient);

		ApiResultListProjectVersionIssueGroup apiResultList = issueGroupControllerApi.listIssueGroupOfProjectVersion(
				avId, Integer.valueOf(0), Integer.MAX_VALUE, searchCondition, "issues", filterSet, null, false, false, false,
				true, "FOLDER:" + folderId, groupingType);
		for (ProjectVersionIssueGroup projectVersionIssueGroup : apiResultList.getData()) {
			issueGroups.add(projectVersionIssueGroup);
		}
		return issueGroups;
	}

	public List<ProjectVersionIssueGroup> getIssueGroupFolders(Long avId, String filterSet) throws ApiException {
		List<ProjectVersionIssueGroup> issueGroups = new ArrayList<ProjectVersionIssueGroup>();
		IssueGroupOfProjectVersionControllerApi issueGroupControllerApi = new IssueGroupOfProjectVersionControllerApi(
				apiClient);

		ApiResultListProjectVersionIssueGroup apiResultList = issueGroupControllerApi.listIssueGroupOfProjectVersion(
				avId, Integer.valueOf(0), Integer.MAX_VALUE, null, "issues", filterSet, null, false, false, false, true,
				null, "FOLDER");
		for (ProjectVersionIssueGroup projectVersionIssueGroup : apiResultList.getData()) {
			issueGroups.add(projectVersionIssueGroup);
		}
		return issueGroups;
	}

	public ProjectVersionIssueGroup getNewIssueGroupForAv(Long avId, String folderId, String filterSet)
			throws ApiException {
		IssueGroupOfProjectVersionControllerApi issueGroupControllerApi = new IssueGroupOfProjectVersionControllerApi(
				apiClient);

		ApiResultListProjectVersionIssueGroup apiResultList = issueGroupControllerApi.listIssueGroupOfProjectVersion(
				avId, Integer.valueOf(0), Integer.MAX_VALUE, "11111111-1111-1111-1111-111111111167", "issues", null,
				null, false, false, false, true, "FOLDER:" + folderId, filterSet);
		for (ProjectVersionIssueGroup projectVersionIssueGroup : apiResultList.getData()) {
			return projectVersionIssueGroup;
		}
		return null;
	}

	public List<FilterSet> getFilterSetsForAppVersion(Long appVersionId) throws ApiException {
		List<FilterSet> filterSetList = new ArrayList<FilterSet>();
		FilterSetOfProjectVersionControllerApi filterSetOfProjectVersionControllerApi = new FilterSetOfProjectVersionControllerApi(
				apiClient);

		ApiResultListFilterSet apiResultListFilterSet = filterSetOfProjectVersionControllerApi
				.listFilterSetOfProjectVersion(appVersionId, 0, -1, null);
		for (FilterSet filterset : apiResultListFilterSet.getData()) {
			filterSetList.add(filterset);
		}

		return filterSetList;
	}

	public FilterSet getDefaultFilterSetForAppVersion(Long appVersionId) throws ApiException {
		FilterSet defaultFilterSet = null;
		List<FilterSet> filterSetList = getFilterSetsForAppVersion(appVersionId);

		if (filterSetList != null) {
			for (FilterSet filterSet : filterSetList) {
				if (filterSet.isDefaultFilterSet()) {
					defaultFilterSet = filterSet;
					break;
				}
			}
		}

		if (defaultFilterSet == null) {
			defaultFilterSet = filterSetList.get(0);
		}
		return defaultFilterSet;
	}

	public List<ProjectVersionIssue> getIssuesForAppVersion(Long appVersionId, int startPage, int pageSize,
															String filter, String groupId, String groupingType) throws ApiException {
		List<ProjectVersionIssue> issues = new ArrayList<ProjectVersionIssue>();
		IssueOfProjectVersionControllerApi issueSetOfProjectVersionControllerApi = new IssueOfProjectVersionControllerApi(
				apiClient);

		ApiResultListProjectVersionIssue apiResultListProjectVersionIssue = issueSetOfProjectVersionControllerApi
				// .listIssueOfProjectVersion(parentId, start, limit, q, qm, orderby, filterset,
				// fields, showhidden, showremoved, showsuppressed, showshortfilenames, filter,
				// groupid, groupingtype)
				.listIssueOfProjectVersion(appVersionId, startPage, pageSize, null, null, "issueName", null, null,
						false, false, false, true, filter, null, null);
		for (ProjectVersionIssue issue : apiResultListProjectVersionIssue.getData()) {
			issues.add(issue);
		}

		return issues;
	}

	public List<IssueSelector> getGroupBySetForAppVersion(Long appVersionId) throws ApiException {
		List<IssueSelector> list = new ArrayList<IssueSelector>();
		IssueSelectorSetOfProjectVersionControllerApi issueSelectorSetOfProjectVersionControllerApi = new IssueSelectorSetOfProjectVersionControllerApi(
				apiClient);

		ApiResultIssueFilterSelectorSet apiResultIssueFilterSelectorSet = issueSelectorSetOfProjectVersionControllerApi
				.getIssueSelectorSetOfProjectVersion(appVersionId, null);
		for (IssueSelector item : apiResultIssueFilterSelectorSet.getData().getGroupBySet()) {
			list.add(item);
		}

		return list;
	}

	public Long getApplicationId(String appName) throws ApiException {
		Project application = null;
		if (!StringUtils.isEmpty(appName)) {
			String appQuery = "name:\"" + appName + "\"";
			ProjectControllerApi projectControllerApi = new ProjectControllerApi(apiClient);

			ApiResultListProject apiResultListProject = projectControllerApi.listProject(null, 0, 1, appQuery, false,
					null);
			if (apiResultListProject.getData().size() > 0) {
				application = apiResultListProject.getData().get(0);
			}
		}
		return application != null ? application.getId() : null;
	}

	public Long getVersionForApplication(Long applicationId, String appVersionName) throws ApiException {
		ProjectVersion applicationVersion = null;
		String versionQuery = "name:\"" + appVersionName + "\"";
		ProjectVersionOfProjectControllerApi projectVersionOfProjectControllerApi = new ProjectVersionOfProjectControllerApi(
				apiClient);
		ApiResultListProjectVersion apiResultListProjectVersion;

		apiResultListProjectVersion = projectVersionOfProjectControllerApi.listProjectVersionOfProject(applicationId,
				null, 0, 1, versionQuery, false, null, false, false);

		if (apiResultListProjectVersion.getData().size() > 0) {
			applicationVersion = apiResultListProjectVersion.getData().get(0);
		}
		return applicationVersion != null ? applicationVersion.getId() : null;
	}

	/**
	 * If the Application exists, use the same Issue Template. If not, search for
	 * the default Issue Template. If no default is set, use the first Issue
	 * Template found.
	 *
	 * @throws ApiException
	 */
	public IssueTemplate getIssueTemplate(Project application) throws ApiException {
		IssueTemplate issueTemplate = null;
		IssueTemplateControllerApi issueTemplateControllerApi = new IssueTemplateControllerApi(apiClient);
		ApiResultIssueTemplate apiResultIssueTemplate;
		if (application != null && application.getIssueTemplateId() != null) {
			apiResultIssueTemplate = issueTemplateControllerApi.readIssueTemplate(application.getIssueTemplateId(),
					null);
			issueTemplate = apiResultIssueTemplate.getData();
		} else {
			issueTemplate = getDefaultIssueTemplate();
		}
		return issueTemplate;
	}

	/**
	 * Get the default Issue Template if defined, otherwise return first one in
	 * list.
	 *
	 * @return IssueTemplate
	 * @throws ApiException
	 */
	private IssueTemplate getDefaultIssueTemplate() throws ApiException {
		IssueTemplate issueTemplate = null;
		List<IssueTemplate> issueTemplates = getIssueTemplates();
		if (issueTemplates != null) {
			for (IssueTemplate template : issueTemplates) {
				if (template != null && template.isDefaultTemplate()) {
					issueTemplate = template;
					break;
				}
			}
			if (issueTemplate == null) {
				issueTemplate = issueTemplates.get(0);
			}
		}

		return issueTemplate;
	}

	/**
	 * Create a new Application Version or both a new Application and a new
	 * Application Version
	 *
	 * @param appId
	 *            - optional
	 * @param issueTemplateId
	 * @param masterAttrGuid
	 * @param type
	 * @return Long appVersionId
	 * @throws ApiException
	 */
	public Long createAppOrVersion(Long appId, String issueTemplateId, String appName, String appVersionName,
								   String masterAttrGuid, AppTypeEnum type) throws ApiException {
		Long appVersionId;
		ProjectVersion appVersion = new ProjectVersion();
		appVersion.setName(appVersionName);
		appVersion.setIssueTemplateId(issueTemplateId);
		appVersion.setMasterAttrGuid(masterAttrGuid);
		appVersion.setActive(true);
		appVersion.setCommitted(false);

		ApiResultProjectVersion apiResultProjectVersion;

		if (type == AppTypeEnum.APPLICATION) {
			Project app = new Project();
			app.setName(appName);
			if (issueTemplateId != null) {
				app.setIssueTemplateId(issueTemplateId);
			} else {
				IssueTemplate it = getDefaultIssueTemplate();
				if (it != null) {
					app.setIssueTemplateId(it.getId());
				}
			}

			appVersion.setProject(app);

			ProjectVersionControllerApi projectVersionControllerApi = new ProjectVersionControllerApi(apiClient);
			try {
				apiResultProjectVersion = projectVersionControllerApi.createProjectVersion(appVersion);
				ProjectVersion newVersion = apiResultProjectVersion.getData();
				appVersionId = newVersion.getId();
			} catch (ApiException ex) {
				throw new ApiException(ex);
			}
		} else {
			ProjectVersionOfProjectControllerApi projectVersionOfProjectControllerApi = new ProjectVersionOfProjectControllerApi(
					apiClient);
			try {
				apiResultProjectVersion = projectVersionOfProjectControllerApi.createProjectVersionOfProject(appId,
						appVersion);
				ProjectVersion newVersion = apiResultProjectVersion.getData();
				appVersionId = newVersion.getId();
			} catch (ApiException ex) {
				throw new ApiException(ex);
			}
		}

		return appVersionId;
	}

	/**
	 * Set required Attributes for the Application Version then commit to make it
	 * usable
	 *
	 * @throws ApiException
	 */
	public void setDefaultAttributesAndCommit(Long appVersionId) throws ApiException {
		AttributeDefinitionControllerApi attributeDefinitionControllerApi = new AttributeDefinitionControllerApi(
				apiClient);
		String required = "required:true";
		List<Attribute> reqAttributes = new ArrayList<Attribute>();
		try {
			ApiResultListAttributeDefinition apiResultListAttributeDefinition = attributeDefinitionControllerApi
					.listAttributeDefinition(null, 0, 0, required, null);

			for (AttributeDefinition def : apiResultListAttributeDefinition.getData()) {
				if (!def.isHasDefault()) {
					if ("URL".equals(def.getName())) {
						break;
					}
					if (def.getType() == AttributeDefinition.TypeEnum.TEXT) {
						Attribute attribute = new Attribute();
						attribute.setValue("changeme");
						attribute.setAttributeDefinitionId(def.getId());
						reqAttributes.add(attribute);
					} else if (def.getType() == AttributeDefinition.TypeEnum.SINGLE
							|| def.getType() == AttributeDefinition.TypeEnum.MULTIPLE) {
						Attribute attribute = new Attribute();
						AttributeOption first = def.getOptions().get(0);
						ArrayList<AttributeOption> options = new ArrayList<AttributeOption>();
						options.add(first);
						attribute.setValues(options);
						attribute.setAttributeDefinitionId(def.getId());
						reqAttributes.add(attribute);
					}
				}
			}
		} catch (ApiException ex) {
			throw new ApiException(ex);
		}

		// set required Attributes
		AttributeOfProjectVersionControllerApi attributeOfProjectVersionControllerApi = new AttributeOfProjectVersionControllerApi(
				apiClient);
		try {
			for (Attribute att : reqAttributes) {
				attributeOfProjectVersionControllerApi.createAttributeOfProjectVersion(appVersionId, att);
			}
		} catch (ApiException ex) {
			throw new ApiException(ex);
		}

		// commit new App Version for use
		ProjectVersionControllerApi projectVersionControllerApi = new ProjectVersionControllerApi(apiClient);
		try {
			ProjectVersion appVersion = new ProjectVersion();
			appVersion.setCommitted(true);
			projectVersionControllerApi.updateProjectVersion(appVersionId, appVersion);
		} catch (ApiException ex) {
			throw new ApiException(ex);
		}
	}

	/**
	 *
	 * @param fpr
	 * @param appVersionId
	 * @return id of the uploaded artifact
	 * @throws ApiException
	 */
	public Long uploadFpr(@Nonnull File fpr, @Nonnull Long appVersionId) throws ApiException {
		ArtifactOfProjectVersionControllerApi artifactOfProjectVersionControllerApi = new ArtifactOfProjectVersionControllerApi(
				apiClient);
		ApiResultArtifact result = artifactOfProjectVersionControllerApi.uploadArtifactOfProjectVersion(appVersionId,
				fpr, null);
		return result.getData().getId();
	}

	public Artifact getArtifactInfo(@Nonnull Long artifactId) throws ApiException {
		ArtifactControllerApi artifactControllerApi = new ArtifactControllerApi(apiClient);
		return artifactControllerApi.readArtifact(artifactId, null, null).getData();
	}

	public List<Folder> getFoldersForAppVersion(Long appVersionId) throws ApiException {
		List<Folder> folders = new ArrayList<Folder>();
		FolderOfProjectVersionControllerApi folderOfProjectVersionControllerApi = new FolderOfProjectVersionControllerApi(
				apiClient);

		ApiResultListFolder apiResultListFolder = folderOfProjectVersionControllerApi
				.listFolderOfProjectVersion(appVersionId);
		for (Folder folder : apiResultListFolder.getData()) {
			folders.add(folder);
		}

		return folders;
	}
}
