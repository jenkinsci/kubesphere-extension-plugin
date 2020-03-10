package io.jenkins.kubesphere.plugins.event;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.StreamTaskListener;
import io.jenkins.kubesphere.plugins.event.models.JobPhase;
import io.jenkins.kubesphere.plugins.event.models.JobState;
import io.jenkins.kubesphere.plugins.event.testutil.ObjectUtils;
import io.jenkins.kubesphere.plugins.event.testutil.Sample;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import jenkins.branch.BranchSource;
import jenkins.branch.DefaultBranchPropertyStrategy;
import jenkins.model.Jenkins;
import jenkins.plugins.git.GitSCMSource;
import jenkins.plugins.git.GitSampleRepoRule;
import jenkins.plugins.git.traits.BranchDiscoveryTrait;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;

public class WebHookEventTest {

    @Rule
    public JenkinsConfiguredWithCodeRule j = new JenkinsConfiguredWithCodeRule();

    @Rule
    public GitSampleRepoRule sampleRepo1 = new GitSampleRepoRule();

    @Rule
    public WireMockRule webHook = new WireMockRule(
            WireMockConfiguration.wireMockConfig().port(30123)
    );

    @Before
    public void prepareWireMock() throws Exception {
        webHook.stubFor(
                post(urlMatching("/event"))
                        .willReturn(aResponse()
                                .withStatus(200))
        );
        webHook.stubFor(
                post(urlMatching("/event/jenkins.job.started"))
                        .willReturn(aResponse()
                                .withStatus(200))
        );
        webHook.stubFor(
                post(urlMatching("/event/jenkins.job.completed"))
                        .willReturn(aResponse()
                                .withStatus(200))
        );
        webHook.stubFor(
                post(urlMatching("/event/jenkins.job.finalized"))
                        .willReturn(aResponse()
                                .withStatus(200))
        );

    }


    @Test
    @ConfiguredWithCode("casc.yaml")
    public void expectedCASC() throws Exception {
        KubeSphereNotification.DescriptorImpl descriptor = Jenkins.get().getExtensionList(KubeSphereNotification.class).get(0).getDescriptor();
        Assert.assertEquals(descriptor.getEndpoints().size(), 1);
        Assert.assertEquals(((WebHookNotificationEndpoint) descriptor.getEndpoints().get(0)).getUrl(), baseUrl() + "/event");
    }

    @Test
    @ConfiguredWithCode("casc.yaml")
    public void triggerJobTest() throws IOException, InterruptedException, ExecutionException {
        j.jenkins.createProjectFromXML("hello", new ByteArrayInputStream(
                Sample.HELLO_WORLD_JOB.getBytes(StandardCharsets.UTF_8)));
        WorkflowJob job = (WorkflowJob) j.jenkins.getItemByFullName("hello", Job.class);
        Run run = job.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(run);
        // sleep to wait event
        Thread.sleep(1000);
        verify(3, postRequestedFor(urlEqualTo("/event")));
        List<LoggedRequest> requests = WireMock.findAll(postRequestedFor(urlEqualTo("/event")));

        int startEventCount = 0;
        int completedEventCount = 0;
        int finalizedEventCount = 0;
        for (LoggedRequest request : requests) {
            KubeSphereNotification.Event event = ObjectUtils.jsonToEvent(request.getBodyAsString());
            switch (event.getType()) {
                case KubeSphereNotification.JENKINS_JOB_STARTED:
                    startEventCount++;
                    JobState state = ObjectUtils.eventToJobState(event);
                    Assert.assertEquals(state.getName(), "hello");
                    Assert.assertEquals(state.getBuild().getPhase(), JobPhase.STARTED);
                    break;
                case KubeSphereNotification.JENKINS_JOB_COMPLETED:
                    completedEventCount++;
                    state = ObjectUtils.eventToJobState(event);
                    Assert.assertEquals(state.getName(), "hello");
                    Assert.assertEquals(state.getBuild().getPhase(), JobPhase.COMPLETED);
                    break;
                case KubeSphereNotification.JENKINS_JOB_FINALIZED:
                    finalizedEventCount++;
                    state = ObjectUtils.eventToJobState(event);
                    Assert.assertEquals(state.getName(), "hello");
                    Assert.assertEquals(state.getBuild().getPhase(), JobPhase.FINALIZED);
                    break;
            }
        }
        Assert.assertEquals(startEventCount, 1);
        Assert.assertEquals(completedEventCount, 1);
        Assert.assertEquals(finalizedEventCount, 1);
    }

