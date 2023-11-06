/*******************************************************************************
 * Copyright 2020-2023 Open Text.
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
package com.fortify.plugin.jenkins.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;

import org.apache.commons.lang.StringUtils;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jenkins.ui.icon.IconType;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Default implementation of {@link FortifyApiToken} for use by Jenkins {@link com.cloudbees.plugins.credentials.CredentialsProvider}
 * instances that store {@link Secret} locally.
 */
public class StandardFortifyApiToken extends BaseStandardCredentials implements FortifyApiToken {

	private static final long serialVersionUID = -7103736612075250489L;

    @NonNull
    private final Secret token;

    @DataBoundConstructor
    public StandardFortifyApiToken(@CheckForNull CredentialsScope scope, @CheckForNull String id, @NonNull String token, @CheckForNull String description) {
        super(scope, id, description);
        this.token = Secret.fromString(token);
    }

    @NonNull
    @Override
    public Secret getToken() {
        return this.token;
    }

    @Extension
    public static class DefaultFortifyApiTokenDescriptor extends BaseStandardCredentials.BaseStandardCredentialsDescriptor {

        @NonNull
        @Override
        public String getDisplayName() {
            return "Fortify Connection Token";
        }

        @Override
        public String getIconClassName() {
            return "icon-fortify-credentials";
        }

        public FormValidation doCheckToken(@QueryParameter Secret token) {
            if (StringUtils.isBlank(Secret.toString(token))) {
                return FormValidation.error("Token cannot be empty");
            }
            return FormValidation.ok();
        }

        static {
            IconSet.icons.addIcon(new Icon("icon-fortify-credentials icon-sm", "fortify/icons/f-16x16.png",
                    Icon.ICON_SMALL_STYLE, IconType.PLUGIN));
            IconSet.icons.addIcon(new Icon("icon-fortify-credentials icon-md", "fortify/icons/f-24x24.png",
                    Icon.ICON_SMALL_STYLE, IconType.PLUGIN));
            IconSet.icons.addIcon(new Icon("icon-fortify-credentials icon-lg", "fortify/icons/f-32x32.png",
                    Icon.ICON_SMALL_STYLE, IconType.PLUGIN));
            IconSet.icons.addIcon(new Icon("icon-fortify-credentials icon-xlg", "fortify/icons/f-48x48.png",
                    Icon.ICON_SMALL_STYLE, IconType.PLUGIN));
        }
    }
}
