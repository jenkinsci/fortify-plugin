package com.fortify.plugin.jenkins.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.Extension;
import hudson.util.Secret;
import lombok.NonNull;

import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jenkins.ui.icon.IconType;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;

/**
 * Default implementation of {@link FortifyApiToken} for use by Jenkins {@link com.cloudbees.plugins.credentials.CredentialsProvider}
 * instances that store {@link Secret} locally.
 */
public class DefaultFortifyApiToken extends BaseStandardCredentials implements FortifyApiToken {

	private static final long serialVersionUID = -7103736612075250489L;

    @NonNull
    private final Secret token;

    @DataBoundConstructor
    public DefaultFortifyApiToken(@CheckForNull CredentialsScope scope, @CheckForNull String id, @NonNull String token, @CheckForNull String description) {
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
