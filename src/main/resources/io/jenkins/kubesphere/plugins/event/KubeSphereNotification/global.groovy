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

package io.jenkins.kubesphere.plugins.event.KubeSphereNotification

import io.jenkins.kubesphere.plugins.event.NotificationEndpoint

def f=namespace(lib.FormTagLib)
def j=namespace("jelly:core")


f.section(title:_("KubeSphere Notifications")) {
    f.block {
        f.hetero_list(name:"endpoints", hasHeader:true, descriptors:NotificationEndpoint.all(), items:descriptor.getEndpoints(),
            addCaption:_("Add a new endpoint"), deleteCaption:_("Delete endpoint"))
    }
}
