package no.nav.aura.basta.backend.fasit.rest.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EnvironmentListPayload {
    private List<EnvironmentPayload> environment;

    public EnvironmentListPayload(List<EnvironmentPayload> environment ){
        this.environment = environment;
    }

    public  EnvironmentListPayload(EnvironmentPayload environment) {
        this.environment = new ArrayList<>();
        this.environment.add(environment);
    }

    public List<EnvironmentPayload> getApplications() {
        return environment;
    }

    public void setApplications(List<EnvironmentPayload> environment) {
        this.environment = environment;
    }

    public EnvironmentListPayload filter(Predicate<EnvironmentPayload> predicate) {
        List<EnvironmentPayload> filteredApplications = environment.stream().filter(predicate).collect(Collectors.toList());
        return new EnvironmentListPayload(filteredApplications);
    }

    public boolean isEmpty() {
        return environment == null || environment.isEmpty();
    }

    public  static  EnvironmentListPayload emptyEnvironmentList() {
        return new EnvironmentListPayload(new ArrayList<>());
    }
}
