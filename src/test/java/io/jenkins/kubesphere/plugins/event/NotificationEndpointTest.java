package io.jenkins.kubesphere.plugins.event;

import io.jenkins.kubesphere.plugins.event.testutil.Sample;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NotificationEndpointTest {
    NotificationEndpoint notificationEndpoint;
    KubeSphereNotification.Event event;
    Map<String, Object> extraArgs;

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() throws Exception {
        notificationEndpoint = new NotificationEndpoint() {
            @Override
            public void notify(KubeSphereNotification.Event event) {
            }

            @Override
            public void notify(KubeSphereNotification.Event event, EndpointEvent endpointEvent) {
            }
        };

        event = Sample.event();
        extraArgs = Sample.extraArgs();
    }

    @After
    public void tearDown() throws Exception {
        event = null;
        notificationEndpoint = null;
    }

    @Test
    public void testInterpolate() {
        String interpolated;

        interpolated = notificationEndpoint.interpolate("value", event);
        assertTrue(interpolated.length() > 0);

        interpolated = notificationEndpoint.interpolate("event ${type} (argFoo: ${argFoo})", event);
        assertEquals("event testEvent (argFoo: foo)", interpolated);

        interpolated = notificationEndpoint.interpolate("event ${type} (argBar: ${argBar}) is now ${what}", event, extraArgs);
        assertEquals("event testEvent (argBar: bar) is now a simple string", interpolated);
    }
}
