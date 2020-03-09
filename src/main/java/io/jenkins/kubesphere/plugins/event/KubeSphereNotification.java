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
import hudson.Extension;
import hudson.init.Initializer;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
import jenkins.util.Timer;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import static hudson.init.InitMilestone.PLUGINS_STARTED;

/**
 * @author runzexia
 */
@Extension
public class KubeSphereNotification implements Describable<KubeSphereNotification> {

    static final String JENKINS_JOB_STARTED = "jenkins.job.started";
    static final String JENKINS_JOB_COMPLETED = "jenkins.job.completed";
    static final String JENKINS_JOB_FINALIZED = "jenkins.job.finalized";
    static final String[] ENDPOINTS = new String[]{
            JENKINS_JOB_STARTED,
            JENKINS_JOB_COMPLETED,
            JENKINS_JOB_FINALIZED,
    };
    private static final Logger LOGGER = Logger.getLogger(KubeSphereNotification.class.getName());
    private static KubeSphereNotification instance;

    @Initializer(after = PLUGINS_STARTED)
    public static void init() {
        instance = Jenkins.get().getExtensionList(KubeSphereNotification.class).get(0);
    }

    public static void notify(Event event) {

        if (instance != null) {
            instance._notify(event);
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Jenkins.get().getDescriptorOrDie(getClass());
    }

    private void _notify(final Event event) {
        for (final NotificationEndpoint endpoint : getDescriptor().getEndpoints()) {
            if (endpoint.getEvents().isEmpty()) {
                start(new Runnable() {
                    @Override
                    public void run() {
                        endpoint.notify(event);
                    }
                });
            } else if (endpoint.getEvents().containsKey(event.getType())) {
                final NotificationEndpoint.EndpointEvent endpointEvent = endpoint.getEvents().get(event.getType());
                start(new Runnable() {
                    @Override
                    public void run() {
                        endpoint.notify(event, endpointEvent);
                    }
                });
            }
        }
    }

    private void start(Runnable runnable) {
        Timer.get().submit(runnable);
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<KubeSphereNotification> {
        private DescribableList<NotificationEndpoint, Descriptor<NotificationEndpoint>> endpoints = new DescribableList<NotificationEndpoint, Descriptor<NotificationEndpoint>>(this);

        public DescriptorImpl() {
            super(KubeSphereNotification.class);
            load();
        }

        public DescribableList<NotificationEndpoint, Descriptor<NotificationEndpoint>> getEndpoints() {
            return endpoints;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            try {
                endpoints.rebuildHetero(req, formData, NotificationEndpoint.all(), "endpoints");
                save();
            } catch (IOException e) {
                throw new FormException(e, "endpoints");
            }
            return true;
        }

        @Override
        public String getDisplayName() {
            return "KubeSphere Notification";
        }
    }

    public static final class Event {

        private Long timestamp;

        private String type;

        private Map<String, Object> args;

        public Event(String type, Object... args) {
            this.timestamp = System.currentTimeMillis();
            this.type = type;
            this.args = Maps.newHashMap();
            for (int i = 0; i < args.length; i += 2) {
                this.args.put((String) args[i], args[i + 1]);
            }
        }
        public Event(){}

        public Long getTimestamp() {
            return timestamp;
        }

        @Whitelisted
        public String getType() {
            return type;
        }

        @Whitelisted
        public Map<String, Object> getArgs() {
            return args;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setArgs(Map<String, Object> args) {
            this.args = args;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
