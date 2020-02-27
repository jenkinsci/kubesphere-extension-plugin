package io.jenkins.kubesphere.plugins.event.NotificationEndpoint

def f=namespace(lib.FormTagLib)
def st=namespace("jelly:stapler")

st.include(it:instance, 'class':descriptor.clazz, page:"global-advanced", optional:true)
f.advanced {
	events = instance?.events ?: [:]
	f.optionalBlock(name:"events", title:_("All Events"), negative:true, checked:events.isEmpty()) {
		f.nested {
			table(width:"100%") {
				my.ENDPOINTS.each { endpoint ->
					f.optionalBlock(name:"event", title:endpoint, checked:events[endpoint] != null) {
						f.invisibleEntry {
							input(type:"hidden", name:"endpoint", value:endpoint)
						}
						f.nested {
							table(width:"100%") {
								set('instance', events.get(endpoint))
								st.include(it:instance, 'class':descriptor.clazz, page:"global-advanced-endpoint", optional:true)
							}
						}
					}
				}
			}
		}
	}
}
