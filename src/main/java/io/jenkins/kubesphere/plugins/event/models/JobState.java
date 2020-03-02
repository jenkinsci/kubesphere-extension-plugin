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
package io.jenkins.kubesphere.plugins.event.models;


import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.IOException;

public class JobState {

    private String name;

    private String displayName;

    private String url;

    private BuildState build;

    private Result previousCompletedResult;

    private JobState() {
    }

    public JobState(JobPhase phase, Run run, TaskListener listener, long timestamp) throws IOException, InterruptedException {
        Job job = run.getParent();
        BuildState buildState = new BuildState(phase, run, timestamp);
        ScmState scmState = new ScmState(run, listener);
        TestState testState = new TestState(run);

        setName(job.getName());
        setDisplayName(job.getDisplayName());
        setUrl(job.getUrl());
        setBuild(buildState);

        buildState.setScm(scmState);
        buildState.setTestSummary(testState);
        setPreviousCompletedResult(findLastBuildThatFinished(run));

    }

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

    public Result getPreviousCompletedResult() {
        return previousCompletedResult;
    }

    public void setPreviousCompletedResult(Result build){
        this.previousCompletedResult = build;
    }


    private Result findLastBuildThatFinished(Run run){
        Run previousRun = run.getPreviousCompletedBuild();
        while(previousRun != null){
            Result previousResults = previousRun.getResult();
            if (previousResults.equals(Result.SUCCESS) || previousResults.equals(Result.FAILURE) || previousResults.equals(Result.UNSTABLE)){
                return previousResults;
            }
            previousRun = previousRun.getPreviousCompletedBuild();
        }
        return null;
    }
}
