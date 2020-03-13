package io.jenkins.kubesphere.plugins.event;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.kubesphere.plugins.event.models.JobPhase;
import io.jenkins.kubesphere.plugins.event.models.JobState;
import org.jenkinsci.plugins.workflow.support.steps.input.InputExtension;
import org.jenkinsci.plugins.workflow.support.steps.input.InputStep;

import java.util.logging.Logger;

/**
 * @author runzexia
 */
@Extension
public class NotificationInputListener implements InputExtension {

    private static final Logger LOGGER = Logger.getLogger(KubeSphereNotification.class.getName());

    @Override
    public void notifyInput(InputStep inputStep, Run run, String userID, TaskListener taskListener, InputEvent inputEvent) {

        if (inputEvent.equals(InputEvent.STARTED)) {
            JobState state = new JobState(JobPhase.PAUSED, run, taskListener, inputStep, userID, run.getTimeInMillis() + run.getDuration());
            KubeSphereNotification.notify(new KubeSphereNotification.Event(KubeSphereNotification.JENKINS_JOB_INPUT_STARTED,
                    "jobState", state));
        } else if (inputEvent.equals(InputEvent.PROCEEDED)) {
            JobState state = new JobState(JobPhase.RUNNING, run, taskListener, inputStep, userID, run.getTimeInMillis() + run.getDuration());
            KubeSphereNotification.notify(new KubeSphereNotification.Event(KubeSphereNotification.JENKINS_JOB_INPUT_PROCEEDED,
                    "jobState", state));

        } else if (inputEvent.equals(InputEvent.ABORTED)) {
            JobState state = new JobState(JobPhase.CANCELED, run, taskListener, inputStep, userID, run.getTimeInMillis() + run.getDuration());
            KubeSphereNotification.notify(new KubeSphereNotification.Event(KubeSphereNotification.JENKINS_JOB_INPUT_ABORTED,
                    "jobState", state));
        } else {
            LOGGER.warning("Unknown InputEvent Type " + inputEvent.toString());
        }
    }
}
