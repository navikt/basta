package no.nav.aura.basta;

import no.nav.aura.basta.spring.SpringConfig;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@ComponentScan(excludeFilters = {@Filter(Configuration.class), @Filter(SpringBootApplication.class) })
@PropertySource(value="file:${home}/database.properties", ignoreResourceNotFound=true)
@Import({SpringConfig.class})
public class BastaJettyRunner implements EmbeddedServletContainerCustomizer {

    private static int portNumber = 8086;

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        container.setPort(portNumber);
    }

    public static void main(String[] args) throws Exception {
        setEnvironmentSpecificProperties();
        SpringApplication springApp = new SpringApplication(BastaJettyRunner.class);
        springApp.setBannerMode(Banner.Mode.OFF);
        springApp.run(args);
    }

    protected static void setEnvironmentSpecificProperties() {
        System.setProperty("fasit_rest_api_url", "https://e34jbsl01655.devillo.no:8443/conf");
        System.setProperty("fasit_resources_v2_url", "http://localhost:8089/v2/resources");
        System.setProperty("fasit_applications_v2_url", "http://localhost:8089/v2/applications");
        System.setProperty("fasit_environments_v2_url", "http://localhost:8089/v2/environments");
        System.setProperty("fasit_nodes_v2_url", "http://localhost:8089/v2/nodes");
        System.setProperty("fasit_scopedresource_v2_url", "http://localhost:8089/v2/scopedresource");

        System.setProperty("srvbasta_username", "mjau");
        System.setProperty("srvbasta_password", "pstpst");

        System.setProperty("environment_class", "p");
        System.setProperty("basta_user_groups", "0000-GA-STDAPPS");
        System.setProperty("basta_operations_groups", "0000-GA-STDAPPS");
        // SUPERUSER ALL THE THINGS
        System.setProperty("basta_superuser_groups", "0000-GA-BASTA_SUPERUSER");
        System.setProperty("basta_prodoperations_groups", "0000-ga-env_config_S");

        System.setProperty("security_CA_test_url", "https://certenroll.test.local/certsrv/mscep/");
        System.setProperty("security_CA_test_username", "srvSCEP");
        System.setProperty("security_CA_test_password", "fjas");
        System.setProperty("security_CA_adeo_url", "adeourl");
        System.setProperty("security_CA_adeo_username", "");
        System.setProperty("security_CA_adeo_password", "");
        System.setProperty("security_CA_preprod_url", "preprodurl");
        System.setProperty("security_CA_preprod_username", "srvSCEP");
        System.setProperty("security_CA_preprod_password", "dilldall");
        System.setProperty("oem_url", "https://fjas.adeo.no");
        System.setProperty("oem_username", "eple");
        System.setProperty("oem_password", "banan");
        System.setProperty("bigip_url", "https://useriost.adeo.no");
        System.setProperty("bigip_username", "mango");
        System.setProperty("bigip_password", "chili");
        System.setProperty("BASTA_MQ_U_USERNAME", "srvAura");
        System.setProperty("BASTA_MQ_U_PASSWORD", "bacon");
        System.setProperty("BASTA_MQ_T_USERNAME", "srvAura");
        System.setProperty("BASTA_MQ_T_PASSWORD", "secret");
        System.setProperty("BASTA_MQ_Q_USERNAME", "srvAura");
        System.setProperty("BASTA_MQ_Q_PASSWORD", "secret");
        System.setProperty("BASTA_MQ_P_USERNAME", "srvAura");
        System.setProperty("BASTA_MQ_P_PASSWORD", "secret");

        System.setProperty("ldap_url", "ldaps://ldapgw.test.local");
        System.setProperty("ldap_domain", "test.local");
    }
}
