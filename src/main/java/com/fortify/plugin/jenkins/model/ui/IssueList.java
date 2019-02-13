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

import java.util.ArrayList;
import java.util.List;

import com.fortify.plugin.jenkins.fortifyclient.schemawrapper.Issue;

public class IssueList {

	protected List<Issue> issue;
	protected List<CustomIssue> customIssue;
	protected List<RemovedIssue> removedIssue;
	protected List<Object> any;

	/**
	 * Gets the value of the issue property.
	 * 
	 */
	public List<Issue> getIssue() {
		if (issue == null) {
			issue = new ArrayList<Issue>();
		}
		return this.issue;
	}

	/**
	 * Gets the value of the customIssue property.
	 * 
	 */
	public List<CustomIssue> getCustomIssue() {
		if (customIssue == null) {
			customIssue = new ArrayList<CustomIssue>();
		}
		return this.customIssue;
	}

	/**
	 * Gets the value of the removedIssue property.
	 * 
	 */
	public List<RemovedIssue> getRemovedIssue() {
		if (removedIssue == null) {
			removedIssue = new ArrayList<RemovedIssue>();
		}
		return this.removedIssue;
	}

	/**
	 * Gets the value of the any property.
	 * 
	 */
	public List<Object> getAny() {
		if (any == null) {
			any = new ArrayList<Object>();
		}
		return this.any;
	}

}
