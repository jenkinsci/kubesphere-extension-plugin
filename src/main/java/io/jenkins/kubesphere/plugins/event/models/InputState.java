package io.jenkins.kubesphere.plugins.event.models;

import org.jenkinsci.plugins.workflow.support.steps.input.InputStep;

import java.util.Arrays;
import java.util.List;

/**
 * @author runzexia
 */
public class InputState {

    private String message;

    /**
     * Optional ID that uniquely identifies this input from all others.
     */
    private String id;

    /**
     * Optional user/group name who can approve this.
     */
    private List<String> submitter;

    private String approver;

    public InputState(InputStep inputStep, String approver) {
        this.setId(inputStep.getId());
        this.setMessage(inputStep.getMessage());
        this.setSubmitter(Arrays.asList(inputStep.getSubmitter().split(",")));
        this.approver = approver;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getSubmitter() {
        return submitter;
    }

    public void setSubmitter(List<String> submitter) {
        this.submitter = submitter;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }
}
