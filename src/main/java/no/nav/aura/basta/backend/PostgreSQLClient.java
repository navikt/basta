package no.nav.aura.basta.backend;

import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.io.BufferedReader;
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
        DB_SERVERS.put("u-fss","E25DBVL001.utvikling.local");
        DB_SERVERS.put("t-fss", "D26DBVL007.test.local");
        DB_SERVERS.put("q-fss", "B27DBVL003.preprod.local");
        DB_SERVERS.put("p-fss", "A01DBVL005.adeo.no");
        DB_SERVERS.put("q-iapp", "B27DBVL002.preprod.local");
        DB_SERVERS.put("p-iapp", "A01DBVL004.adeo.no");
    }

    public CreateDBResponse createDatabase(String dbName, String environmentClass, String zoneName) {
        String key = environmentClass + "-" + zoneName;
        if (!DB_SERVERS.containsKey(key)) {
            throw new RuntimeException("Environment not supported: " + key);
        }

        String url = "http://" + DB_SERVERS.get(key) + ":5000/api/v1/create/";

        try {
            HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
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
            System.out.println("Result: "+builder.toString());
            CreateDBResponse response = new Gson().fromJson(builder.toString(), CreateDBResponse.class);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Request to the PostgreSQL database automation API failed", e);
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
