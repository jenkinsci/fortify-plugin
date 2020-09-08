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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.fortify.plugin.jenkins.bean.IssueBean;
import com.fortify.ssc.restclient.ApiException;

public class FortifyClientTest {

	private boolean noSSC;
	private String url;
	private String token;
	private String fprName;
	private String projName;
	private String projVersion;
	private Map<String, Long> projects;
	private FortifyClient fortifyclient;

	@Before
	public void g_init() throws Exception {
		noSSC = false;

		String propertyFileName = getPropertyFileName();
		propertyFileName = propertyFileName == null ? "ssc" : propertyFileName;

		try {
			loadSscTestConfiguration(propertyFileName);
		} catch (Exception e) {
			System.out.println("##################################################################");
			System.out.println("Can't find SSC test configuration: " + propertyFileName);
			System.out.println("##################################################################");
			throw e;
		}

		URL u = new URL(url);
		try {
			HttpURLConnection http = (HttpURLConnection) u.openConnection();
			int code = http.getResponseCode();
		} catch (IOException e) {
			noSSC = true;
			System.out.println("###############################################");
			System.out.println("SSC is not running, this test is skipped");
			System.out.println("###############################################");
		}

		fortifyclient = new FortifyClient();
		fortifyclient.init(url, token, null, null, null); // TODO - cleanup later
	}

	@Test
	public void allTests() throws Exception {
		// we have to call this manually because we need to call in this particular
		// order
		testProjectTemplateList();
		testProjectCreate();
		testProjectList();
		testUploadFPR();
		System.out.println("Just uploaded an FPR to SSC, will sleep for 30s to let it process the FPR");
		Thread.sleep(30 * 1000);
		// testAuditFPR();
		testGetFolderIdToAttributesList();
		testGetIssuesByFolder();

		// testCheckAuditScript();
	}

	private void testGetIssuesByFolder() throws Exception {
		if (noSSC)
			return;
		PrintWriter log = new PrintWriter(System.out);
		try {
			Long versionId = fortifyclient.createProject(projName, projVersion, null,
					Collections.<String, String>emptyMap(), log);
			Map<String, List<String>> folderIds = fortifyclient.getFolderIdToAttributesList(versionId, null, log);
			String infoId = "";
			for (String folderId : folderIds.keySet()) {
				String nextName = folderIds.get(folderId).get(0);
				if (nextName.equals("All")) {
					infoId = folderId;
				}
			}
			Map<String, IssueBean> map = fortifyclient.getIssuesByFolderId(versionId, infoId, 0, -1, null, null, null,
					null, Boolean.FALSE, log);
			System.out.printf("Obtained %d issues for '%s' in this order:\n", map.size(), getFullProjectName());
			for (String id : map.keySet()) {
				IssueBean attrs = map.get(id);
				String file = attrs.getFilePath();
				String line = attrs.getLineNumber();
				String category = attrs.getCategory();
				String severity = attrs.getSeverity();
				System.out.printf(">>%s:%s (%s) %s\n", file, line, category, severity);
			}
		} catch (ApiException e) {
			// if due to connection error, probably SSC is not started up
			if (containsRootCause(e, ConnectException.class)) {
				// we should run this anyway...., ignore it
			} else {
				throw e;
			}
		}
	}

	private void testGetFolderIdToAttributesList() throws Exception {
		if (noSSC)
			return;
		PrintWriter log = new PrintWriter(System.out);
		try {
			Long versionId = fortifyclient.createProject(projName, projVersion, null,
					Collections.<String, String>emptyMap(), log);
			Map<String, List<String>> folderIds = fortifyclient.getFolderIdToAttributesList(versionId, null, log);
			List<String> names = new ArrayList<String>(folderIds.size());
			for (String folderId : folderIds.keySet()) {
				names.add(folderIds.get(folderId).get(0));
			}
			System.out.printf("Obtained %d folders for '%s': %s\n", folderIds.size(), getFullProjectName(),
					names.toString());
		} catch (ApiException e) {
			// if due to connection error, probably SSC is not started up
			if (containsRootCause(e, ConnectException.class)) {
				// we should run this anyway...., ignore it
			} else {
				throw e;
			}
		}
	}

