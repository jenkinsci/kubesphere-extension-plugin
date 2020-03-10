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

import hudson.EnvVars;
import hudson.model.Job;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.kubesphere.plugins.event.KubeSphereNotification;
import jenkins.model.Jenkins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static io.jenkins.kubesphere.plugins.event.Utils.verifyNotEmpty;

/**
 * @author runzexia
 */
public class BuildState {

    /**
     *  Map of artifacts: file name => Map of artifact locations ( location name => artifact URL )
     *  ---
     *  artifacts:
     *   notification.hpi:
     *     archive: http://localhost:8080/job/notification-plugin/78/artifact/target/notification.hpi
     *   notification.jar:
     *     archive: http://localhost:8080/job/notification-plugin/78/artifact/target/notification.jar
     */

    private Map<String, Artifact> artifacts;
    private String fullUrl;
    private int number;
    private long queueId;
    private JobPhase phase;
    private long timestamp;
    private String status;
    private String url;
    private String displayName;
    private ScmState scm;
    private Map<String, String> parameters;
    private TestState testSummary;

    private BuildState() {
    }

    public BuildState(JobPhase phase, Run run, long timestamp) {
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        String rootUrl = null;
        if (jenkins != null) {
            rootUrl = jenkins.getRootUrl();
        }
        Result result = run.getResult();
        setNumber(run.number);
        setQueueId(run.getQueueId());
        setUrl(run.getUrl());
        setPhase(phase);
        setTimestamp(timestamp);
        if (result != null) {
            setStatus(result.toString());
        }

        if (rootUrl != null) {
            setFullUrl(rootUrl + run.getUrl());
        }

        ParametersAction paramsAction = run.getAction(ParametersAction.class);
        if (paramsAction != null) {
            EnvVars env = new EnvVars();
            for (ParameterValue value : paramsAction.getParameters()) {
                if (!value.isSensitive()) {
                    value.buildEnvironment(run, env);
                }
            }

            setParameters(env);

        }
        setArtifacts(new HashMap<>());
        updateArtifacts(run.getParent(), run);
    }

    public Map<String, Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(Map<String, Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public long getQueueId() {
        return queueId;
    }

    public void setQueueId(long queue) {
        this.queueId = queue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> params) {
        this.parameters = params;
    }


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ScmState getScm() {
        return scm;
    }

    public void setScm(ScmState scmState) {
        this.scm = scmState;
    }

    public TestState getTestSummary() {
        return testSummary;
    }

    public void setTestSummary(TestState testSummary) {
        this.testSummary = testSummary;
    }




    /**
     * Updates artifacts Map with S3 links, if corresponding publisher is available.
     * @param job Job to update
     * @param run Run to update
     */
    public void updateArtifacts(Job job, Run run) {
        updateArchivedArtifacts(run);
    }

    private void updateArchivedArtifacts(Run run) {
        @SuppressWarnings("unchecked")
        List<Run.Artifact> buildArtifacts = run.getArtifacts();
        for (Run.Artifact a : buildArtifacts) {
            String artifactUrl = Jenkins.getInstance().getRootUrl() + run.getUrl() + "artifact/" + a.getHref();
            updateArtifact(a.relativePath, artifactUrl);
        }
    }

    /**
     * Updates an artifact URL.
     *
     * @param fileName     artifact file name
     * @param locationUrl  artifact URL at the location specified
     */
    private void updateArtifact(String fileName, String locationUrl) {
        verifyNotEmpty(fileName, locationUrl);
        if (!artifacts.containsKey(fileName)) {
            artifacts.put(fileName, new Artifact());
        }
        artifacts.get(fileName).setArchive(locationUrl);
    }

    public JobPhase getPhase() {
        return phase;
    }

    public void setPhase(JobPhase phase) {
        this.phase = phase;
    }
}