    @Test
    @ConfiguredWithCode("casc_interpolate.yaml")
    public void triggerJobWithInterpolateTest() throws IOException, InterruptedException, ExecutionException {
        j.jenkins.createProjectFromXML("hello", new ByteArrayInputStream(
                Sample.HELLO_WORLD_JOB.getBytes(StandardCharsets.UTF_8)));
        WorkflowJob job = (WorkflowJob) j.jenkins.getItemByFullName("hello", Job.class);
        Run run = job.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(run);
        // sleep to wait event
        Thread.sleep(1000);
        verify(1, postRequestedFor(urlEqualTo("/event/jenkins.job.started")));
        verify(1, postRequestedFor(urlEqualTo("/event/jenkins.job.completed")));
        verify(1, postRequestedFor(urlEqualTo("/event/jenkins.job.finalized")));

        List<LoggedRequest> requests = WireMock.findAll(
                postRequestedFor(urlEqualTo("/event/jenkins.job.started")));
        Assert.assertEquals(requests.size(), 1);

        KubeSphereNotification.Event startEvent = ObjectUtils.jsonToEvent(requests.get(0).getBodyAsString());
        Assert.assertEquals(startEvent.getType(), KubeSphereNotification.JENKINS_JOB_STARTED);
        JobState startState = ObjectUtils.eventToJobState(startEvent);
        Assert.assertEquals(startState.getName(), "hello");
        Assert.assertEquals(startState.getBuild().getPhase(), JobPhase.STARTED);
        Assert.assertEquals(startState.getBuild().getNumber(), 1);

        requests = WireMock.findAll(
                postRequestedFor(urlEqualTo("/event/jenkins.job.completed")));
        Assert.assertEquals(requests.size(), 1);

        KubeSphereNotification.Event completedEvent = ObjectUtils.jsonToEvent(requests.get(0).getBodyAsString());
        Assert.assertEquals(completedEvent.getType(), KubeSphereNotification.JENKINS_JOB_COMPLETED);
        JobState completedState = ObjectUtils.eventToJobState(completedEvent);
        Assert.assertEquals(completedState.getName(), "hello");
        Assert.assertEquals(completedState.getBuild().getPhase(), JobPhase.COMPLETED);
        Assert.assertEquals(completedState.getBuild().getNumber(), 1);
        Assert.assertEquals(completedState.getBuild().getStatus(), Result.SUCCESS.toString());

        requests = WireMock.findAll(
                postRequestedFor(urlEqualTo("/event/jenkins.job.finalized")));
        Assert.assertEquals(requests.size(), 1);

        KubeSphereNotification.Event finalizedEvent = ObjectUtils.jsonToEvent(requests.get(0).getBodyAsString());
        Assert.assertEquals(finalizedEvent.getType(), KubeSphereNotification.JENKINS_JOB_FINALIZED);
        JobState finalizedState = ObjectUtils.eventToJobState(finalizedEvent);
        Assert.assertEquals(finalizedState.getName(), "hello");
        Assert.assertEquals(finalizedState.getBuild().getPhase(), JobPhase.FINALIZED);
        Assert.assertEquals(finalizedState.getBuild().getNumber(), 1);
        Assert.assertEquals(finalizedState.getBuild().getStatus(), Result.SUCCESS.toString());

    }

