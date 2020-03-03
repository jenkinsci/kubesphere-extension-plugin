/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jenkins.kubesphere.plugins.event;

import hudson.Extension;
import jenkins.util.Timer;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author runzexia
 */
@Extension
public class WebHookNotificationEndpoint extends NotificationEndpoint {

    private static final Logger LOGGER = Logger.getLogger(WebHookNotificationEndpoint.class.getName());

    private String url;

    private long timeout;

    public WebHookNotificationEndpoint() {

    }

    @DataBoundConstructor
    public WebHookNotificationEndpoint(String url, long timeout) {
        this.url = url;
        this.timeout = timeout;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public void notify(KubeSphereNotification.Event event) {
        requestURL(event, url);
    }

    @Override
    public void notify(KubeSphereNotification.Event event, NotificationEndpoint.EndpointEvent endpointEvent) {
        final WebHookEndpointEventCustom custom = (WebHookEndpointEventCustom) endpointEvent.getCustom();
        requestURL(event, custom == null ? this.url : custom.getURL());
    }

    private void requestURL(KubeSphereNotification.Event event, String url) {
        final HttpClient client = HttpClientBuilder.create().build();
        final HttpPost method = new HttpPost(url);
        JSONObject json = JSONObject.fromObject(event);
        try {
            StringEntity entity = new StringEntity(json.toString());
            method.setEntity(entity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Timer.get().schedule(new Runnable() {
            @Override
            public void run() {
                method.abort();
            }
        }, this.timeout, TimeUnit.SECONDS);
        try {
            final HttpResponse response = client.execute(method);
            LOGGER.log(Level.FINE, "{0} status {1}", new Object[]{url, response});
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "communication failure: {0}", e.getMessage());
        } finally {
            method.releaseConnection();
        }
    }

    private Object readResolve() {
        setUrl(url);
        return this;
    }

    @Extension
    public static final class DescriptorImpl extends NotificationEndpoint.DescriptorImpl {
        @Override
        public String getDisplayName() {
            return Messages.WebHookNotificationEndpoint_DisplayName();
        }

        @Override
        protected NotificationEndpoint.EndpointEventCustom parseCustom(JSONObject event) {
            final JSONObject customJSON = ((JSONObject) event).getJSONObject("custom");
            if (!customJSON.isNullObject()) {
                return new WebHookEndpointEventCustom(customJSON.getString("url"));
            }
            return null;
        }

    }

    public static class WebHookEndpointEventCustom implements NotificationEndpoint.EndpointEventCustom {
        private final String url;

        public WebHookEndpointEventCustom(String url) {
            this.url = url;
        }

        public String getURL() {
            return url;
        }
    }
}
