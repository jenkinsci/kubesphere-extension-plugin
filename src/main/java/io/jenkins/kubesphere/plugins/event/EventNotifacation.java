package io.jenkins.kubesphere.plugins.event;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import org.jenkinsci.Symbol;

@Extension
@Symbol("kubesphere-event-notifacation")
public class EventNotifacation implements Describable<EventNotifacation> {
    @Override
    public Descriptor<EventNotifacation> getDescriptor() {
        return null;
    }
}
