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
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.User;
import hudson.scm.ChangeLogSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ScmState {
    private String url;

    private String branch;

    private String commit;

    private List<String> changes;

    private List<String> culprits;

    private ScmState() {

    }

    public ScmState(Run run, TaskListener listener) {
        EnvVars environment = null;
        try {
            environment = run.getEnvironment(listener);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return;
        }
        if (environment.get("GIT_URL") != null) {
            setUrl(environment.get("GIT_URL"));
        }

        if (environment.get("GIT_BRANCH") != null) {
            setBranch(environment.get("GIT_BRANCH"));
        }

        if (environment.get("GIT_COMMIT") != null) {
            setCommit(environment.get("GIT_COMMIT"));
        }

        setChanges(getChangedFiles(run));
        setCulprits(getCulprits(run));
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public List<String> getChanges() {
        return changes;
    }

    public void setChanges(List<String> changes) {
        this.changes = changes;
    }

    public List<String> getCulprits() {
        return culprits;
    }

    public void setCulprits(List<String> culprits) {
        this.culprits = culprits;
    }

    private List<String> getChangedFiles(Run run) {
        List<String> affectedPaths = new ArrayList<>();

        if (run instanceof AbstractBuild) {
            AbstractBuild build = (AbstractBuild) run;

            Object[] items = build.getChangeSet().getItems();

            if (items != null && items.length > 0) {
                for (Object o : items) {
                    if (o instanceof ChangeLogSet.Entry) {
                        affectedPaths.addAll(((ChangeLogSet.Entry) o).getAffectedPaths());
                    }
                }
            }
        }

        return affectedPaths;
    }

    private List<String> getCulprits(Run run) {
        List<String> culprits = new ArrayList<>();

        if (run instanceof AbstractBuild) {
            AbstractBuild build = (AbstractBuild) run;
            Set<User> buildCulprits = build.getCulprits();
            for (User user : buildCulprits) {
                culprits.add(user.getId());
            }
        }

        return culprits;
    }
}
