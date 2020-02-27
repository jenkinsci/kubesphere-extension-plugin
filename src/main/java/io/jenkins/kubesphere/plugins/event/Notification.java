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
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static hudson.init.InitMilestone.PLUGINS_STARTED;

@Extension
@Symbol("kubesphere-event-notifacation")
public class Notification implements Describable<Notification> {

    static final String JENKINS_JOB_STARTED = "jenkins.job.started";

    static final String JENKINS_JOB_COMPLETED = "jenkins.job.completed";

    static final String JENKINS_JOB_FINALIZED = "jenkins.job.finalized";

    static final String[] ENDPOINTS = new String[] {
            JENKINS_JOB_STARTED,
            JENKINS_JOB_COMPLETED,
            JENKINS_JOB_FINALIZED,
    };

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Objects.requireNonNull(Jenkins.getInstanceOrNull()).getDescriptorOrDie(getClass());
    }

    private static Notification instance;

    @Initializer(after = PLUGINS_STARTED)
    public static void init() {
        instance = new Notification();
    }

    public static void notify(Event event) {
        if (instance != null) {
            instance._notify(event);
        }
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
            } else if (endpoint.getEvents().containsKey(event.getName())) {
                final NotificationEndpoint.EndpointEvent endpointEvent = endpoint.getEvents().get(event.getName());
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
    public static final class DescriptorImpl extends Descriptor<Notification> {
        private DescribableList<NotificationEndpoint, Descriptor<NotificationEndpoint>> endpoints = new DescribableList<NotificationEndpoint, Descriptor<NotificationEndpoint>>(this);

        public DescribableList<NotificationEndpoint, Descriptor<NotificationEndpoint>> getEndpoints() {
            return endpoints;
        }

        public DescriptorImpl() {
            super(Notification.class);
            load();
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
            return "KubeSphere Notifacation";
        }
    }

    public static final class Event {

        private final Long timestamp;

        private final String type;

        private final Map<String, Object> args = Maps.newHashMap();

        public Event(String type, Object... args) {
            this.timestamp = System.currentTimeMillis();
            this.type = type;
            for (int i = 0; i < args.length; i+=2) {
                this.args.put((String) args[i], args[i+1]);
            }
        }

        public Long getTimestamp() {
            return timestamp;
        }

        @Whitelisted
        public String getName() {
            return type;
        }

        @Whitelisted
        public Map<String, Object> getArgs() {
            return args;
        }

    }
}
