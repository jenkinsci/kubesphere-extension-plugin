package io.jenkins.kubesphere.plugins.auth;

import net.sf.json.JSONObject;

public class KubesphereTokenReviewResponse {
    private String apiVersion;

    private String kind;

    private TokenStatus status;

    private String token;

    public KubesphereTokenReviewResponse() {

    }

    public KubesphereTokenReviewResponse(JSONObject jsonObject, String token) {
        KubesphereTokenReviewResponse bean =
                (KubesphereTokenReviewResponse) JSONObject.toBean(jsonObject, KubesphereTokenReviewResponse.class);
        this.setToken(token);
        this.setApiVersion(bean.getApiVersion());
        this.setKind(bean.getKind());
        this.setStatus(bean.getStatus());
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public TokenStatus getStatus() {
        return status;
    }

    public void setStatus(TokenStatus status) {
        this.status = status;
    }

    public static class TokenStatus {

        private UserInfo user;

        private Boolean authenticated;

        public UserInfo getUser() {
            return user;
        }

        public void setUser(UserInfo user) {
            this.user = user;
        }

        public Boolean getAuthenticated() {
            return authenticated;
        }

        public void setAuthenticated(Boolean authenticated) {
            this.authenticated = authenticated;
        }

        public static class UserInfo {
            private String username;

            private String uid;

            public String getUid() {
                return uid;
            }

            public void setUid(String uid) {
                this.uid = uid;
            }

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }
        }
    }
}
