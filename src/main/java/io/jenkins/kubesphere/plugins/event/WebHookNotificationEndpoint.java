package io.jenkins.kubesphere.plugins.event;

import hudson.Extension;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

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
    public void notify(Notification.Event event) {
        requestURL(event, url);
    }

    @Override
    public void notify(Notification.Event event, NotificationEndpoint.EndpointEvent endpointEvent) {
        final WebHookEndpointEventCustom custom = (WebHookEndpointEventCustom) endpointEvent.getCustom();
        requestURL(event, custom == null ? this.url : custom.getURL());
    }
    private void requestURL(Notification.Event event, String url) {

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
            final JSONObject customJSON = ((JSONObject)event).getJSONObject("custom");
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
