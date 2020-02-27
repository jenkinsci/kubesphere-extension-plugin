/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jenkins.kubesphere.plugins.event.models;


import hudson.model.Job;

import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.IOException;

public class JobState {

    private String name;

    private String displayName;

    private String url;

    private BuildState build;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public BuildState getBuild() {
        return build;
    }

    public void setBuild(BuildState build) {
        this.build = build;
    }

    private JobState(){
    }
    public JobState JobState(JobPhase phase, Run run, TaskListener listener, long timestamp) throws IOException, InterruptedException {
        Job job = run.getParent();
        JobState           jobState     = new JobState();
        BuildState         buildState   = new BuildState(phase,run);
        ScmState           scmState     = new ScmState(run,listener);
        TestState          testState = new TestState(run);

        jobState.setName( job.getName());
        jobState.setDisplayName(job.getDisplayName());
        jobState.setUrl( job.getUrl());
        jobState.setBuild( buildState );

        buildState.setScm( scmState );
        buildState.setTestSummary(testState);



        return jobState;
    }
}
