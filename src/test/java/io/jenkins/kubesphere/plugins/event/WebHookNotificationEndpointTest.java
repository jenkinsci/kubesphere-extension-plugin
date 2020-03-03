package io.jenkins.kubesphere.plugins.event;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.jenkins.kubesphere.plugins.event.testutil.Sample;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;



import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;


public class WebHookNotificationEndpointTest {
    private WebHookNotificationEndpoint notificationEndpoint;

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Rule
    public WireMockRule webHook = new WireMockRule(
            WireMockConfiguration.wireMockConfig().port(0)
    );
    @Before
    public void prepareWireMock() throws Exception {
        webHook.stubFor(
                post(urlMatching("/event"))
                        .willReturn(aResponse()
                                .withStatus(200))
        );
    }

    @Before
    public void setUp() {
        notificationEndpoint = new WebHookNotificationEndpoint();
        notificationEndpoint.setTimeout(1); //notification endpoint will timeout after 1 sec
    }

    @After
    public void tearDown() {
        notificationEndpoint = null;
    }

    @Test
    public void expectedNotify() throws Exception {
        notificationEndpoint.setUrl(baseUrl() + "/event");
        notificationEndpoint.notify(Sample.event());
        verify(1, postRequestedFor(urlEqualTo("/event")));
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + webHook.port();
    }

}
