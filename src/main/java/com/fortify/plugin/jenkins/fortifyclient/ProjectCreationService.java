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

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;

import com.fortify.plugin.jenkins.fortifyclient.ApiClientWrapper.AppTypeEnum;
import com.fortify.plugin.jenkins.bean.MetaDataDefinition;
import com.fortify.plugin.jenkins.bean.MetaDataSelectedValue;
import com.fortify.plugin.jenkins.bean.MetaDataValue;
import com.fortify.plugin.jenkins.bean.ProjectDataEntry;
import com.fortify.plugin.jenkins.bean.ProjectMetaDataCategory;
import com.fortify.plugin.jenkins.bean.SystemUsageType;
import com.fortify.ssc.restclient.ApiException;
import com.fortify.ssc.restclient.model.AttributeDefinition;
import com.fortify.ssc.restclient.model.AttributeDefinition.TypeEnum;
import com.fortify.ssc.restclient.model.AttributeOption;
import com.fortify.ssc.restclient.model.IssueTemplate;

public class ProjectCreationService {
	private static DatatypeFactory datatypeFactory;
	static {
		try {
			datatypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			System.err.println("Cannot instantiate javax.xml.datatype.DatatypeFactory" + e.getLocalizedMessage());
		}
	}

	public static XMLGregorianCalendar convertDateToXMLGregorianCalender(Date date) {
		if (date == null) {
			return null;
		}

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(date.getTime());
		return datatypeFactory.newXMLGregorianCalendar(calendar);
	}

	private static String DATEFORMAT = "MM/dd/yyyy";

	private final PrintWriter logWriter;

	private ApiClientWrapper apiClientWrapper;

	public ProjectCreationService(PrintWriter log, ApiClientWrapper apiClientWrapper) throws ApiException {
		logWriter = log;
		this.apiClientWrapper = apiClientWrapper;
	}

	/**
	 *
	 * @param projectData
	 *            - entry contains a application name, application version name, and
	 *            fpr filename for creating and uploading an application version.
	 *            Entry also contains a map of application attribute names and
	 *            values. If a application attribute definition does not already
	 *            exist for the name, it will be created.
	 *
	 * @throws ApiException
	 * @throws IOException
	 */
	public Long createProject(ProjectDataEntry projectData) throws IOException, ApiException {
		String projectName = projectData.getProjectName();
		String projectVersionName = projectData.getProjectVersionName();
		Long applicationVersionId = getAppVersionIdIfExists(projectName, projectVersionName); // get app version id if
																								// app version already
																								// exists

		if (applicationVersionId != null) {
			return applicationVersionId;
		}

		List<MetaDataDefinition> serverProjectAttributeDefinitions = new ArrayList<MetaDataDefinition>();
		try {
			List<AttributeDefinition> data = apiClientWrapper.getAttributeDefinitions();

			// TODO - refactor - don't use MetaDataDefinition, MetaDataSelectedValue,etc.
			if (data != null && !data.isEmpty()) {
				for (AttributeDefinition next : data) {
					MetaDataDefinition nextDef = convertAttributeDefinitionToMetaDataDefinition(next);
					serverProjectAttributeDefinitions.add(nextDef);
				}
			}
		} catch (Exception e) {
			logWriter.println(MessageFormat.format("[WARN] REST api call for attribute definitions failed: {0}",
					e.getLocalizedMessage()));
		}

		// these are the application attribute values we'll be setting for the
		// application version
		//List<MetaDataSelectedValue> selectedProjectAttributes = new ArrayList<MetaDataSelectedValue>();

		Set<MetaDataDefinition> definitionsNotSet = new LinkedHashSet<MetaDataDefinition>();
		definitionsNotSet.addAll(serverProjectAttributeDefinitions);

		// set values for required definitions that have not already been set above
		for (MetaDataDefinition definition : definitionsNotSet) {
			if (definition.isRequired()) {
				MetaDataSelectedValue newValue = new MetaDataSelectedValue();
				newValue.setMetaDataDefinitionId(definition.getId());
				newValue.setProjectVersionId(-1l);
				if (Boolean.TRUE.equals(definition.isFreeFormValue())
						|| Boolean.TRUE.equals(definition.isFreeFormLongValue())
						|| Boolean.TRUE.equals(definition.isFreeFormSensitiveValue())) {
					newValue.setFreeFormValue("some default freeform value");
				} else {
					if (Boolean.TRUE.equals(definition.isIntegerValue())) {
						setIntegerValue(String.valueOf(0L), definition, newValue);
					} else if (Boolean.TRUE.equals(definition.isBooleanValue())) {
						setBooleanValue(String.valueOf(false), definition, newValue);
					} else if (Boolean.TRUE.equals(definition.isDateValue())) {
						final DateFormat mmddyyyy = new SimpleDateFormat(DATEFORMAT);
						setDateValue(mmddyyyy.format(new Date()), definition, newValue);
					} else {
						List<MetaDataValue> values = definition.getValue();
						if (!values.isEmpty()) {
							newValue.getValue().add(values.get(0));
						}
					}
				}
				//selectedProjectAttributes.add(newValue);
			}
		}

		// get the issue templates on the server
		List<IssueTemplate> projectTemplates = null;
		try {
			projectTemplates = apiClientWrapper.getIssueTemplates();
		} catch (ApiException e) {
			logWriter.println(MessageFormat.format("[ERROR] REST api call to get Issue Templates failed: {0}",
					e.getLocalizedMessage()));
			throw (e);
		}

		String selectedIssueTemplateName = projectData.getProjectTemplateName();
		selectedIssueTemplateName = !StringUtils.isEmpty(selectedIssueTemplateName) ? selectedIssueTemplateName : null;
		IssueTemplate issueTemplate = null;

		// If no default issue template is specified in SSC, the first template will be
		// used.
		IssueTemplate defaultIssueTemplate = getDefaultIssueTemplate(projectTemplates);

		if (selectedIssueTemplateName == null) {
			issueTemplate = defaultIssueTemplate;
			logWriter.printf("No Issue Template selected. Using default template '%s'.%n", issueTemplate.getName());
		} else {
			for (IssueTemplate pt : projectTemplates) {
				if (pt.getName().equals(selectedIssueTemplateName)) {
					issueTemplate = pt;
					break;
				}
			}

			if (issueTemplate != null) {
				logWriter.printf("Selected Issue Template is '%s'%n", issueTemplate.getName()); // Issue template found
			} else {
				issueTemplate = defaultIssueTemplate; // selected issue template is not valid so use default template
				logWriter.printf("Specified Issue Template ='%s' doesn't exist, template '%s' is used instead!%n",
						selectedIssueTemplateName, issueTemplate.getName());
			}
		}

		String masterAttrGuid = issueTemplate.getMasterAttrGuid();
		applicationVersionId = getAppVersionIdOrCreate(projectName, projectVersionName, issueTemplate.getId(),
				masterAttrGuid);

		return applicationVersionId;
	}

