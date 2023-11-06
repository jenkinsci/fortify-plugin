package com.fortify.plugin.jenkins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import org.junit.Rule;
import org.junit.Test;

import hudson.ProxyConfiguration;
import hudson.util.Secret;
import jenkins.model.Jenkins;

public class FortifyJCasCCompatibilityTest {

	@Rule
	public final JenkinsConfiguredWithCodeRule jenkinsConfiguredWithRule = new JenkinsConfiguredWithCodeRule();

	/*
	 * Resource: /com.fortify.plugin.jenkins/src/test/resources/com/fortify/plugin/jenkins/configuration-as-code.yaml
	 * 
	 * unclassified:
	 *   fortify:
	 *     url: "https://qa-plg-ssc3.prgqa.hpecorp.net:8443/ssc"
	 *     token: "3ab8c774-0850-483b-8be6-2907722a81d8"
	 *     proxyConfig:
	 *       proxyUrl: "web-proxy.us.softwaregrp.net:8080"
	 *       proxyUsername: "fakeuser"
	 *     projectTemplate: "Prioritized High Risk Issue Template"
	 *     connectTimeout: "10"
	 *     readTimeout: "20"
	 *     writeTimeout: "30"
	 *     breakdownPageSize: "40"
	 *     ctrlUrl: "https://qa-cs-r-ctrl.prgqa.hpecorp.net:8443/scancentral-ctrl/"
	 *     ctrlToken: "iamclient!"
	 */
	@Test
	@ConfiguredWithCode("com/fortify/plugin/jenkins/configuration-as-code.yml")
	public void assertConfiguredAsExpected() {
		final FortifyPlugin.DescriptorImpl descriptor = (FortifyPlugin.DescriptorImpl) jenkinsConfiguredWithRule.jenkins.getDescriptorOrDie(FortifyPlugin.class);
		assertTrue("Fortify plugin can not be found", descriptor != null);
		assertEquals(String.format("Wrong SSC URL. Expected %s but received %s", "https://qa-plg-ssc3.prgqa.hpecorp.net:8443/ssc", descriptor.getUrl()),
				"https://qa-plg-ssc3.prgqa.hpecorp.net:8443/ssc", descriptor.getUrl());
		assertEquals(String.format("Wrong SSC token. Expected %s but received %s", "3ab8c774-0850-483b-8be6-2907722a81d8",
				descriptor.getToken()), "3ab8c774-0850-483b-8be6-2907722a81d8", descriptor.getToken());
		assertEquals(String.format("Wrong SSC token credentials. Expected %s but received %s", "fortify_api_token",
				descriptor.getSscTokenCredentialsId()), "fortify_api_token", descriptor.getSscTokenCredentialsId());
		assertEquals(String.format("Wrong Application Template. Expected %s but received %s", "Prioritized High Risk Issue Template",
				descriptor.getProjectTemplate()), "Prioritized High Risk Issue Template", descriptor.getProjectTemplate());
		assertEquals(String.format("Wrong Connect timeout. Expected %s but received %s", Integer.valueOf(10),
				descriptor.getConnectTimeout()), Integer.valueOf(10), descriptor.getConnectTimeout());
		assertEquals(String.format("Wrong page size. Expected %s but received %s", Integer.valueOf(40), descriptor.getBreakdownPageSize()),
				Integer.valueOf(40), descriptor.getBreakdownPageSize());
		assertEquals(String.format("Wrong app version list limit. Expected %s but received %s", Integer.valueOf(80), descriptor.getAppVersionListLimit()),
				Integer.valueOf(80), descriptor.getAppVersionListLimit());
		assertTrue("Proxy should be used", descriptor.getIsProxy());
		assertEquals(String.format("Wrong Fortify proxy configuration. Username expected %s but received %s", "fakeuser",
				Secret.toString(descriptor.getProxyConfig().getProxyUsername())), "fakeuser", Secret.toString(descriptor.getProxyConfig().getProxyUsername()));
		ProxyConfiguration proxy = Jenkins.getInstanceOrNull().proxy;
		assertNotNull("Failed to configure Jenkins proxy upon migration", proxy);
		assertEquals(String.format("Wrong Jenkins proxy configuration. Username expected %s but received %s", "fakeuser",
				proxy.getUserName()), "fakeuser", proxy.getUserName());
		assertEquals(String.format("Local scan setting is incorrect. Expected %s but received %s", true, descriptor.isDisableLocalScans()),
				true, descriptor.isDisableLocalScans());
	}
}
