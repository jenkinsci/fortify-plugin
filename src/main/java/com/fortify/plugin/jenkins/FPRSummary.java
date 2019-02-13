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
package com.fortify.plugin.jenkins;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.fortify.plugin.jenkins.bean.IssueFolderBean;
import com.thoughtworks.xstream.XStream;

import hudson.FilePath;
import hudson.XmlFile;
import hudson.util.XStream2;

public class FPRSummary implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String FILE_BASENAME = "fortify";
	private static final String FILE_EXTENSION = ".xml";

	private FilePath fprFile;
	private String logMsg;

	private transient PersistentSummary persistentSummary;

	public FPRSummary() {
		persistentSummary = new PersistentSummary();
	}

	private String buildFilename(String appName, String appVersion) {
		String filename = FILE_BASENAME;
		if (appName != null) {
			filename += "-" + appName;
		}
		if (appVersion != null) {
			filename += "-" + appVersion;
		}
		filename += FILE_EXTENSION;
		return filename;
	}

	public void load(File parent, String appName, String appVersion) throws IOException {
		File file = new File(parent, buildFilename(appName, appVersion));
		XmlFile xml = new XmlFile(getXStream(), file);
		persistentSummary = (PersistentSummary) xml.read();
	}

	public void save(File parent, String appName, String appVersion) throws IOException {
		// save data under the builds directory, this is always in Jenkins master node
		File file = new File(parent, buildFilename(appName, appVersion));
		XmlFile xml = new XmlFile(getXStream(), file);
		xml.write(persistentSummary);
	}

	public double getNvs() {
		return persistentSummary.nvs;
	}

	public void setNvs(double nvs) {
		persistentSummary.nvs = nvs;
	}

	public void setFolderBeans(List<IssueFolderBean> folderBeans) {
		persistentSummary.folderBeans = folderBeans;
	}

	public List<IssueFolderBean> getFolderBeans() {
		return null == persistentSummary.folderBeans ? Collections.<IssueFolderBean>emptyList()
				: persistentSummary.folderBeans;
	}

	public FilePath getFprFile() {
		return fprFile;
	}

	public void setFprFile(FilePath fprFile) {
		this.fprFile = fprFile;
	}

	public int getFailedCount() {
		return persistentSummary.failedCount;
	}

	public void setFailedCount(int failedCount) {
		persistentSummary.failedCount = failedCount;
	}

	public int getTotalIssues() {
		return persistentSummary.totalIssues;
	}

	public void setTotalIssues(int totalIssues) {
		persistentSummary.totalIssues = totalIssues;
	}

	public void log(String msg) {
		if (null == logMsg) {
			logMsg = msg;
		} else if (null != msg) {
			this.logMsg += msg;
		}
	}

	public String getLogMessage() {
		return logMsg;
	}

	private XStream getXStream() {
		XStream stream = new XStream2();
		stream.alias("issueFolder", IssueFolderBean.class);
		stream.alias("fortifySummary", PersistentSummary.class);
		return stream;
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		persistentSummary = new PersistentSummary();
	}

	private static class PersistentSummary {
		private int failedCount;
		private double nvs;
		private List<IssueFolderBean> folderBeans;
		private int totalIssues;
	}

}
