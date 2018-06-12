package no.nav.aura.basta;

import com.jayway.restassured.RestAssured;
import no.nav.aura.basta.repository.OrderRepository;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = {BastaJettyRunner.class})
public class ApplicationTest {

    @Autowired
    public TestRestTemplate testRestTemplate;

    @Autowired
    public OrderRepository orderRepository;

    @BeforeClass
    public static void setupProperties() {
        RestAssured.port = 8086;
        setEnvironmentSpecificProperties();
    }

    @Test
    public void verifyRestTemplate() throws Exception{
        assertNotNull(testRestTemplate);
    }

    private static void setEnvironmentSpecificProperties() {
        System.setProperty("ws_orchestrator_url", "https://someserver/vmware-vmo-webcontrol/webservice");
        System.setProperty("user_orchestrator_username", "orcname");
        System.setProperty("user_orchestrator_password", "secret");
        System.setProperty("rest_orchestrator_provision_url", "https://orcdev.adeo.no/vco/api/workflows/110abd83-455e-4aef-b141-fc4512bafec2/executions");
        System.setProperty("rest_orchestrator_decomission_url", "https://orcdev.adeo.no/vco/api/workflows/557dccf4-863a-49b3-b9f5-53a70f5b9fc2/executions");
        System.setProperty("rest_orchestrator_startstop_url", "https://orcdev.adeo.no/vco/api/workflows/f8f03155-fe07-436c-ad14-561158332130/executions");
        System.setProperty("rest_orchestrator_modify_url", "https://orcdev.adeo.no/vco/api/workflows/18af9950-e9cc-4485-954b-6c8d9fa42a68/executions");

        System.setProperty("ws.menandmice.url", "https://someserver/menandmice/webservice");
        System.setProperty("ws.menandmice.username", "mmName");
        System.setProperty("ws.menandmice.password", "mmSecret");

        System.setProperty("fasit_rest_api_url", "https://fasit.adeo.no/conf");
        System.setProperty("fasit_resources_v2_url", "http://localhost:8089/v2/resources");
        System.setProperty("fasit_applications_v2_url", "http://localhost:8089/v2/applications");
        System.setProperty("fasit_environments_v2_url", "http://localhost:8089/v2/environments");
        System.setProperty("fasit_nodes_v2_url", "http://localhost:8089/v2/nodes");
        System.setProperty("fasit_scopedresource_v2_url", "http://localhost:8089/v2/scopedresource");

        System.setProperty("srvbasta_username", "mjau");
        System.setProperty("srvbasta_password", "pstpst");

        System.setProperty("environment_class", "p");
        System.setProperty("basta_operations_groups", "0000-GA-STDAPPS");
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

        System.setProperty("flyway.enabled", "false");
        System.setProperty("BASTADB_TYPE", "h2");
        System.setProperty("BASTADB_URL", "jdbc:h2:mem:basta");
        System.setProperty("BASTADB_ONSHOSTS", "basta:6200");
        System.setProperty("BASTADB_USERNAME", "sa");
        System.setProperty("BASTADB_PASSWORD", "");
    }
}