    @Test
    @ConfiguredWithCode("casc_interpolate.yaml")
    public void JobWithUTTest() throws IOException, InterruptedException, ExecutionException {

        WorkflowJob p = j.createProject(WorkflowJob.class, "unit_test");
        p.setDefinition(new CpsFlowDefinition(Sample.UNIT_TEST_JENKINSFILE, false));
        p.save();
        WorkflowJob job = (WorkflowJob) j.jenkins.getItemByFullName("unit_test", Job.class);
        Run run = job.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(run);
        // sleep to wait event
        Thread.sleep(1000);
        verify(1, postRequestedFor(urlEqualTo("/event/jenkins.job.started")));
        verify(1, postRequestedFor(urlEqualTo("/event/jenkins.job.completed")));
        verify(1, postRequestedFor(urlEqualTo("/event/jenkins.job.finalized")));

        List<LoggedRequest> requests = WireMock.findAll(
                postRequestedFor(urlEqualTo("/event/jenkins.job.started")));
        Assert.assertEquals(requests.size(), 1);

        KubeSphereNotification.Event startEvent = ObjectUtils.jsonToEvent(requests.get(0).getBodyAsString());
        Assert.assertEquals(startEvent.getType(), KubeSphereNotification.JENKINS_JOB_STARTED);
        JobState startState = ObjectUtils.eventToJobState(startEvent);
        Assert.assertEquals(startState.getName(), "unit_test");
        Assert.assertEquals(startState.getBuild().getPhase(), JobPhase.STARTED);
        Assert.assertEquals(startState.getBuild().getNumber(), 1);

        requests = WireMock.findAll(
                postRequestedFor(urlEqualTo("/event/jenkins.job.completed")));
        Assert.assertEquals(requests.size(), 1);

        KubeSphereNotification.Event completedEvent = ObjectUtils.jsonToEvent(requests.get(0).getBodyAsString());
        Assert.assertEquals(completedEvent.getType(), KubeSphereNotification.JENKINS_JOB_COMPLETED);
        JobState completedState = ObjectUtils.eventToJobState(completedEvent);
        Assert.assertEquals(completedState.getName(), "unit_test");
        Assert.assertEquals(completedState.getBuild().getPhase(), JobPhase.COMPLETED);
        Assert.assertEquals(completedState.getBuild().getNumber(), 1);
        Assert.assertEquals(completedState.getBuild().getStatus(), Result.UNSTABLE.toString());
        Assert.assertEquals(completedState.getBuild().getTestSummary().getFailed(), 1);
        Assert.assertEquals(completedState.getBuild().getTestSummary().getPassed(), 2);
        Assert.assertEquals(completedState.getBuild().getTestSummary().getTotal(), 3);
        Assert.assertEquals(completedState.getBuild().getTestSummary().getFailedTests().size(), 1);
        Assert.assertEquals(completedState.getBuild().getTestSummary().getFailedTests().get(0), "foo3.AFailingTest");

        requests = WireMock.findAll(
                postRequestedFor(urlEqualTo("/event/jenkins.job.finalized")));
        Assert.assertEquals(requests.size(), 1);

        KubeSphereNotification.Event finalizedEvent = ObjectUtils.jsonToEvent(requests.get(0).getBodyAsString());
        Assert.assertEquals(finalizedEvent.getType(), KubeSphereNotification.JENKINS_JOB_FINALIZED);
        JobState finalizedState = ObjectUtils.eventToJobState(finalizedEvent);
        Assert.assertEquals(finalizedState.getName(), "unit_test");
        Assert.assertEquals(finalizedState.getBuild().getPhase(), JobPhase.FINALIZED);
        Assert.assertEquals(finalizedState.getBuild().getNumber(), 1);
        Assert.assertEquals(finalizedState.getBuild().getStatus(), Result.UNSTABLE.toString());
        Assert.assertEquals(completedState.getBuild().getTestSummary().getFailed(), 1);
        Assert.assertEquals(completedState.getBuild().getTestSummary().getPassed(), 2);
        Assert.assertEquals(completedState.getBuild().getTestSummary().getTotal(), 3);
        Assert.assertEquals(completedState.getBuild().getTestSummary().getFailedTests().size(), 1);
        Assert.assertEquals(completedState.getBuild().getTestSummary().getFailedTests().get(0), "foo3.AFailingTest");

    }

