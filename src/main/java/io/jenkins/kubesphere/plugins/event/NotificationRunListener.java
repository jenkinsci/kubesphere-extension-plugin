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
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.kubesphere.plugins.event.models.JobPhase;
import io.jenkins.kubesphere.plugins.event.models.JobState;

import java.io.IOException;

/**
 * @author runzexia
 */
@Extension
public class NotificationRunListener extends RunListener<Run> {

    @Override
    public void onStarted(Run r, TaskListener listener) {
        try {
            JobState state = new JobState(JobPhase.STARTED, r, listener, r.getTimeInMillis());

            Notification.notify(new Notification.Event(Notification.JENKINS_JOB_STARTED,
                    "jobState", state));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompleted(Run r, TaskListener listener) {
        try {
            JobState state = new JobState(JobPhase.COMPLETED, r, listener, r.getTimeInMillis() + +r.getDuration());
            Notification.notify(new Notification.Event(Notification.JENKINS_JOB_COMPLETED,
                    "jobState", state));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFinalized(Run r) {
        try {
            JobState state = new JobState(JobPhase.FINALIZED, r, null, r.getTimeInMillis() + +r.getDuration());
            Notification.notify(new Notification.Event(Notification.JENKINS_JOB_FINALIZED,
                    "jobState", state));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
