package io.jenkins.kubesphere.plugins.event.WebHookNotificationEndpoint

def f=namespace(lib.FormTagLib)

custom = instance?.custom
f.optionalBlock(name:"custom", title:_("customize"), checked:custom != null) {
	f.entry(title:_("URL")) {
		f.textbox(name:"url", value:custom?.url)
	}
}
