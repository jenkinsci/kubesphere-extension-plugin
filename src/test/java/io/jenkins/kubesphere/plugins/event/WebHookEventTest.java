package io.jenkins.kubesphere.plugins.event;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.kubesphere.plugins.event.models.JobPhase;
import io.jenkins.kubesphere.plugins.event.models.JobState;
import io.jenkins.kubesphere.plugins.event.testutil.ObjectUtils;
import io.jenkins.kubesphere.plugins.event.testutil.Sample;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

public class WebHookEventTest {

    @Rule
    public JenkinsConfiguredWithCodeRule j = new JenkinsConfiguredWithCodeRule();

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

    }


    @Test
    @ConfiguredWithCode("casc.yaml")
    public void expectedCASC() throws Exception {
        KubeSphereNotification.DescriptorImpl descriptor = Jenkins.get().getExtensionList(KubeSphereNotification.class).get(0).getDescriptor();
        Assert.assertEquals(descriptor.getEndpoints().size(),1);
        Assert.assertEquals(((WebHookNotificationEndpoint)descriptor.getEndpoints().get(0)).getUrl(), baseUrl()+"/event");
    }

    @Test
    @ConfiguredWithCode("casc.yaml")
    public void triggerJobTest() throws IOException, InterruptedException, ExecutionException {
        j.jenkins.createProjectFromXML("hello", new ByteArrayInputStream(
                Sample.HELLO_WORLD_JOB.getBytes(StandardCharsets.UTF_8)));
        WorkflowJob job = (WorkflowJob) j.jenkins.getItemByFullName("hello", Job.class);
        Run run = job.scheduleBuild2(0).waitForStart();
        j.waitForCompletion(run);
        verify(3, postRequestedFor(urlEqualTo("/event")));
        List<LoggedRequest> requests = WireMock.findAll(postRequestedFor(urlEqualTo("/event")));

        int startEventCount = 0;
        int completedEventCount = 0;
        int finalizedEventCount = 0;
        for (LoggedRequest request: requests){
             KubeSphereNotification.Event event = ObjectUtils.jsonToEvent(request.getBodyAsString());
             switch (event.getType()){
                 case KubeSphereNotification.JENKINS_JOB_STARTED:
                     startEventCount++;
                     JobState state = ObjectUtils.eventToJobState(event);
                     Assert.assertEquals(state.getName(),"hello");
                     Assert.assertEquals(state.getBuild().getPhase(),JobPhase.STARTED);
                     break;
                 case KubeSphereNotification.JENKINS_JOB_COMPLETED:
                     completedEventCount++;
                     state = ObjectUtils.eventToJobState(event);
                     Assert.assertEquals(state.getName(),"hello");
                     Assert.assertEquals(state.getBuild().getPhase(),JobPhase.COMPLETED);
                     break;
                 case KubeSphereNotification.JENKINS_JOB_FINALIZED:
                     finalizedEventCount++;
                     state = ObjectUtils.eventToJobState(event);
                     Assert.assertEquals(state.getName(),"hello");
                     Assert.assertEquals(state.getBuild().getPhase(),JobPhase.FINALIZED);
                     break;
             }
        }
        Assert.assertEquals(startEventCount,1);
        Assert.assertEquals(completedEventCount,1);
        Assert.assertEquals(finalizedEventCount,1);
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + webHook.port();
    }
}
