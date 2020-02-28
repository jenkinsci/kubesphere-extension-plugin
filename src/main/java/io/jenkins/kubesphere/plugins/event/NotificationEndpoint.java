package io.jenkins.kubesphere.plugins.event;

import com.google.common.collect.Maps;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

public abstract class NotificationEndpoint extends AbstractDescribableImpl<NotificationEndpoint> implements ExtensionPoint {

    private static final Logger LOGGER = Logger.getLogger(NotificationEndpoint.class.getName());

    public static DescriptorExtensionList<NotificationEndpoint, Descriptor<NotificationEndpoint>> all() {
        LOGGER.warning(String.valueOf(Jenkins.get().getDescriptorList(NotificationEndpoint.class)));
        LOGGER.warning(String.valueOf(Jenkins.get().getDescriptorList(NotificationEndpoint.class).size()));
        return Jenkins.get().getDescriptorList(NotificationEndpoint.class);

    }

    public abstract void notify(Notification.Event event);

    public abstract void notify(Notification.Event event, EndpointEvent endpointEvent);

    private Map<String, EndpointEvent> events = Maps.newHashMap();

    public Map<String, EndpointEvent> getEvents() {
        return events;
    }


    public abstract static class DescriptorImpl extends Descriptor<NotificationEndpoint> {

        public ListBoxModel doFillEndpointsItems() {
            final ListBoxModel listBoxModel = new ListBoxModel();
            for (String endpoint : Notification.ENDPOINTS) {
                listBoxModel.add(endpoint);
            }
            return listBoxModel;
        }

        @Override
        public NotificationEndpoint newInstance(StaplerRequest req, JSONObject formData) throws hudson.model.Descriptor.FormException {
            final NotificationEndpoint instance = super.newInstance(req, formData);

            final JSONObject events = formData.getJSONObject("events");
            if (!events.isNullObject()) {
                final JSONArray eventArray;
                if (events.get("event") instanceof JSONArray) {
                    eventArray = events.getJSONArray("event");
                } else {
                    eventArray = new JSONArray();
                    eventArray.add(events.getJSONObject("event"));
                }
                for (Object event : eventArray) {
                    final String endpoint = ((JSONObject)event).getString("endpoint");
                    final EndpointEventCustom custom = parseCustom(((JSONObject)event));
                    instance.getEvents().put(endpoint, new EndpointEvent(custom));
                }
            }
            return instance;
        }

        protected EndpointEventCustom parseCustom(JSONObject event) {
            return null;
        }

    }

    public interface EndpointEventCustom {

    }
    public static class EndpointEvent {
        private final EndpointEventCustom custom;
        public EndpointEvent(EndpointEventCustom custom) {
            this.custom = custom;
        }
        public EndpointEventCustom getCustom() {
            return custom;
        }
    }
}
