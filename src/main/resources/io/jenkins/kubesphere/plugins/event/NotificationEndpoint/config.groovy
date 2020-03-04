/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jenkins.kubesphere.plugins.event.NotificationEndpoint

import io.jenkins.kubesphere.plugins.event.KubeSphereNotification
def f=namespace(lib.FormTagLib)
def st=namespace("jelly:stapler")

st.include(it:instance, 'class':descriptor.clazz, page:"config-advanced", optional:true)
f.advanced {
	events = instance?.events ?: [:]
	f.optionalBlock(name:"events", title:_("All Events"), negative:true, checked:events.isEmpty()) {
		f.nested {
			table(width:"100%") {
				KubeSphereNotification.ENDPOINTS.each { endpoint ->
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
