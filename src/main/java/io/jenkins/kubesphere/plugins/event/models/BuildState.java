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

import hudson.EnvVars;

import hudson.model.Job;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.jenkins.kubesphere.plugins.event.Utils.verifyNotEmpty;

/**
 * @author runzexia
 */
public class BuildState {

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




    /**
     *  Map of artifacts: file name => Map of artifact locations ( location name => artifact URL )
     *  ---
     *  artifacts:
     *   notification.hpi:
     *     archive: http://localhost:8080/job/notification-plugin/78/artifact/target/notification.hpi
     *   notification.jar:
     *     archive: http://localhost:8080/job/notification-plugin/78/artifact/target/notification.jar
     */
    private final Map<String, Map<String, String>> artifacts = new HashMap<String, Map<String, String>>();

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
        this.parameters = new HashMap<String, String>( params );
    }

    public Map<String, Map<String, String>> getArtifacts () {
        return artifacts;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ScmState getScm ()
    {
        return scm;
    }

    public void setScm ( ScmState scmState )
    {
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
    public void updateArtifacts (Job job, Run run )
    {
        updateArchivedArtifacts( run );
    }


    private void updateArchivedArtifacts ( Run run )
    {
        @SuppressWarnings( "unchecked" )
        List<Run.Artifact> buildArtifacts = run.getArtifacts();

        if ( buildArtifacts == null ) { return; }

        for ( Run.Artifact a : buildArtifacts ) {
            String artifactUrl = Jenkins.getInstance().getRootUrl() + run.getUrl() + "artifact/" + a.getHref();
            updateArtifact( a.relativePath, "archive", artifactUrl );
        }
    }




    /**
     * Updates an artifact URL.
     *
     * @param fileName     artifact file name
     * @param locationName artifact location name, like "s3" or "archive"
     * @param locationUrl  artifact URL at the location specified
     */
    private void updateArtifact( String fileName, String locationName, String locationUrl )
    {
        verifyNotEmpty( fileName, locationName, locationUrl );

        if ( ! artifacts.containsKey( fileName )) {
            artifacts.put( fileName, new HashMap<String, String>());
        }

        if ( artifacts.get( fileName ).containsKey( locationName )) {
            throw new RuntimeException( String.format(
                "Adding artifacts mapping '%s/%s/%s' - artifacts Map already contains mapping of location '%s': %s",
                fileName, locationName, locationUrl, locationName, artifacts ));
        }

        artifacts.get( fileName ).put( locationName, locationUrl );
    }

    public JobPhase getPhase() {
        return phase;
    }

    public void setPhase(JobPhase phase) {
        this.phase = phase;
    }
    private BuildState(){
    }
    public BuildState(JobPhase phase, Run run){
        Jenkins jenkins      = Jenkins.getInstanceOrNull();
        String rootUrl = null;
        if (jenkins != null) {
            rootUrl = jenkins.getRootUrl();
        }
        Result result       = run.getResult();
        BuildState buildState  = new BuildState();
        buildState.setNumber( run.number );
        buildState.setQueueId( run.getQueueId() );
        buildState.setUrl( run.getUrl());
        buildState.setPhase( phase );
        buildState.setTimestamp( timestamp );
        if ( result != null ) {
            buildState.setStatus(result.toString());
        }

        if ( rootUrl != null ) {
            buildState.setFullUrl(rootUrl + run.getUrl());
        }

        ParametersAction paramsAction = run.getAction(ParametersAction.class);
        if ( paramsAction != null ) {
            EnvVars env = new EnvVars();
            for (ParameterValue value : paramsAction.getParameters()){
                if ( ! value.isSensitive()) {
                    value.buildEnvironment( run, env );
                }
            }
            buildState.setParameters(env);
        }
        buildState.updateArtifacts( run.getParent(), run );
    }
}
