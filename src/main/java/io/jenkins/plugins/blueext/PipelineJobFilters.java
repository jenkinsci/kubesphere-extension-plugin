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

package io.jenkins.plugins.blueext;

import com.google.common.base.Predicate;
import hudson.Extension;
import hudson.model.Item;
import io.jenkins.blueocean.service.embedded.rest.ContainerFilter;
import jenkins.branch.MultiBranchProject;

public class PipelineJobFilters {

    @Extension
    public static class MultiBranchFilter extends ContainerFilter {
        private final Predicate<Item> filter = job -> !isMultiBranch(job);

        @Override
        public String getName() {
            return "no-multi-branch-job";
        }
        @Override
        public Predicate<Item> getFilter() {
            return filter;
        }
    }

    public static boolean isMultiBranch(Item item){
        return item instanceof MultiBranchProject;
    }
}
