package no.nav.aura.basta.backend;

import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class PostgreSQLClient {
    private static final Map<String,String> DB_SERVERS;
    static {
        DB_SERVERS = new HashMap<>();
        DB_SERVERS.put("u-fss","E25DBVL001.utvikling.local"); // postgres_api_fss_u.adeo.no
        DB_SERVERS.put("t-fss", "D26DBVL007.test.local"); // postgres_api_fss_t.adeo.no
        DB_SERVERS.put("q-fss", "B27DBVL003.preprod.local"); // postgres_api_fss_q.adeo.no
        DB_SERVERS.put("p-fss", "A01DBVL005.adeo.no"); // postgres_api_fss_p.adeo.no

        DB_SERVERS.put("u-iapp", "postgres_api_iapp_u.adeo.no");
        DB_SERVERS.put("t-iapp", "postgres_api_iapp_t.adeo.no");
        DB_SERVERS.put("q-iapp", "B27DBVL002.preprod.local"); // postgres_api_iapp_q.adeo.no
        DB_SERVERS.put("p-iapp", "A01DBVL004.adeo.no"); // postgres_api_iapp_p.adeo.no

        DB_SERVERS.put("u-sbs","postgres_api_sbs_u.adeo.no");
        DB_SERVERS.put("t-sbs", "postgres_api_sbs_t.adeo.no");
        DB_SERVERS.put("q-sbs", "postgres_api_sbs_q.adeo.no");
        DB_SERVERS.put("p-sbs", "postgres_api_sbs_p.adeo.no");
    }

    public CreateDBResponse createDatabase(String dbName, String environmentClass, String zoneName) {
        String key = environmentClass + "-" + zoneName;
        if (!DB_SERVERS.containsKey(key)) {
            throw new RuntimeException("Environment not supported: " + key);
        }

        String url = "http://" + DB_SERVERS.get(key) + ":5000/api/v1/create/";

        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            String payload = "db_name=" + URLEncoder.encode(dbName, "UTF-8");
            connection.getOutputStream().write(payload.getBytes());

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            for (String line = null; (line = reader.readLine()) != null;) {
                builder.append(line).append("\n");
            }
            reader.close();
            connection.disconnect();

            if (connection.getResponseCode() >= 400) {
                throw new RuntimeException("Request to the PostgreSQL database automation API failed with status code " + connection.getResponseCode() + ": " + builder.toString());
            } else {
                System.out.println("Result: " + builder.toString());
                return new Gson().fromJson(builder.toString(), CreateDBResponse.class);
            }
        } catch (Exception e) {
            throw new RuntimeException("Request to the PostgreSQL database automation API failed (" + e.getClass().getName() + ")", e);
        }
    }

    public static class CreateDBResponse {
        public String db_name;
        public String username;
        public String password;
        public String encoding;
        public String server;
        public String version;
    }
}
