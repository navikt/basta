package no.nav.aura.basta.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OracleDataSourceCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return context.getEnvironment().getProperty("BASTADB_TYPE") == null || context.getEnvironment().getProperty
                ("BASTADB_TYPE").equalsIgnoreCase("oracle");
    }
}
