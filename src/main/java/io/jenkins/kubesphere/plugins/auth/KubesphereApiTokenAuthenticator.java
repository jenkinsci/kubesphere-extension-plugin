package io.jenkins.kubesphere.plugins.auth;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import hudson.Extension;
import hudson.model.User;
import jenkins.security.BasicHeaderApiTokenAuthenticator;
import jenkins.security.BasicHeaderAuthenticator;
import jenkins.security.SecurityListener;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.acegisecurity.Authentication;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

/**
 * @author runzexia
 */
@Extension
public class KubesphereApiTokenAuthenticator extends BasicHeaderAuthenticator {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private static final Logger LOGGER = Logger.getLogger(BasicHeaderApiTokenAuthenticator.class.getName());

    public static KubesphereTokenReviewResponse getReviewResponse(String username, String token) throws IOException {
        KubesphereTokenAuthGlobalConfiguration authConfig = KubesphereTokenAuthGlobalConfiguration.get();
        if (authConfig.getCacheConfiguration() != null) {
            synchronized (authConfig) {
                CacheMap<String, KubesphereTokenReviewResponse> tokenCache = authConfig.getTokenAuthCache();

                KubesphereTokenAuthGlobalConfiguration.CacheConfiguration
                        cacheConfig = authConfig.getCacheConfiguration();
                if (tokenCache == null) {
                    authConfig.setTokenAuthCache(
                            new CacheMap<>(cacheConfig.getSize()));
                } else {
                    if (tokenCache.getCacheSize()
                            != cacheConfig.getSize()) {
                        (tokenCache).setCacheSize(
                                authConfig.getCacheConfiguration().getSize());
                    }
                    final CacheEntry<KubesphereTokenReviewResponse> cached = tokenCache.get(username);

                    if (cached != null && cached.isValid() && cached.getValue().getToken().equals(token)) {
                        return cached.getValue();
                    }
                }
            }
        }
        KubesphereTokenReviewResponse reviewResponse = getReviewResponseFromApiServer(
                KubesphereTokenAuthGlobalConfiguration.get().getRequestUrl(), token);
        if (authConfig.getCacheConfiguration() != null &&
                reviewResponse != null &&
                reviewResponse.getStatus().getAuthenticated() &&
                (reviewResponse.getStatus().getUser().getUsername().equals(username))) {
            synchronized (authConfig) {
                CacheMap tokenCache = authConfig.getTokenAuthCache();
                if (tokenCache.containsKey(username)) {
                    tokenCache.replace(username, new CacheEntry<>(
                            authConfig.getCacheConfiguration().getTtl(), reviewResponse
                    ));
                } else {
                    tokenCache.put(username, new CacheEntry<>(
                            authConfig.getCacheConfiguration().getTtl(), reviewResponse
                    ));
                }
            }
        }
        return reviewResponse;
    }

    public static KubesphereTokenReviewResponse getReviewResponseFromApiServer(String requestUrl, String token) throws IOException {

        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(30, TimeUnit.SECONDS);
        client.setReadTimeout(60, TimeUnit.SECONDS);
        Request.Builder builder = new Request.Builder();
        builder.url(requestUrl);
        KubesphereTokenReviewRequest reviewRequest = new KubesphereTokenReviewRequest(token);
        builder.post(RequestBody.create(JSON, JSONObject.fromObject(reviewRequest).toString()));
        Response response = client.newCall(builder.build()).execute();
        try {
            JSONObject responseObject = JSONObject.fromObject(response.body().string());
            KubesphereTokenReviewResponse reviewResponse = new KubesphereTokenReviewResponse(responseObject, token);
            return reviewResponse;
        }catch (JSONException e){
            LOGGER.warning("Get illegal JSON" + e.getMessage());
            return null;
        }
    }

    @Override
    public Authentication authenticate(HttpServletRequest req, HttpServletResponse rsp, String username, String password) throws ServletException {
        // attempt to authenticate as API token
        User u = User.getById(username, true);
        if (!KubesphereTokenAuthGlobalConfiguration.get().isEnabled()) {
            return null;
        }
        if (u == null) {
            return null;
        }
        try {
            KubesphereTokenReviewResponse reviewResponse = getReviewResponse(username, password);
            if (reviewResponse != null && reviewResponse.getStatus().getAuthenticated() && reviewResponse.getStatus().getUser().getUsername().equals(username)) {
                Authentication auth;
                try {
                    UserDetails userDetails = u.getUserDetailsForImpersonation();
                    auth = new UsernamePasswordAuthenticationToken(userDetails.getUsername(), "", userDetails.getAuthorities());

                    SecurityListener.fireAuthenticated(userDetails);

                } catch (UsernameNotFoundException x) {
                    // The token was valid, but the impersonation failed. This token is clearly not his real password,
                    // so there's no point in continuing the request processing. Report this error and abort.
                    LOGGER.log(WARNING, "API token matched for user " + username + " but the impersonation failed", x);
                    throw new ServletException(x);
                } catch (DataAccessException x) {
                    throw new ServletException(x);
                }
                return auth;
            }

        } catch (IOException e) {
            return null;
        }
        return null;
    }

    public static class CacheEntry<T> {
        private final long expires;
        private final T value;

        public CacheEntry(int ttlSeconds, T value) {
            this.expires = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(ttlSeconds);
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public boolean isValid() {
            return System.currentTimeMillis() < expires;
        }
    }

    /**
     * While we could use Guava's CacheBuilder the method signature changes make using it problematic.
     * Safer to roll our own and ensure compatibility across as wide a range of Jenkins versions as possible.
     *
     * @param <K> Key type
     * @param <V> Cache entry type
     */
    public static class CacheMap<K, V> extends LinkedHashMap<K, CacheEntry<V>> {

        private int cacheSize;

        public CacheMap(int cacheSize) {
            super(cacheSize + 1); // prevent realloc when hitting cacheConfiguration size limit
            this.cacheSize = cacheSize;
        }

        public int getCacheSize() {
            return this.cacheSize;
        }

        public void setCacheSize(int cacheSize) {
            this.cacheSize = cacheSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, CacheEntry<V>> eldest) {
            return size() > cacheSize || eldest.getValue() == null || !eldest.getValue().isValid();
        }
    }
}


