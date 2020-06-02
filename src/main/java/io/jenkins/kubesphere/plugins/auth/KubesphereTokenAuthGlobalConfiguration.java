package io.jenkins.kubesphere.plugins.auth;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.kubesphere.plugins.auth.KubesphereApiTokenAuthenticator.CacheMap;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.logging.Logger;

@Extension
public class KubesphereTokenAuthGlobalConfiguration extends GlobalConfiguration {
    private static final Logger LOGGER = Logger.getLogger(KubesphereTokenAuthGlobalConfiguration.class.getName());

    private boolean enabled = false;

    private String server;

    private CacheConfiguration cacheConfiguration;

    private transient CacheMap<String, KubesphereTokenReviewResponse> tokenAuthCache = null;

    public KubesphereTokenAuthGlobalConfiguration() {
        this.load();
    }

    /**
     * @deprecated only for test
     */
    @Deprecated
    public KubesphereTokenAuthGlobalConfiguration(boolean enabled, String server, CacheConfiguration cacheConfiguration) {
        this.enabled = enabled;
        this.server = server;
        this.cacheConfiguration = cacheConfiguration;
    }


    public static KubesphereTokenAuthGlobalConfiguration get() {
        return GlobalConfiguration.all().get(KubesphereTokenAuthGlobalConfiguration.class);
    }

    public static String serverToUrl(String server) {
        String serverUrl = server.trim();
        if (!serverUrl.contains("://")) {
            serverUrl = "http://" + serverUrl;
        }
        if (!serverUrl.endsWith("/")) {
            serverUrl += "/";
        }
        return serverUrl;
    }

    public CacheMap<String, KubesphereTokenReviewResponse> getTokenAuthCache() {
        return tokenAuthCache;
    }

    public void setTokenAuthCache(CacheMap<String, KubesphereTokenReviewResponse> tokenAuthCache) {
        this.tokenAuthCache = tokenAuthCache;
    }

    public FormValidation doVerifyConnect(@QueryParameter String server) {
        try {
            KubesphereTokenReviewResponse reviewResponse = KubesphereApiTokenAuthenticator.
                    getReviewResponseFromApiServer(serverToUrl(server), "mock");
            if (reviewResponse.getKind() == null) {
                return FormValidation.error("Response format error");
            }
            if (reviewResponse.getKind().equals("TokenReview")) {
                return FormValidation.ok(String.format("Connect to %s success.", server));
            }
        } catch (IOException e) {
            return FormValidation.error(e, "Connect error");
        }
        return FormValidation.error(String.format("Connect to %s , response format error.", server));
    }

    @Override
    @Nonnull
    public String getDisplayName() {
        return "Kubesphere Jenkins Token Auth";
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) {
        req.bindJSON(this, json);
        if (json.get("cacheConfiguration") == null) {
            this.cacheConfiguration = null;
        }
        this.save();
        return true;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    @DataBoundSetter
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CacheConfiguration getCacheConfiguration() {
        return this.cacheConfiguration;
    }

    @DataBoundSetter
    public void setCacheConfiguration(CacheConfiguration cacheConfiguration) {
        this.cacheConfiguration = cacheConfiguration;
    }

    public String getServer() {
        return this.server;
    }

    @DataBoundSetter
    public void setServer(String server) {
        this.server = server;
    }

    public String getServerUrl() {
        return serverToUrl(this.server);
    }

    public String getRequestUrl() {
        return serverToUrl(this.server) + "oauth/authenticate";
    }


    public static class CacheConfiguration extends AbstractDescribableImpl<CacheConfiguration> {
        private final int size;
        private final int ttl;

        @DataBoundConstructor
        public CacheConfiguration(int size, int ttl) {
            this.size = Math.max(10, Math.min(size, 1000));
            this.ttl = Math.max(30, Math.min(ttl, 3600));
        }

        public int getSize() {
            return size;
        }

        public int getTtl() {
            return ttl;
        }

        @Extension
        public static class DescriptorImpl extends Descriptor<CacheConfiguration> {

            @Override
            public String getDisplayName() {
                return "";
            }

            public ListBoxModel doFillSizeItems() {
                ListBoxModel m = new ListBoxModel();
                m.add("10");
                m.add("20");
                m.add("50");
                m.add("100");
                m.add("200");
                m.add("500");
                m.add("1000");
                return m;
            }

            public ListBoxModel doFillTtlItems() {
                ListBoxModel m = new ListBoxModel();
                for (int ttl : new int[]{30, 60, 120, 300, 600, 900, 1800, 3600}) {
                    m.add(Util.getTimeSpanString(ttl * 1000L), Integer.toString(ttl));
                }
                return m;
            }

        }
    }
}
