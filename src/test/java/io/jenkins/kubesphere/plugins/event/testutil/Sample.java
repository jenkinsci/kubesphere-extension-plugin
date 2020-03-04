package io.jenkins.kubesphere.plugins.event.testutil;

import io.jenkins.kubesphere.plugins.event.KubeSphereNotification;

import java.util.Hashtable;
import java.util.Map;

/**
 * Created by daniel.burgmann on 20.02.16 12:33
 */
public final class Sample {
    private Sample() {
    }

    public static KubeSphereNotification.Event event() {
        return new KubeSphereNotification.Event("testEvent", "argFoo", "foo", "argBar", "bar");
    }

    public static Map<String, Object> extraArgs() {
        Map<String, Object> extraArgs = new Hashtable<String, Object>();
        extraArgs.put("what", "a simple string");
        extraArgs.put("howMuch", 42);
        return extraArgs;
    }
}