    @Test
    @ConfiguredWithCode("casc_interpolate.yaml")
    public void JobWithRunTwiceTest() throws IOException, InterruptedException, ExecutionException {

        WorkflowJob p = j.createProject(WorkflowJob.class, "unit_test");
        p.setDefinition(new CpsFlowDefinition(Sample.UNIT_TEST_JENKINSFILE, false));
        p.save();
        WorkflowJob job = (WorkflowJob) j.jenkins.getItemByFullName("unit_test", Job.class);
        Run run = job.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(run);
        Run run2 = job.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(run2);
        // sleep to wait event
        Thread.sleep(1000);

        verify(2, postRequestedFor(urlEqualTo("/event/jenkins.job.started")));
        verify(2, postRequestedFor(urlEqualTo("/event/jenkins.job.completed")));
        verify(2, postRequestedFor(urlEqualTo("/event/jenkins.job.finalized")));

        List<LoggedRequest> requests = WireMock.findAll(
                postRequestedFor(urlEqualTo("/event/jenkins.job.started")));
        Assert.assertEquals(requests.size(), 2);

        KubeSphereNotification.Event startEvent = ObjectUtils.jsonToEvent(requests.get(1).getBodyAsString());
        Assert.assertEquals(startEvent.getType(), KubeSphereNotification.JENKINS_JOB_STARTED);
        JobState startState = ObjectUtils.eventToJobState(startEvent);
        Assert.assertEquals(startState.getName(), "unit_test");
        Assert.assertEquals(startState.getBuild().getPhase(), JobPhase.STARTED);
        Assert.assertEquals(startState.getBuild().getNumber(), 2);
        Assert.assertEquals(startState.getPreviousCompletedBuild().getNumber(), 1);
        Assert.assertEquals(startState.getPreviousCompletedBuild().getStatus(), Result.UNSTABLE.toString());
        Assert.assertEquals(startState.getPreviousCompletedBuild().getPhase(), JobPhase.COMPLETED);
        Assert.assertEquals(startState.getPreviousCompletedBuild().getTestSummary().getFailed(), 1);
        Assert.assertEquals(startState.getPreviousCompletedBuild().getTestSummary().getTotal(), 3);
        Assert.assertEquals(startState.getPreviousCompletedBuild().getTestSummary().getPassed(), 2);


        requests = WireMock.findAll(
                postRequestedFor(urlEqualTo("/event/jenkins.job.completed")));
        Assert.assertEquals(requests.size(), 2);

        KubeSphereNotification.Event completedEvent = ObjectUtils.jsonToEvent(requests.get(1).getBodyAsString());
        Assert.assertEquals(completedEvent.getType(), KubeSphereNotification.JENKINS_JOB_COMPLETED);
        JobState completedState = ObjectUtils.eventToJobState(completedEvent);
        Assert.assertEquals(completedState.getName(), "unit_test");
        Assert.assertEquals(completedState.getBuild().getPhase(), JobPhase.COMPLETED);
        Assert.assertEquals(completedState.getBuild().getNumber(), 2);
        Assert.assertEquals(completedState.getBuild().getStatus(), Result.UNSTABLE.toString());
        Assert.assertEquals(completedState.getBuild().getTestSummary().getFailed(), 1);
        Assert.assertEquals(completedState.getBuild().getTestSummary().getPassed(), 2);
        Assert.assertEquals(completedState.getBuild().getTestSummary().getTotal(), 3);
        Assert.assertEquals(completedState.getBuild().getTestSummary().getFailedTests().size(), 1);
        Assert.assertEquals(completedState.getBuild().getTestSummary().getFailedTests().get(0), "foo3.AFailingTest");

        requests = WireMock.findAll(
                postRequestedFor(urlEqualTo("/event/jenkins.job.finalized")));
        Assert.assertEquals(requests.size(), 2);

        KubeSphereNotification.Event finalizedEvent = ObjectUtils.jsonToEvent(requests.get(1).getBodyAsString());
        Assert.assertEquals(finalizedEvent.getType(), KubeSphereNotification.JENKINS_JOB_FINALIZED);
        JobState finalizedState = ObjectUtils.eventToJobState(finalizedEvent);
        Assert.assertEquals(finalizedState.getName(), "unit_test");
        Assert.assertEquals(finalizedState.getBuild().getPhase(), JobPhase.FINALIZED);
        Assert.assertEquals(finalizedState.getBuild().getNumber(), 2);
        Assert.assertEquals(finalizedState.getBuild().getStatus(), Result.UNSTABLE.toString());
        Assert.assertEquals(completedState.getBuild().getTestSummary().getFailed(), 1);
        Assert.assertEquals(completedState.getBuild().getTestSummary().getPassed(), 2);
        Assert.assertEquals(completedState.getBuild().getTestSummary().getTotal(), 3);
        Assert.assertEquals(completedState.getBuild().getTestSummary().getFailedTests().size(), 1);
        Assert.assertEquals(completedState.getBuild().getTestSummary().getFailedTests().get(0), "foo3.AFailingTest");

    }


