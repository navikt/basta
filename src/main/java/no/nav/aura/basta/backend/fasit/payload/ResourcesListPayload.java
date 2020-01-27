package no.nav.aura.basta.backend.fasit.payload;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ResourcesListPayload {
    private List<ResourcePayload> resources = new ArrayList<>();

    public ResourcesListPayload(List<ResourcePayload> resources ){
        this.resources = resources;
    }

    public  ResourcesListPayload(ResourcePayload resource) {
        this.resources = new ArrayList<>();
        this.resources.add(resource);
    }

    public List<ResourcePayload> getResources() {
        return resources;
    }

    public void setResources(List<ResourcePayload> resources) {
        this.resources = resources;
    }

    public ResourcesListPayload filter(Predicate<ResourcePayload> predicate) {
        List<ResourcePayload> filteredResources = resources.stream().filter(predicate).collect(Collectors.toList());
        return new ResourcesListPayload(filteredResources);
    }

    public boolean isEmpty() {
        return resources.equals(null) || resources.isEmpty();
    }

    public  static  ResourcesListPayload emptyResourcesList() {
        return new ResourcesListPayload(new ArrayList<>());
    }
}
