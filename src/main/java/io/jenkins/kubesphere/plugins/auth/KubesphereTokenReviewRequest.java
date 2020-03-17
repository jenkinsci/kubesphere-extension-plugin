package io.jenkins.kubesphere.plugins.auth;

/**
 * @author runzexia
 */
public class KubesphereTokenReviewRequest {

    private String apiVersion;

    private String kind;

    private Spec spec;

    public KubesphereTokenReviewRequest(String token) {
        this.apiVersion = "authentication.k8s.io/v1beta1";
        this.kind = "TokenReview";
        this.spec = new Spec(token);
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public Spec getSpec() {
        return spec;
    }

    public void setSpec(Spec spec) {
        this.spec = spec;
    }

    public static class Spec {
        private String token;

        public Spec(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
