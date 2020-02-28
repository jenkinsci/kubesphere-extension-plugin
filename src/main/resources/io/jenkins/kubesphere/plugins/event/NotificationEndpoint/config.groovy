package io.jenkins.kubesphere.plugins.event.NotificationEndpoint

import io.jenkins.kubesphere.plugins.event.Notification
def f=namespace(lib.FormTagLib)
def st=namespace("jelly:stapler")

st.include(it:instance, 'class':descriptor.clazz, page:"config-advanced", optional:true)
f.advanced {
	events = instance?.events ?: [:]
	f.optionalBlock(name:"events", title:_("All Events"), negative:true, checked:events.isEmpty()) {
		f.nested {
			table(width:"100%") {
				Notification.ENDPOINTS.each { endpoint ->
					f.optionalBlock(name:"event", title:endpoint, checked:events[endpoint] != null) {
						f.invisibleEntry {
							input(type:"hidden", name:"endpoint", value:endpoint)
						}
						f.nested {
							table(width:"100%") {
								set('instance', events.get(endpoint))
								st.include(it:instance, 'class':descriptor.clazz, page:"config-advanced-endpoint", optional:true)
							}
						}
					}
				}
			}
		}
	}
}
