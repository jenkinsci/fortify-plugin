package com.fortify.plugin.jenkins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jvnet.hudson.test.RestartableJenkinsRule;

import hudson.util.Secret;
import io.jenkins.plugins.casc.misc.RoundTripAbstractTest;

public class FortifyJCasCCompatibilityTest extends RoundTripAbstractTest {

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
	@Override
	protected void assertConfiguredAsExpected(RestartableJenkinsRule restartableJenkinsRule, String s) {
		FortifyPlugin.DescriptorImpl descriptor = FortifyPlugin.DESCRIPTOR;
		assertTrue("Fortify plugin can not be found", descriptor != null);
		assertEquals(String.format("Wrong SSC URL. Expected %s but received %s", "https://qa-plg-ssc3.prgqa.hpecorp.net:8443/ssc", descriptor.getUrl()),
				"https://qa-plg-ssc3.prgqa.hpecorp.net:8443/ssc", descriptor.getUrl());
		assertEquals(String.format("Wrong SSC token. Expected %s but received %s", "3ab8c774-0850-483b-8be6-2907722a81d8",
				descriptor.getToken()), "3ab8c774-0850-483b-8be6-2907722a81d8", descriptor.getToken());
		assertEquals(String.format("Wrong Application Template. Expected %s but received %s", "Prioritized High Risk Issue Template",
				descriptor.getProjectTemplate()), "Prioritized High Risk Issue Template", descriptor.getProjectTemplate());
		assertEquals(String.format("Wrong Connect timeout. Expected %s but received %s", Integer.valueOf(10),
				descriptor.getConnectTimeout()), Integer.valueOf(10), descriptor.getConnectTimeout());
		assertEquals(String.format("Wrong page size. Expected %s but received %s", Integer.valueOf(40), descriptor.getBreakdownPageSize()),
				Integer.valueOf(40), descriptor.getBreakdownPageSize());
		assertTrue("Proxy should be used", descriptor.getUseProxy());
		assertEquals(String.format("Wrong proxy authentication. Username expected %s but received %s", Secret.fromString("fakeuser"),
				descriptor.getProxyConfig().getProxyUsername()), Secret.fromString("fakeuser"), descriptor.getProxyConfig().getProxyUsername());
		assertEquals(String.format("Wrong proxy authentication. Username expected %s but received %s", "fakeuser",
				descriptor.getProxyUsername()), "fakeuser", descriptor.getProxyUsername());
		assertEquals(String.format("Local scan setting is incorrect. Expected %s but received %s", true, descriptor.isPreventLocalScans()),
				true, descriptor.isPreventLocalScans());
	}

	@Override
	protected String stringInLogExpected() {
		return "";
	}

}