	private void testProjectTemplateList() throws Exception {
		if (noSSC)
			return;

		try {
			Map<String, String> projectTemplateList = fortifyclient.getProjectTemplateList();
			for (String s : projectTemplateList.keySet()) {
				System.out.println(s + " -> " + projectTemplateList.get(s));
			}
		} catch (ApiException e) {
			// if due to connection error, probably SSC is not started up
			if (containsRootCause(e, ConnectException.class)) {
				// we should run this anyway...., ignore it
			} else {
				throw e;
			}
		}
	}

	private void testProjectList() throws Exception {
		if (noSSC)
			return;

		try {
			projects = fortifyclient.getProjectList();
			for (String s : projects.keySet()) {
				System.out.println(s + " -> " + projects.get(s));
			}
		} catch (ApiException e) {
			// if due to connection error, probably SSC is not started up
			if (containsRootCause(e, ConnectException.class)) {
				// we should run this anyway...., ignore it
			} else {
				throw e;
			}
		}
	}

	private void testProjectCreate() throws Exception {
		if (noSSC)
			return;

		PrintWriter log = new PrintWriter(System.out);

		try {
			fortifyclient.createProject(projName, projVersion, null, Collections.<String, String>emptyMap(), log);

			System.out.println("Application '" + getFullProjectName() + "' was created.");

		} catch (ApiException e) {
			// if due to connection error, probably SSC is not started up
			if (containsRootCause(e, ConnectException.class)) {
				// we should run this anyway...., ignore it
			} else {
				throw e;
			}
		}
	}

	private void testUploadFPR() throws Exception {
		if (noSSC)
			return;
		// we assume you have called testProjectList(), so projects shouldn't be null
		if (null == projects)
			return;

		try {
			File fpr = resourceToFile(fprName);
			fortifyclient.uploadFPR(fpr, projects.get(getFullProjectName()));
		} catch (ApiException e) {
			// if due to connection error, probably SSC is not started up
			if (containsRootCause(e, ConnectException.class)) {
				// we should run this anyway...., ignore it
			} else {
				throw e;
			}
		}
	}

	private boolean containsRootCause(Throwable t, Class c) {
		while (null != t) {
			t = t.getCause();
			if (null != t && c.isInstance(t)) {
				return true;
			}
		}
		return false;
	}

	private void loadSscTestConfiguration(String propertyFileName) throws Exception {
		File file = resourceToFile(propertyFileName + ".properties");
		Properties prop = new Properties();
		prop.load(new FileInputStream(file));

		projName = prop.getProperty("projName");
		projVersion = prop.getProperty("projVersion");
		fprName = prop.getProperty("fprName");
		url = prop.getProperty("sscUrl");
		token = prop.getProperty("token");

		String sscUrl = getSscUrl();
		if (sscUrl != null && sscUrl.length() > 0) {
			if (sscUrl.contains(":")) {
				url = url.replace("localhost:8180", sscUrl);
			} else {
				url = url.replace("localhost", sscUrl);
			}
		}
	}

	private static String getPropertyFileName() {
		String s = System.getenv("MAVEN_CMD_LINE_ARGS");
		if (null == s) {
			s = ""; // otherwise, it will throw NPE, it won't match anyway
		}
		String x = "(.*\\s)?-Dproperty\\.file=(.+?)(\\s.*)?";
		Pattern p = Pattern.compile(x);
		Matcher m = p.matcher(s);
		if (m.matches()) {
			// it's alwasy group2
			return m.group(2);
		}

		System.out.println("###############################################");
		System.out.println("Please specify mvn -Dproperty.file=[filename]");
		System.out.println("###############################################");
		return null;
	}

	private static String getSscUrl() {
		String s = System.getenv("MAVEN_CMD_LINE_ARGS");
		if (null == s) {
			s = ""; // otherwise, it will throw NPE, it won't match anyway
		}
		String x = "(.*\\s)?-Dssc\\.url=(.+?)(\\s.*)?";
		Pattern p = Pattern.compile(x);
		Matcher m = p.matcher(s);
		if (m.matches()) {
			// it's alwasy group2
			return m.group(2);
		}
		return null;
	}

	private String getFullProjectName() {
		return projName + " (" + projVersion + ")";
	}

	private static File resourceToFile(String filename) throws IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			File tmp = File.createTempFile("test", "." + FilenameUtils.getExtension(filename));
			tmp.deleteOnExit();
			in = FortifyClientTest.class.getClassLoader().getResourceAsStream(filename);
			out = new FileOutputStream(tmp);
			IOUtils.copy(in, out);
			return tmp;
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}
}
