package no.nav.aura.basta.backend.fasit.rest.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ApplicationListPayload {
    private List<ApplicationPayload> application;

    public ApplicationListPayload(List<ApplicationPayload> application ){
        this.application = application;
    }

    public  ApplicationListPayload(ApplicationPayload application) {
        this.application = new ArrayList<>();
        this.application.add(application);
    }

    public List<ApplicationPayload> getApplications() {
        return application;
    }

    public void setApplications(List<ApplicationPayload> application) {
        this.application = application;
    }

    public ApplicationListPayload filter(Predicate<ApplicationPayload> predicate) {
        List<ApplicationPayload> filteredApplications = application.stream().filter(predicate).collect(Collectors.toList());
        return new ApplicationListPayload(filteredApplications);
    }

    public boolean isEmpty() {
        return application == null || application.isEmpty();
    }

    public  static  ApplicationListPayload emptyApplicationList() {
        return new ApplicationListPayload(new ArrayList<>());
    }
}
