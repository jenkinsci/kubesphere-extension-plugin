package io.jenkins.kubesphere.plugins.event.testutil;

import io.jenkins.kubesphere.plugins.event.KubeSphereNotification;
import io.jenkins.kubesphere.plugins.event.models.JobState;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class ObjectUtils {

    public static KubeSphereNotification.Event jsonToEvent(String json){
        JSONObject jsonObj = (JSONObject) JSONSerializer.toJSON(json);
        return (KubeSphereNotification.Event) JSONObject.toBean(jsonObj,KubeSphereNotification.Event.class);
    }

    public static JobState eventToJobState(KubeSphereNotification.Event event){
        JSONObject jsonObj = (JSONObject) JSONSerializer.toJSON(event.getArgs().get("jobState"));
        return  (JobState) JSONObject.toBean(jsonObj, JobState.class);
    }
}