    @Test
    @ConfiguredWithCode("casc_interpolate.yaml")
    public void JobWithArtifactsTest() throws IOException, InterruptedException, ExecutionException {
        WorkflowJob p = j.createProject(WorkflowJob.class, "artifacts_test");
        p.setDefinition(new CpsFlowDefinition(Sample.ARTIFACTS_TEST_JENKINSFILE, false));
        p.save();
        WorkflowJob job = (WorkflowJob) j.jenkins.getItemByFullName("artifacts_test", Job.class);
        Run run = job.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(run);
        Run run2 = job.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(run2);
        // sleep to wait event
        Thread.sleep(1000);

        verify(2, postRequestedFor(urlEqualTo("/event/jenkins.job.started")));
        verify(2, postRequestedFor(urlEqualTo("/event/jenkins.job.completed")));
        verify(2, postRequestedFor(urlEqualTo("/event/jenkins.job.finalized")));

        List<LoggedRequest> requests = WireMock.findAll(
                postRequestedFor(urlEqualTo("/event/jenkins.job.started")));
        Assert.assertEquals(requests.size(), 2);

        KubeSphereNotification.Event startEvent = ObjectUtils.jsonToEvent(requests.get(1).getBodyAsString());
        Assert.assertEquals(startEvent.getType(), KubeSphereNotification.JENKINS_JOB_STARTED);
        JobState startState = ObjectUtils.eventToJobState(startEvent);
        Assert.assertEquals(startState.getName(), "artifacts_test");
        Assert.assertEquals(startState.getBuild().getPhase(), JobPhase.STARTED);
        Assert.assertEquals(startState.getBuild().getNumber(), 2);
        Assert.assertEquals(startState.getPreviousCompletedBuild().getNumber(), 1);
        Assert.assertEquals(startState.getPreviousCompletedBuild().getArtifacts().size(), 1);
        Assert.assertNotNull(startState.getPreviousCompletedBuild().getArtifacts().get("result.xml"));

        requests = WireMock.findAll(
                postRequestedFor(urlEqualTo("/event/jenkins.job.completed")));
        Assert.assertEquals(requests.size(), 2);

        KubeSphereNotification.Event completedEvent = ObjectUtils.jsonToEvent(requests.get(1).getBodyAsString());
        Assert.assertEquals(completedEvent.getType(), KubeSphereNotification.JENKINS_JOB_COMPLETED);
        JobState completedState = ObjectUtils.eventToJobState(completedEvent);
        Assert.assertEquals(completedState.getName(), "artifacts_test");
        Assert.assertEquals(completedState.getBuild().getPhase(), JobPhase.COMPLETED);
        Assert.assertEquals(completedState.getBuild().getNumber(), 2);
        Assert.assertEquals(completedState.getBuild().getStatus(), Result.UNSTABLE.toString());
        Assert.assertEquals(completedState.getBuild().getArtifacts().size(), 1);
        Assert.assertNotNull(completedState.getBuild().getArtifacts().get("result.xml"));

        requests = WireMock.findAll(
                postRequestedFor(urlEqualTo("/event/jenkins.job.finalized")));
        Assert.assertEquals(requests.size(), 2);

        KubeSphereNotification.Event finalizedEvent = ObjectUtils.jsonToEvent(requests.get(1).getBodyAsString());
        Assert.assertEquals(finalizedEvent.getType(), KubeSphereNotification.JENKINS_JOB_FINALIZED);
        JobState finalizedState = ObjectUtils.eventToJobState(finalizedEvent);
        Assert.assertEquals(finalizedState.getName(), "artifacts_test");
        Assert.assertEquals(finalizedState.getBuild().getPhase(), JobPhase.FINALIZED);
        Assert.assertEquals(finalizedState.getBuild().getNumber(), 2);
        Assert.assertEquals(finalizedState.getBuild().getStatus(), Result.UNSTABLE.toString());
        Assert.assertEquals(finalizedState.getBuild().getArtifacts().size(), 1);
        Assert.assertNotNull(finalizedState.getBuild().getArtifacts().get("result.xml"));
    }

