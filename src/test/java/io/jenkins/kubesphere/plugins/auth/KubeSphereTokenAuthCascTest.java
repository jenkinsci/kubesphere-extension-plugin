package io.jenkins.kubesphere.plugins.auth;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import io.jenkins.kubesphere.plugins.event.KubeSphereNotification;
import io.jenkins.kubesphere.plugins.event.WebHookNotificationEndpoint;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

public class KubeSphereTokenAuthCascTest {

    @Rule
    public JenkinsConfiguredWithCodeRule j = new JenkinsConfiguredWithCodeRule();

    @Rule
    public WireMockRule webHook = new WireMockRule(
            WireMockConfiguration.wireMockConfig().port(30123)
    );

    @Before
    public void prepareWireMock() throws Exception {
        webHook.stubFor(
                post(urlMatching("/oauth/authenticate")).
                        withRequestBody(WireMock.containing("testToken"))
                        .willReturn(aResponse().withBody("{\n" +
                                "    \"apiVersion\": \"authentication.k8s.io/v1beta1\",\n" +
                                "    \"kind\": \"TokenReview\",\n" +
                                "    \"status\": {\n" +
                                "        \"authenticated\": true,\n" +
                                "        \"user\": {\n" +
                                "            \"uid\": \"admin\",\n" +
                                "            \"username\": \"admin\"\n" +
                                "        }\n" +
                                "    }\n" +
                                "}")
                                .withStatus(200))
        );

    }

    @Test
    @ConfiguredWithCode("casc.yaml")
    public void expectedCASC() throws Exception {
        KubesphereTokenAuthGlobalConfiguration config = GlobalConfiguration.all().
                get(KubesphereTokenAuthGlobalConfiguration.class);


        Assert.assertEquals(config.getCacheConfiguration().getSize(), 20);
        Assert.assertEquals(config.getCacheConfiguration().getTtl(), 300);
        Assert.assertEquals(config.isEnabled(), true);
        Assert.assertEquals(config.getServer(), "http://127.0.0.1:30123/");
        Assert.assertEquals(config.getServerUrl(),"http://127.0.0.1:30123/");
        Assert.assertEquals(config.getRequestUrl(),"http://127.0.0.1:30123/oauth/authenticate");
    }

    @Test
    @ConfiguredWithCode("casc.yaml")
    public void expectedUserCouldAccessAPI() throws Exception{
        HttpResponse<String> response = Unirest.get(j.jenkins.getRootUrl()+"api/json").
                header("Accept", "*/*")
                .header("Accept-Encoding","")
                .header("Authorization", "Basic YWRtaW46dGVzdFRva2Vu")
                .asString();
        Assert.assertEquals(200, response.getStatus());
        response = Unirest.get(j.jenkins.getRootUrl()+"api/json").
                header("Accept", "*/*")
                .header("Accept-Encoding","")
                .header("Authorization", "Basic YWRtaW46YWFhYWE=")
                .asString();
        Assert.assertNotEquals(200, response.getStatus());
    }

}
