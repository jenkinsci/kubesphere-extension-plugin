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
