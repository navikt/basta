package no.nav.aura.basta.rest;

import java.util.List;

import no.nav.aura.basta.persistence.ApplicationMappingType;
import no.nav.aura.envconfig.client.ApplicationDO;
import no.nav.aura.envconfig.client.ApplicationGroupDO;
import no.nav.aura.envconfig.client.FasitRestClient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class ApplicationMapping {
    private String name;
    private List<String> applications = Lists.newArrayList();

    @JsonIgnore
    private ApplicationMappingType mappingType = ApplicationMappingType.APPLICATION;

    private static Logger log = LoggerFactory.getLogger(ApplicationMapping.class);

    public ApplicationMapping() {
    }

    public ApplicationMapping(String name) {
        this.name = name;
    }

    public ApplicationMapping(String name, List<String> applications) {
        this(name);
        this.mappingType = ApplicationMappingType.APPLICATION_GROUP;
        this.applications = applications;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getApplications() {
        return applications;
    }

    public void setApplications(List<String> applications) {
        System.out.println("Create ApplicationMapping and applications are set " + applications + " setting mapping type tp application group");
        this.mappingType = ApplicationMappingType.APPLICATION_GROUP;
        this.applications = applications;
    }

    public ApplicationMappingType getMappingType() {
        return mappingType;
    }

    @JsonIgnore
    public boolean isMappedToApplicationGroup() {
        return mappingType.equals(ApplicationMappingType.APPLICATION_GROUP);
    }

    public boolean applicationsNeedsToBeFetchedFromFasit() {
        return isMappedToApplicationGroup() && (applications == null || applications.isEmpty());
    }

    private ImmutableList<String> fetchApplicationsForApplicationGroupFromFasit(FasitRestClient fasitClient) {
        ApplicationGroupDO applicationGroup = fasitClient.getApplicationGroup(name);
        return FluentIterable.from(applicationGroup.getApplications()).transform(
                new Function<ApplicationDO, String>() {
                    public String apply(ApplicationDO application) {
                        return application.getName();
                    }
                }).toSortedList(String.CASE_INSENSITIVE_ORDER);

    }

    public void loadApplicationsInApplicationGroup(FasitRestClient fasitClient) {
        if (!isMappedToApplicationGroup()) {
            throw new RuntimeException(String.format("ApplicationMapping for %s is not of type ApplicationGroup", name));
        }
        if(applicationsNeedsToBeFetchedFromFasit()) {
            log.debug(String.format("We need to fetch applications in groups for %s from Fasit, calling Fasit now...", name));
            applications = fetchApplicationsForApplicationGroupFromFasit(fasitClient);
        }
    }
}
