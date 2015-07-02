package no.nav.aura.basta.it;

import no.nav.aura.basta.JettyTest;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class BastaProtractorIT extends JettyTest {

    @Test
    public void runProtractorE2ETests() throws Exception {
        // TODO ikke være avhengig av global protractor npm package ++
        // TODO windows og linux støtte på execute
        // TODO fikse mocks for junit jettyTester eller bruke standaloneconfig

        String baseurl = "http://localhost:" + this.jetty.getPort();
        ProcessBuilder builder = new ProcessBuilder()
                .command("protractor.cmd", "src/test/js/conf.js")
                .inheritIO();

        builder.environment().put("TEST_BASEURL", baseurl);

        final Process process = builder.start();
        process.waitFor();
        int exitValue = process.exitValue();
        Assert.assertThat("protractor exitvalue", exitValue, Matchers.is(0));
    }

}
