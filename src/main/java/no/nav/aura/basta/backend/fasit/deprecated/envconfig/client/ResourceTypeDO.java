package no.nav.aura.basta.backend.fasit.deprecated.envconfig.client;

import no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources.ApplicationCertificate;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources.BaseUrl;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources.Channel;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources.Credential;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources.DBTypeAware;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources.Ldap;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources.LoadBalancer;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources.Queue;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources.QueueManager;
import no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources.Resource;

@Deprecated
public enum ResourceTypeDO {
    DataSource,
    MSSQLDataSource,
    DB2DataSource,
    LDAP(Ldap.class),
    BaseUrl(BaseUrl.class),
    Channel(Channel.class),
    Credential(Credential.class),
    Certificate(ApplicationCertificate.class),
    DeploymentManager,
    QueueManager(QueueManager.class),
    Queue(Queue.class),
    LoadBalancer(LoadBalancer.class),
    LoadBalancerConfig;
    
    private Class<?> mapFromResourceClass;

    private ResourceTypeDO() {
    }

    private ResourceTypeDO(Class<?> mapFromResourceClass) {
        this.mapFromResourceClass = mapFromResourceClass;
    }

    public boolean isTypeFor(Resource resource) {
        return mapFromResourceClass != null && mapFromResourceClass.isAssignableFrom(resource.getClass());
    }

    public static ResourceTypeDO findTypeFor(Resource resource) {
        ResourceTypeDO resourceTypeDO = find(resource);
        if (resourceTypeDO != null) {
            return resourceTypeDO;
        }
        throw new IllegalArgumentException("Resource of type " + resource.getClass() + " has no mapped resourceType");
    }

    private static ResourceTypeDO find(Resource resource) {
        if (resource instanceof DBTypeAware) {
            DBTypeAware ds = (DBTypeAware) resource;
            if (ds.getType() == DBTypeAware.DBType.DB2) {
                return DB2DataSource;
            }
            return DataSource;
        }

        ResourceTypeDO[] values = ResourceTypeDO.values();
        for (ResourceTypeDO resourceType : values) {
            if (resourceType.isTypeFor(resource)) {
                return resourceType;
            }
        }
        return null;
    }

    public static boolean isDefinedFor(Resource resource) {
        return find(resource) != null;
    }

}