    @Test
    @ConfiguredWithCode("casc_interpolate.yaml")
    public void MultiBranchJobTest() throws Exception {
        sampleRepo1.init();
        sampleRepo1.write("Jenkinsfile", "node { echo 'hi'; }");
        sampleRepo1.git("add", "Jenkinsfile");
        sampleRepo1.git("commit", "--all", "--message=buildable");

        WorkflowMultiBranchProject project = j.jenkins.createProject(WorkflowMultiBranchProject.class, "Repo");
        GitSCMSource source = new GitSCMSource(sampleRepo1.toString());
        source.setTraits(new ArrayList<>(Arrays.asList(new BranchDiscoveryTrait())));

        BranchSource branchSource = new BranchSource(source);
        branchSource.setStrategy(new DefaultBranchPropertyStrategy(null));

        TaskListener listener = StreamTaskListener.fromStderr();
        assertEquals("[SCMHead{'master'}]", source.fetch(listener).toString());
        project.setSourcesList(new ArrayList<>(Arrays.asList(branchSource)));

        project.scheduleBuild2(0).getFuture().get();

        Thread.sleep(5000);
        verify(1, postRequestedFor(urlEqualTo("/event/jenkins.job.started")));
        verify(1, postRequestedFor(urlEqualTo("/event/jenkins.job.completed")));
        verify(1, postRequestedFor(urlEqualTo("/event/jenkins.job.finalized")));

        List<LoggedRequest> requests = WireMock.findAll(
                postRequestedFor(urlEqualTo("/event/jenkins.job.started")));
        Assert.assertEquals(requests.size(), 1);

        KubeSphereNotification.Event startEvent = ObjectUtils.jsonToEvent(requests.get(0).getBodyAsString());
        Assert.assertEquals(startEvent.getType(), KubeSphereNotification.JENKINS_JOB_STARTED);
        JobState startState = ObjectUtils.eventToJobState(startEvent);
        Assert.assertEquals(startState.getName(), "master");
        Assert.assertEquals(startState.getBuild().getPhase(), JobPhase.STARTED);
        Assert.assertEquals(startState.getBuild().getNumber(), 1);
        Assert.assertEquals(startState.getUrl(),"job/Repo/job/master/");


        requests = WireMock.findAll(
                postRequestedFor(urlEqualTo("/event/jenkins.job.completed")));
        Assert.assertEquals(requests.size(), 1);

        KubeSphereNotification.Event completedEvent = ObjectUtils.jsonToEvent(requests.get(0).getBodyAsString());
        Assert.assertEquals(completedEvent.getType(), KubeSphereNotification.JENKINS_JOB_COMPLETED);
        JobState completedState = ObjectUtils.eventToJobState(completedEvent);
        Assert.assertEquals(completedState.getName(), "master");
        Assert.assertEquals(completedState.getBuild().getPhase(), JobPhase.COMPLETED);
        Assert.assertEquals(completedState.getBuild().getNumber(), 1);
        Assert.assertEquals(completedState.getBuild().getStatus(), Result.SUCCESS.toString());
        Assert.assertEquals(completedState.getUrl(),"job/Repo/job/master/");

        requests = WireMock.findAll(
                postRequestedFor(urlEqualTo("/event/jenkins.job.finalized")));
        Assert.assertEquals(requests.size(), 1);

        KubeSphereNotification.Event finalizedEvent = ObjectUtils.jsonToEvent(requests.get(0).getBodyAsString());
        Assert.assertEquals(finalizedEvent.getType(), KubeSphereNotification.JENKINS_JOB_FINALIZED);
        JobState finalizedState = ObjectUtils.eventToJobState(finalizedEvent);
        Assert.assertEquals(finalizedState.getName(), "master");
        Assert.assertEquals(finalizedState.getBuild().getPhase(), JobPhase.FINALIZED);
        Assert.assertEquals(finalizedState.getBuild().getNumber(), 1);
        Assert.assertEquals(finalizedState.getBuild().getStatus(), Result.SUCCESS.toString());
        Assert.assertEquals(finalizedState.getUrl(),"job/Repo/job/master/");
    }

