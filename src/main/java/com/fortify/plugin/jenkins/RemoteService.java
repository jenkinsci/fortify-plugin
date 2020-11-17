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
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.remoting.RoleChecker;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;

public class RemoteService implements FilePath.FileCallable<FPRSummary> {

	private static final long serialVersionUID = 229830219491170076L;

	private final String fpr;
	private final StringBuilder logMsg;

	public RemoteService(String fpr) {
		this.fpr = fpr;
		this.logMsg = new StringBuilder();
	}

	@Override
	public FPRSummary invoke(File workspace, VirtualChannel channel) throws IOException {
		FPRSummary summary = new FPRSummary();

		File realFPR = locateFPR(workspace, fpr);
		summary.setFprFile(new FilePath(realFPR));

		// setup log message to FPRSummary
		String s = logMsg.toString();
		if (!StringUtils.isBlank(s)) {
			summary.log(s);
		}
		return summary;
	}

	private static File locateFPR(File workspace, String fprPath) {
		File fpr = new File(fprPath);

		// If full path, locate FPR outside workspace
		if (fpr.isAbsolute()) {
			if (fpr.exists()) {
				return fpr;
			} else {
				throw new RuntimeException("FPR file '" + fpr + "' doesn't exist!");
			}
		}
		// If relative path, search in workspace
		File fprInWorkspace = locateFPRInWorkspace(workspace, fprPath);
		if (null == fprInWorkspace) {
			throw new RuntimeException(
					"Can't locate FPR file '" + fpr + "' under workspace: " + workspace.getAbsolutePath());
		}
		return fprInWorkspace;
	}

	@SuppressWarnings("unchecked")
	private static File locateFPRInWorkspace(File path, String preferredFileName) {
		String ext[] = { "fpr" };
		Iterator<File> iterator = FileUtils.iterateFiles(path, ext, true);

		long latestTime = 0;
		File latestFile = null;
		while (iterator.hasNext()) {
			File file = iterator.next();
			if (isEmpty(preferredFileName) || preferredFileName.equalsIgnoreCase(file.getName())) {
				if (null == latestFile) {
					latestTime = file.lastModified();
					latestFile = file;
				} else {
					// if this file is newer, we will use this file
					if (latestTime < file.lastModified()) {
						latestTime = file.lastModified();
						latestFile = file;

						// else if the last modified time is the same, but this file's file name is
						// shorter, we will use this one
						// this to assume, if you copy the file, the file name is usually "Copy of
						// XXX.fpr"
					} else if (latestTime == file.lastModified()
							&& latestFile.getName().length() > file.getName().length()) {
						latestFile = file;
					}
				}
			}
		}

		return latestFile;
	}

	private static boolean isEmpty(String str) {
		return (null == str || str.length() == 0);
	}

	@Override
	public void checkRoles(RoleChecker arg0) throws SecurityException {
		// do nothing at this time
	}
}
