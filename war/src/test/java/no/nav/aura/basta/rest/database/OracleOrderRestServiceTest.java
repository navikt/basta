package no.nav.aura.basta.rest.database;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OracleOrderRestServiceTest {

    @Test
    public void createsCorrectDBName() {
        String dbName = OracleOrderRestService.createDBName("skikkeliglangtappnavnnnnnnnnnnnn", "tpr-u1");
        assertTrue("not more than 28 characters", dbName.length() <= 28);
        assertFalse("does not contain special characters oracle disapproves of", dbName.contains("-"));
    }

}