package no.nav.aura.basta;

import java.io.File;

import javax.sql.DataSource;

public class JunitBastaJettyRunner extends BastaJettyRunner {

    public JunitBastaJettyRunner() {
        super(0, new File(getProjectRoot(), "src/test/resources/junit-override-web.xml").getPath());
    }

    @Override
    protected DataSource createDatasource() {
        return createDataSource("h2", "jdbc:h2:mem:", "sa", "");
    }

}
