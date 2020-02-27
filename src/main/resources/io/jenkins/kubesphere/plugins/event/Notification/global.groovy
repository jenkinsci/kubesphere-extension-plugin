package io.jenkins.kubesphere.plugins.event.Notification

import io.jenkins.kubesphere.plugins.event.NotificationEndpoint

def f=namespace(lib.FormTagLib)
def j=namespace("jelly:core")


f.section(title:_("KubeSphere Notifications")) {
    f.block {
        f.hetero_list(name:"endpoints", hasHeader:true, descriptors:NotificationEndpoint.all(), items:descriptor.getEndpoints(),
            addCaption:_("Add a new endpoint"), deleteCaption:_("Delete endpoint"))
    }
}
