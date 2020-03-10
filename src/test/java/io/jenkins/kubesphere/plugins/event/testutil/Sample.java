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

    public static final String HELLO_WORLD_JOB = "<flow-definition plugin=\"workflow-job@2.32\">\n" +
            "<actions/>\n" +
            "<description/>\n" +
            "<keepDependencies>false</keepDependencies>\n" +
            "<properties/>\n" +
            "<definition class=\"org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition\" plugin=\"workflow-cps@2.66\">\n" +
            "<script>node { echo 'Hello World' }</script>\n" +
            "<sandbox>true</sandbox>\n" +
            "</definition>\n" +
            "<triggers/>\n" +
            "<disabled>false</disabled>\n" +
            "</flow-definition>";

    public static final String UNIT_TEST_JENKINSFILE = "node {\n" +
            "    def test = \"<testsuite tests=\\\"3\\\">\\n\" +\n" +
            "        \"    <testcase classname=\\\"foo1\\\" name=\\\"ASuccessfulTest\\\"/>\\n\" +\n" +
            "        \"    <testcase classname=\\\"foo2\\\" name=\\\"AnotherSuccessfulTest\\\"/>\\n\" +\n" +
            "        \"    <testcase classname=\\\"foo3\\\" name=\\\"AFailingTest\\\">\\n\" +\n" +
            "        \"        <failure type=\\\"NotEnoughFoo\\\"> details about failure </failure>\\n\" +\n" +
            "        \"    </testcase>\\n\" +\n" +
            "        \"</testsuite>\";\n" +
            "    writeFile file:'result.xml', text: test\n" +
            "    junit 'result.xml'\n" +
            "}\n";

    public static final String ARTIFACTS_TEST_JENKINSFILE = "node {\n" +
            "    def test = \"<testsuite tests=\\\"3\\\">\\n\" +\n" +
            "        \"    <testcase classname=\\\"foo1\\\" name=\\\"ASuccessfulTest\\\"/>\\n\" +\n" +
            "        \"    <testcase classname=\\\"foo2\\\" name=\\\"AnotherSuccessfulTest\\\"/>\\n\" +\n" +
            "        \"    <testcase classname=\\\"foo3\\\" name=\\\"AFailingTest\\\">\\n\" +\n" +
            "        \"        <failure type=\\\"NotEnoughFoo\\\"> details about failure </failure>\\n\" +\n" +
            "        \"    </testcase>\\n\" +\n" +
            "        \"</testsuite>\";\n" +
            "    writeFile file:'result.xml', text: test\n" +
            "    archiveArtifacts artifacts:'result.xml'\n" +
            "    junit 'result.xml'\n" +
            "}\n";
}