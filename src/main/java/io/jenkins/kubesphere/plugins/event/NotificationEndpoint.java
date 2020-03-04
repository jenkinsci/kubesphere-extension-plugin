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
import java.util.logging.Logger;

public abstract class NotificationEndpoint extends AbstractDescribableImpl<NotificationEndpoint> implements ExtensionPoint {

    private static final Logger LOGGER = Logger.getLogger(NotificationEndpoint.class.getName());
    private Map<String, EndpointEvent> events = Maps.newHashMap();

    public static DescriptorExtensionList<NotificationEndpoint, Descriptor<NotificationEndpoint>> all() {
        return Jenkins.get().getDescriptorList(NotificationEndpoint.class);
    }

    public abstract void notify(KubeSphereNotification.Event event);

    public abstract void notify(KubeSphereNotification.Event event, EndpointEvent endpointEvent);

    public Map<String, EndpointEvent> getEvents() {
        return events;
    }


    public interface EndpointEventCustom {

    }

    public abstract static class DescriptorImpl extends Descriptor<NotificationEndpoint> {

        public ListBoxModel doFillEndpointsItems() {
            final ListBoxModel listBoxModel = new ListBoxModel();
            for (String endpoint : KubeSphereNotification.ENDPOINTS) {
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
                    final String endpoint = ((JSONObject) event).getString("endpoint");
                    final EndpointEventCustom custom = parseCustom(((JSONObject) event));
                    instance.getEvents().put(endpoint, new EndpointEvent(custom));
                }
            }
            return instance;
        }

        protected EndpointEventCustom parseCustom(JSONObject event) {
            return null;
        }

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
