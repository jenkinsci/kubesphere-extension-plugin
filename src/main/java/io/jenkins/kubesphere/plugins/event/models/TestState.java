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


import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author runzexia
 */
public class TestState
{
    private int total;
    private int failed;
    private int passed;
    private int skipped;
    private List<String> failedTests;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public int getPassed() {
        return passed;
    }

    public void setPassed(int passed) {
        this.passed = passed;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public List<String> getFailedTests() {
        return failedTests;
    }

    public void setFailedTests(List<String> failedTests) {
        this.failedTests = failedTests;
    }

    private TestState(){}

    public TestState (Run build){
        TestState resultSummary = null;

        AbstractTestResultAction testAction = build.getAction(AbstractTestResultAction.class);
        if(testAction != null) {
            int total = testAction.getTotalCount();
            int failCount = testAction.getFailCount();
            int skipCount = testAction.getSkipCount();

            resultSummary = new TestState();
            resultSummary.setTotal(total);
            resultSummary.setFailed(failCount);
            resultSummary.setSkipped(skipCount);
            resultSummary.setPassed(total - failCount - skipCount);
            resultSummary.setFailedTests(getFailedTestNames(testAction));
        }

    }
    private List<String> getFailedTestNames(AbstractTestResultAction testResultAction) {
        List<String> failedTests = new ArrayList<>();

        List<? extends TestResult> results = testResultAction.getFailedTests();

        for(TestResult t : results) {
            failedTests.add(t.getFullName());
        }

        return failedTests;
    }
}
