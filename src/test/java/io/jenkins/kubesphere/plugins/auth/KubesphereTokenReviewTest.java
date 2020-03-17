package io.jenkins.kubesphere.plugins.auth;

import net.sf.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KubesphereTokenReviewTest {

    @Test
    public void newReviewRequestTest() throws Exception{
        KubesphereTokenReviewRequest reviewRequest = new KubesphereTokenReviewRequest("testToken");

        assertEquals(reviewRequest.getApiVersion(),"authentication.k8s.io/v1beta1");
        assertEquals(reviewRequest.getKind(),"TokenReview");
        assertEquals(reviewRequest.getSpec().getToken(),"testToken");
        assertEquals(JSONObject.fromObject(reviewRequest),JSONObject.fromObject("{\n" +
                "  \"apiVersion\": \"authentication.k8s.io/v1beta1\",\n" +
                "  \"kind\": \"TokenReview\",\n" +
                "  \"spec\": {\n" +
                "    \"token\": \"testToken\"\n" +
                "  }\n" +
                "}"));

    }

    @Test
    public void newReviewResponseTest() throws Exception{
        JSONObject jsonObject1 = JSONObject.fromObject("{\n" +
                "    \"apiVersion\": \"authentication.k8s.io/v1beta1\",\n" +
                "    \"kind\": \"TokenReview\",\n" +
                "    \"status\": {\n" +
                "        \"authenticated\": true,\n" +
                "        \"user\": {\n" +
                "            \"uid\": \"admin\",\n" +
                "            \"username\": \"admin\"\n" +
                "        }\n" +
                "    }\n" +
                "}");

        JSONObject jsonObject2 = JSONObject.fromObject("{\n" +
                "    \"apiVersion\":\"authentication.k8s.io/v1beta1\",\n" +
                "    \"kind\":\"TokenReview\",\n" +
                "    \"status\":{\n" +
                "        \"authenticated\":true,\n" +
                "        \"user\":{\n" +
                "            \"uid\":\"admin\",\n" +
                "            \"username\":\"admin\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"token\":\"testToken\"\n" +
                "}");
        KubesphereTokenReviewResponse response = new KubesphereTokenReviewResponse(jsonObject1,"testToken");

        assertEquals(response.getKind(),"TokenReview");
        assertEquals(response.getApiVersion(),"authentication.k8s.io/v1beta1");
        assertEquals(response.getToken(),"testToken");
        assertEquals(response.getStatus().getAuthenticated(),true);
        assertEquals(response.getStatus().getUser().getUsername(),"admin");
        assertEquals(response.getStatus().getUser().getUid(),"admin");
        assertEquals(JSONObject.fromObject(response),jsonObject2);
    }
}
