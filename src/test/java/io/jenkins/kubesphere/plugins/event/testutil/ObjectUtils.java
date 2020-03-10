package io.jenkins.kubesphere.plugins.event.testutil;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jenkins.kubesphere.plugins.event.KubeSphereNotification;
import io.jenkins.kubesphere.plugins.event.models.JobState;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class ObjectUtils {
    private static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();

    public static KubeSphereNotification.Event jsonToEvent(String json){
        return gson.fromJson(json,KubeSphereNotification.Event.class);
    }

    public static JobState eventToJobState(KubeSphereNotification.Event event){
        String json = gson.toJson(event.getArgs().get("jobState"));
        return  gson.fromJson(json, JobState.class);
    }
}