	/**
	 * Get the Application Version Id for given appName & appVersionName, if it
	 * exists
	 *
	 * @return Long appVersionId
	 * @throws ApiException
	 */
	public Long getAppVersionIdIfExists(String appName, String appVersionName) throws ApiException {
		Long appId = apiClientWrapper.getApplicationId(appName);
		if (appId != null) {
			return getAppVersionIdIfExists(appId, appVersionName);
		}

		return null;
	}

	private Long getAppVersionIdIfExists(Long appId, String appVersionName) throws ApiException {
		if (appId != null) {
			return apiClientWrapper.getVersionForApplication(appId, appVersionName);
		}

		return null;
	}

	/**
	 * Get the default Issue Template if defined, otherwise return first one in
	 * list.
	 *
	 * @return IssueTemplate
	 * @throws ApiException
	 */
	private IssueTemplate getDefaultIssueTemplate(List<IssueTemplate> issueTemplates) {
		IssueTemplate issueTemplate = null;
		if (issueTemplates != null) {
			for (IssueTemplate it : issueTemplates) {
				Boolean isDefault = it.isDefaultTemplate();
				if (isDefault != null && isDefault.booleanValue()) {
					issueTemplate = it;
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
	 * Get the Application Version Id if the Application Version exists or create a
	 * new one. Creating a new version entails setting the Issue Template and
	 * required Attributes.
	 *
	 * @return Long appVersionId
	 * @throws ApiException
	 */
	private Long getAppVersionIdOrCreate(String appName, String appVersionName, String issueTemplateId,
			String masterAttrGuid) throws ApiException {
		Long applicationId = apiClientWrapper.getApplicationId(appName);
		Long appVersionId;

		if (applicationId != null) {
			appVersionId = apiClientWrapper.getVersionForApplication(applicationId, appVersionName);
			if (appVersionId == null) {
				logWriter.printf("Application version '%s' does not exist for application '%s'. Will create the application version.%n",
						appVersionName, appName);
				appVersionId = apiClientWrapper.createAppOrVersion(applicationId, issueTemplateId, appName,
						appVersionName, masterAttrGuid, ApiClientWrapper.AppTypeEnum.APP_VERSION);
				apiClientWrapper.setDefaultAttributesAndCommit(appVersionId);
			}
		} else {
			logWriter.printf("Application '%s' does not exist. Will create application '%s' and application version '%s'.%n",
					appName, appName, appVersionName);
			appVersionId = apiClientWrapper.createAppOrVersion(null, issueTemplateId, appName, appVersionName,
					masterAttrGuid, AppTypeEnum.APPLICATION);
			apiClientWrapper.setDefaultAttributesAndCommit(appVersionId);
		}

		return appVersionId;
	}

	private void setIntegerValue(final String projectAttributeValue,
			final MetaDataDefinition projectAttributeDefinition, final MetaDataSelectedValue value) {
		try {
			value.setIntegerValue(Long.valueOf(projectAttributeValue));
		} catch (NumberFormatException e) {
			logWriter.printf("[WARN] Failed to set an integer value of '" + projectAttributeValue + "' for '"
					+ projectAttributeDefinition.getName() + "'; continuing with 0%n");
			value.setIntegerValue(0L);
		}
	}

	private void setBooleanValue(final String projectAttributeValue,
			final MetaDataDefinition projectAttributeDefinition, final MetaDataSelectedValue value) {
		try {
			value.setBooleanValue(Boolean.valueOf(projectAttributeValue));
		} catch (Exception e) {
			logWriter.printf("[WARN] Failed to set a boolean value of '" + projectAttributeValue + "' for '"
					+ projectAttributeDefinition.getName() + "'; continuing with false%n");
			value.setBooleanValue(Boolean.FALSE);
		}
	}

	private void setDateValue(final String projectAttributeValue, final MetaDataDefinition projectAttributeDefinition,
			final MetaDataSelectedValue value) {
		try {
			final DateFormat mmddyyyy = new SimpleDateFormat(DATEFORMAT);
			Date date = mmddyyyy.parse(projectAttributeValue);
			value.setDateValue(convertDateToXMLGregorianCalender(date));
		} catch (ParseException e) {
			Date now = new Date();
			logWriter.printf("[WARN] Failed to set date value of '" + projectAttributeValue + "' for '"
					+ projectAttributeDefinition.getName() + "'; continuing with '" + now + "'%n");
			value.setDateValue(convertDateToXMLGregorianCalender(now));
		}
	}

	private MetaDataDefinition convertAttributeDefinitionToMetaDataDefinition(AttributeDefinition attributeDefinition) {
		MetaDataDefinition metaDataDefinition = new MetaDataDefinition();
		metaDataDefinition.setRequired(attributeDefinition.isRequired());
		metaDataDefinition.setName(attributeDefinition.getName());
		metaDataDefinition.setAppEntityType(attributeDefinition.getAppEntityType().name());
		metaDataDefinition.setId(String.valueOf(attributeDefinition.getGuid()));
		metaDataDefinition.setHidden(attributeDefinition.isHidden());
		metaDataDefinition.setType(ProjectMetaDataCategory.fromValue(attributeDefinition.getCategory().name()));
		metaDataDefinition.setSystemUsage(SystemUsageType.fromValue(attributeDefinition.getSystemUsage().name()));
		metaDataDefinition.setDescription(attributeDefinition.getDescription());
		if (attributeDefinition.getType() == TypeEnum.SINGLE || attributeDefinition.getType() == TypeEnum.MULTIPLE) {
			if (attributeDefinition.getOptions() != null && attributeDefinition.getOptions().size() > 0) {
				List<MetaDataValue> values = metaDataDefinition.getValue();
				for (AttributeOption option : attributeDefinition.getOptions()) {
					MetaDataValue nextValue = new MetaDataValue();
					nextValue.setId(String.valueOf(option.getGuid()));
					nextValue.setShortName(option.getName());
					values.add(nextValue);
				}
			}
		} else {
			metaDataDefinition.setBooleanValue(TypeEnum.BOOLEAN == attributeDefinition.getType());
			metaDataDefinition.setDateValue(TypeEnum.DATE == attributeDefinition.getType());
			metaDataDefinition.setFileValue(TypeEnum.FILE == attributeDefinition.getType());
			metaDataDefinition.setFreeFormSensitiveValue(TypeEnum.SENSITIVE_TEXT == attributeDefinition.getType());
			metaDataDefinition.setFreeFormLongValue(TypeEnum.LONG_TEXT == attributeDefinition.getType());
			metaDataDefinition.setFreeFormValue(TypeEnum.TEXT == attributeDefinition.getType());
			metaDataDefinition.setIntegerValue(TypeEnum.INTEGER == attributeDefinition.getType());
			metaDataDefinition.setMultiple(TypeEnum.MULTIPLE == attributeDefinition.getType());
		}
		return metaDataDefinition;
	}

}
