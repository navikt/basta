package no.nav.aura.basta.rest;

import no.nav.aura.basta.persistence.ApplicationMapping;

import com.google.common.collect.Lists;

import java.util.ArrayList;

public class Application {
    private String name;
    private ArrayList<String> applications = Lists.newArrayList();
    private ApplicationMapping mappingType = ApplicationMapping.APPLICATION;


    public Application() {
    }

    public Application(String applicationName) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getApplications() {
        return applications;
    }

    public void setApplications(String[] applications) {
        this.mappingType = ApplicationMapping.APPLICATION_GROUP;
        this.applications = Lists.newArrayList(applications);
    }

    public ApplicationMapping getMappingType() {
        return mappingType;
    }
}
