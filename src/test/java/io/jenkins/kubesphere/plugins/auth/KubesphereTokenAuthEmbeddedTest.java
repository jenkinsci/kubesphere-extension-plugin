package io.jenkins.kubesphere.plugins.auth;

import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class KubesphereTokenAuthEmbeddedTest {

    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test
    public void validateTest() throws Exception{
        KubesphereTokenAuthGlobalConfiguration configuration = new KubesphereTokenAuthGlobalConfiguration(false,"",null);

        FormValidation validation  = configuration.doVerifyConnect("abcdefgaaaaaa");
        assertThat(validation.getMessage(),startsWith("Connect error"));

        FormValidation validation1 = configuration.doVerifyConnect("https://api.github.com/");
        assertThat(validation1.getMessage(),startsWith("Response format error"));
    }

    @Test
    public void serverToUrlTest() throws Exception{
        String url = KubesphereTokenAuthGlobalConfiguration.serverToUrl("api.github.com");
        assertEquals(url,"http://api.github.com/");
    }

    @Test
    public void getReviewResponseFromCache() throws Exception{
        KubesphereTokenAuthGlobalConfiguration config = GlobalConfiguration.all().get(KubesphereTokenAuthGlobalConfiguration.class);

        config.setServer("mock");
        config.setCacheConfiguration(new KubesphereTokenAuthGlobalConfiguration.CacheConfiguration(20,60));
        config.setTokenAuthCache(new KubesphereApiTokenAuthenticator.CacheMap<>(
                config.getCacheConfiguration().getSize()));
        config.getTokenAuthCache().put("admin",new KubesphereApiTokenAuthenticator.CacheEntry<KubesphereTokenReviewResponse>(config.getCacheConfiguration().getTtl(),
                new KubesphereTokenReviewResponse(JSONObject.fromObject("{\n" +
                "    \"apiVersion\": \"authentication.k8s.io/v1beta1\",\n" +
                "    \"kind\": \"TokenReview\",\n" +
                "    \"status\": {\n" +
                "        \"authenticated\": true,\n" +
                "        \"user\": {\n" +
                "            \"uid\": \"admin\",\n" +
                "            \"username\": \"admin\"\n" +
                "        }\n" +
                "    }\n" +
                "}"),"mock")));

        KubesphereTokenReviewResponse reviewResponse = KubesphereApiTokenAuthenticator.getReviewResponse("admin","mock");
        assertEquals(JSONObject.fromObject(reviewResponse), JSONObject.fromObject(new KubesphereTokenReviewResponse(JSONObject.fromObject("{\n" +
                "    \"apiVersion\": \"authentication.k8s.io/v1beta1\",\n" +
                "    \"kind\": \"TokenReview\",\n" +
                "    \"status\": {\n" +
                "        \"authenticated\": true,\n" +
                "        \"user\": {\n" +
                "            \"uid\": \"admin\",\n" +
                "            \"username\": \"admin\"\n" +
                "        }\n" +
                "    }\n" +
                "}"),"mock")));
    }

}
