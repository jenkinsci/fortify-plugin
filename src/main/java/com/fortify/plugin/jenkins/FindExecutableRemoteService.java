/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates.
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

import org.jenkinsci.remoting.RoleChecker;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;

public class FindExecutableRemoteService implements FilePath.FileCallable<String> {
	private final String filename;
	private String home;
	private String path;
	private final FilePath workspace;

	/**
	 * Searches the PATH on the remote machine
	 * 
	 * @param filename
	 *            - name of the executable to look for
	 * @param home
	 *            - potential home directory - if not null, will first search
	 *            <home>/bin/filename
	 * @param path
	 *            - path environment variable - depending on OS, must use correct
	 *            path separator
	 * @param workspace
	 *            - current workspace - useful to find gradlew
	 */
	public FindExecutableRemoteService(String filename, String home, String path, FilePath workspace) {
		this.filename = filename;
		this.home = home;
		this.path = path;
		this.workspace = workspace;
	}

	@Override
	public String invoke(File file, VirtualChannel channel) throws IOException {
		if (home != null) {
			if (!home.endsWith(File.separator)) {
				home += File.separator;
			}
			String s = home + "bin" + File.separator + filename;
			File f = new File(s);
			if (f.isFile()) {
				return s;
			}
		}
		// if no path is passed in, get the system path
		if (path == null) {
			path = System.getenv("PATH");
		}
		File f = PathUtils.locateFileInPath(filename, path);
		if (f != null) {
			return f.getPath();
		}
		if (workspace != null) {
			f = PathUtils.locateFileInPath(filename, workspace.getRemote());
			if (f != null) {
				return f.getPath();
			}
		}
		return null;
	}

	@Override
	public void checkRoles(RoleChecker arg0) throws SecurityException {
		// do nothing at this time
	}
}
