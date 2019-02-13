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

/**
 * A simple case to handle version number comparison
 * <p>
 * Only accept numbers in the version number, e.g. 1.2.3.4. Doesn't support
 * alphabets, e.g. 1.2.4a
 * <p>
 * E.g. 1.10 is large than 1.2 <br/>
 * E.g. 1.2.1 is large than 1.2
 * 
 * @author sng
 *
 */
public class VersionNumber implements Comparable {

	private int[] version;

	public VersionNumber(String versionStr) {
		String[] array = versionStr.trim().split("\\.");
		version = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			String nextNr = array[i];
			if (nextNr == null) {
				nextNr = "0";
			}
			String trimmedNr = nextNr.trim();
			if (trimmedNr.length() > 0) {
				try {
					version[i] = Integer.parseInt(trimmedNr);
				} catch (NumberFormatException e) {
					// ignore for now
					version[i] = 0;
				}
			}
		}
	}

	@Override
	public int compareTo(Object o) {
		if (null == o)
			return 1;
		if (o instanceof VersionNumber) {
			VersionNumber that = (VersionNumber) o;
			if (null == that.version && null == this.version)
				return 0;
			else if (null == that.version && null != this.version)
				return 1;
			else if (null == this.version && null != that.version)
				return -1;
			else {
				// both version are not null
				for (int i = 0; i < this.version.length; i++) {
					if (i >= that.version.length)
						return 1;
					else {
						if (this.version[i] > that.version[i])
							return 1;
						else if (that.version[i] > this.version[i])
							return -1;
						// else --> that are equal
					}
				}
				// reach here, can be two conditions
				if (this.version.length == that.version.length)
					return 0;
				return -1;
			}
		} else {
			return 1;
		}
	}

}
