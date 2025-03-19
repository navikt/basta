package no.nav.aura.basta.backend.fasit.deprecated.envconfig.resources;

public interface DBTypeAware {

     enum DBType {
        ORACLE, DB2
    }

    DBType getType();
}