    @Test
    @ConfiguredWithCode("casc_interpolate.yaml")
    public void MultiBranchJobTwiceTest() throws Exception {
        sampleRepo1.init();
        sampleRepo1.write("Jenkinsfile", "node { echo 'hi'; }");
        sampleRepo1.git("add", "Jenkinsfile");
        sampleRepo1.git("commit", "--all", "--message=build1");

        WorkflowMultiBranchProject project = j.jenkins.createProject(WorkflowMultiBranchProject.class, "Repo");
        GitSCMSource source = new GitSCMSource(sampleRepo1.toString());
        source.setTraits(new ArrayList<>(Arrays.asList(new BranchDiscoveryTrait())));

        BranchSource branchSource = new BranchSource(source);
        branchSource.setStrategy(new DefaultBranchPropertyStrategy(null));

        TaskListener listener = StreamTaskListener.fromStderr();
        assertEquals("[SCMHead{'master'}]", source.fetch(listener).toString());
        project.setSourcesList(new ArrayList<>(Arrays.asList(branchSource)));

        project.scheduleBuild2(0).getFuture().get();
        Thread.sleep(1000);
        sampleRepo1.write("Jenkinsfile", Sample.UNIT_TEST_JENKINSFILE);
        sampleRepo1.git("add", "Jenkinsfile");
        sampleRepo1.git("commit", "--all", "--message=build2");
        project.scheduleBuild2(0).getFuture().get();

        Thread.sleep(2000);

        verify(2, postRequestedFor(urlEqualTo("/event/jenkins.job.started")));
        verify(2, postRequestedFor(urlEqualTo("/event/jenkins.job.completed")));
        verify(2, postRequestedFor(urlEqualTo("/event/jenkins.job.finalized")));

        List<LoggedRequest> requests = WireMock.findAll(
                postRequestedFor(urlEqualTo("/event/jenkins.job.started")));
        Assert.assertEquals(requests.size(), 2);

        KubeSphereNotification.Event startEvent = ObjectUtils.jsonToEvent(requests.get(1).getBodyAsString());
        Assert.assertEquals(startEvent.getType(), KubeSphereNotification.JENKINS_JOB_STARTED);
        JobState startState = ObjectUtils.eventToJobState(startEvent);
        Assert.assertEquals(startState.getName(), "master");
        Assert.assertEquals(startState.getBuild().getPhase(), JobPhase.STARTED);
        Assert.assertEquals(startState.getBuild().getNumber(), 2);
        Assert.assertEquals(startState.getUrl(),"job/Repo/job/master/");


        requests = WireMock.findAll(
                postRequestedFor(urlEqualTo("/event/jenkins.job.completed")));
        Assert.assertEquals(requests.size(), 2);

        KubeSphereNotification.Event completedEvent = ObjectUtils.jsonToEvent(requests.get(1).getBodyAsString());
        Assert.assertEquals(completedEvent.getType(), KubeSphereNotification.JENKINS_JOB_COMPLETED);
        JobState completedState = ObjectUtils.eventToJobState(completedEvent);
        Assert.assertEquals(completedState.getName(), "master");
        Assert.assertEquals(completedState.getBuild().getPhase(), JobPhase.COMPLETED);
        Assert.assertEquals(completedState.getBuild().getNumber(), 2);
        Assert.assertEquals(completedState.getBuild().getStatus(), Result.UNSTABLE.toString());
        Assert.assertEquals(completedState.getUrl(),"job/Repo/job/master/");

        requests = WireMock.findAll(
                postRequestedFor(urlEqualTo("/event/jenkins.job.finalized")));
        Assert.assertEquals(requests.size(), 2);

        KubeSphereNotification.Event finalizedEvent = ObjectUtils.jsonToEvent(requests.get(1).getBodyAsString());
        Assert.assertEquals(finalizedEvent.getType(), KubeSphereNotification.JENKINS_JOB_FINALIZED);
        JobState finalizedState = ObjectUtils.eventToJobState(finalizedEvent);
        Assert.assertEquals(finalizedState.getName(), "master");
        Assert.assertEquals(finalizedState.getBuild().getPhase(), JobPhase.FINALIZED);
        Assert.assertEquals(finalizedState.getBuild().getNumber(), 2);
        Assert.assertEquals(finalizedState.getBuild().getStatus(), Result.UNSTABLE.toString());
        Assert.assertEquals(finalizedState.getUrl(),"job/Repo/job/master/");
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + webHook.port();
    }
}
