# KubeSphere Extension Plugin


KubeSphere is an enterprise-grade multi-tenant container management platform that built on Kubernetes.

In the DevOps section we use Jenkins as our engine.

This plugin will extend some of the features that kubesphere uses in Jenkins, such as adding event transitions, extending the Blueocan API, etc.


## 1. BlueOcean ContainerFilter is currently extended in the repository to implement specific API filtering.

## 2. KubeSphereNotification can send events inside Jenkins to different types of NotificationEndpoint.

### 2.1 NotificationEndpoint Type

- WebHookNotificationEndpoint （Send POST request to push JSON data to http / https address.

### 2.2 Event Type

The following types of events are supported.

> Note that the pipeline input event requires the RC version of the pipeline input plugin to be installed

- jenkins.job.started (Events are triggered when the pipeline started)
- jenkins.job.completed (Events are triggered when the pipeline completed)
- jenkins.job.finalized (Events are triggered when the pipeline finalized)
- jenkins.job.input.started (Events are triggered when the pipeline input step started)
- jenkins.job.input.proceeded (Events are triggered when the pipeline input step proceeded)
- jenkins.job.input.aborted (Events are triggered when the pipeline input step aborted)

If you want to know the specific structure of the event, please read [EventExample](EventExample.md)

### 2.3 Configure plugin with CasC

You can configure this plugin using CasC.
Currently, the configuration of Event Custom Endpoint is still not supported.

```yaml
unclassified:
  kubeSphereNotification:
    endpoints:
      - webHook:
          timeout: 300
          url: "http://127.0.0.1:30123/event"
```

### 2.4 Configure plugin with interpolate

Currently the plugin supports interpolate notification configuration.

For Example:
When sending an event, $ {type} will be replaced with the specific event type.
```yaml
unclassified:
  kubeSphereNotification:
    endpoints:
      - webHook:
          timeout: 300
          url: "http://127.0.0.1:30123/event/^${type}"
```

## 3. Basic Header Auth with KubeSphere IAM

This plugin extends [BasicHeaderAuthenticator](https://javadoc.jenkins.io/jenkins/security/BasicHeaderAuthenticator.html), 
allowing users to request Jenkins' API through KubeSphere's API Token.


Configure Basic Header Auth with CasC Plugin:

```yaml
unclassified:
  kubespheretokenauthglobalconfiguration:
    cacheConfiguration:  // enabel auth cache
      size: 20  // cache size
      ttl: 300 // cache object's ttl
    enabled: true 
    server: "http://127.0.0.1:30123/" // ks-apigateway url
```