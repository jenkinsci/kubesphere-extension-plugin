package io.jenkins.kubesphere.plugins.event;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import jenkins.model.Jenkins;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

public class WebHookEventTest {

    @Rule
    public JenkinsConfiguredWithCodeRule j = new JenkinsConfiguredWithCodeRule();

    @Rule
    public WireMockRule webHook = new WireMockRule(
            WireMockConfiguration.wireMockConfig().port(30123)
    );

    @Before
    public void prepareWireMock() throws Exception {
        webHook.stubFor(
                post(urlMatching("/event"))
                        .willReturn(aResponse()
                                .withStatus(200))
        );

        webHook.stubFor(
                post(urlMatching("/delayed"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withFixedDelay(2000)) // 2 seconds delay
        );
    }


    @Test
    @ConfiguredWithCode("casc.yaml")
    public void expectedCASC() throws Exception {
        KubeSphereNotification.DescriptorImpl descriptor = Jenkins.get().getExtensionList(KubeSphereNotification.class).get(0).getDescriptor();
        Assert.assertEquals(descriptor.getEndpoints().size(),1);
        Assert.assertEquals(((WebHookNotificationEndpoint)descriptor.getEndpoints().get(0)).getUrl(), baseUrl());
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + webHook.port();
    }
}
