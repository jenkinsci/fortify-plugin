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
import java.io.FilenameFilter;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

public class PathUtils {
	private static final Pattern FwdSlashSeq = Pattern.compile("//+");

	private static String getEnvironmentPath() {
		Map<String, String> env = System.getenv();
		for (Map.Entry<String, String> entry : env.entrySet()) {
			// the key may be case sensitive on Unix
			if ("PATH".equalsIgnoreCase(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}

	public static File[] locateBasenameInPath(String filename) {
		String path = getEnvironmentPath();
		String pathSep = System.getProperty("path.separator"); // ":" on Unix, ";" on Win
		if (null != path) {
			String[] array = path.split(Pattern.quote(pathSep)); // need to quote the pathSep coz it is metachar
			for (String s : array) {
				File[] files = findBasenameInFolder(s, filename);
				if (null != files && files.length > 0)
					return files;
			}
		}
		return null;
	}

	public static File[] findBasenameInFolder(String pathname, String filename) {
		File path = new File(pathname);
		File[] files = null;
		if (path.exists() && path.isDirectory()) {
			final String finalBasename = FilenameUtils.getBaseName(filename);
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return finalBasename.equalsIgnoreCase(FilenameUtils.getBaseName(name));
				}
			};
			files = path.listFiles(filter);
		}
		return files;
	}

	public static File locateFileInPath(String filename, String path) {
		if (path != null) {
			String[] array = path.split(Pattern.quote(File.pathSeparator));
			for (String s : array) {
				File folder = new File(s);
				if (folder.isDirectory()) {
					File file = new File(folder, filename);
					if (file.isFile()) {
						return file;
					}
				}
			}
		}
		return null;
	}

	public static String getShortFileName(String fullPath) {
		if (fullPath == null) {
			return null;
		}
		// convert all directory separators to /
		String normalizedPath = normalizeDirectorySeparators(fullPath);
		int lastSlash = normalizedPath.lastIndexOf("/");
		if (lastSlash == -1) {
			return fullPath;
		}
		return normalizedPath.substring(lastSlash + 1);
	}

	public static String getPath(String fullFilePath) {
		if (fullFilePath == null) {
			return null;
		}
		// convert all directory separators to /
		String normalizedPath = normalizeDirectorySeparators(fullFilePath);
		int lastSlash = normalizedPath.lastIndexOf("/");
		if (lastSlash == -1) {
			return "";
		}
		return normalizedPath.substring(0, lastSlash + 1);
	}

	public static String normalizeDirectorySeparators(String path) {
		if (path == null) {
			return null;
		}
		String norm = path.replace('\\', '/');
		if (norm.contains("//")) {
			norm = FwdSlashSeq.matcher(norm).replaceAll("/");
		}
		return norm;
	}

	public static String appendExtentionIfNotEmpty(String s, String extention) {
		if (StringUtils.isNotEmpty(s) && !s.toLowerCase().endsWith(extention)) {
			s += extention;
		}
		return s;
	}

}
