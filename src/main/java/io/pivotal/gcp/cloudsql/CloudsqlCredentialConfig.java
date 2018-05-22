package io.pivotal.gcp.cloudsql;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class CloudsqlCredentialConfig {

    private static final String VCAP_SERVICES = "VCAP_SERVICES";
    private static final String GOOGLE_MYSQL = "google-cloudsql-mysql";
    private static final String GOOGLE_APPLICATION_CREDENTIALS = "GOOGLE_APPLICATION_CREDENTIALS";
    private static final String GCP_CREDENTIALS_FILE_NAME = "GCP_credentials.json";

    private String instanceName;
    private String databaseName;
    private String username;
    private String password;
    private String jdbcUrl;

    @Bean
    public CloudsqlProperties getProps() {
        loadCredentials();
        return new CloudsqlProperties(this.username, this.password, this.instanceName, this.databaseName, this.jdbcUrl);
    }

    public void loadCredentials() {

        log.info("Loading Credentials.");
        JSONObject mysqlCredentials = getCredentialObject(GOOGLE_MYSQL);
        log.info(mysqlCredentials.toString());

        String projectId = mysqlCredentials.getString("ProjectId");
        String region = mysqlCredentials.getString("region");
        String instance = mysqlCredentials.getString("instance_name");

        this.instanceName = String.join(":", projectId, region, instance);
        this.databaseName = mysqlCredentials.getString("database_name");
        this.username = mysqlCredentials.getString("Username");
        this.password = mysqlCredentials.getString("Password");
        this.jdbcUrl = String.format(
                "jdbc:mysql://google/%s?cloudSqlInstance=%s&"
                        + "socketFactory=com.google.cloud.sql.mysql.SocketFactory",
                this.databaseName,
                this.instanceName);

        log.info(String.format("Username: %s, Password: %s, DB Name: %s, Instance Name: %s", username, password, databaseName, instanceName));

        String privateKeyData = mysqlCredentials.getString("PrivateKeyData");
        String credFile = writeCredentials(privateKeyData);
        log.info("Cred File: " + credFile);
        updateEnvironment(credFile);
    }

    private JSONObject getCredentialObject(String vcapKey) throws JSONException {

        String env = System.getenv(VCAP_SERVICES);
        JSONObject json = new JSONObject(env);

        JSONArray root = json.getJSONArray(vcapKey);
        JSONObject obj0 = root.getJSONObject(0);
        JSONObject credentials = obj0.getJSONObject("credentials");

        if(credentials != null) {
            return credentials;
        } else {
            throw new JSONException(String.format(
                    "Unable to find JSON Credentials for %s.",
                    vcapKey));
        }
    }

    private String writeCredentials(String privateKeyData) {

        String fileLocation = null;

        InputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(privateKeyData));
        File gcpJsonFile = new File(System.getProperty("java.io.tmpdir"), GCP_CREDENTIALS_FILE_NAME);
        try {
            FileCopyUtils.copy(in, new FileOutputStream(gcpJsonFile));
            fileLocation = gcpJsonFile.getPath();
        } catch (IOException e) {
            throw new RuntimeException("Failed while creating " + GCP_CREDENTIALS_FILE_NAME + " file", e);
        }

        return fileLocation;
    }

    private void updateEnvironment(String credFile) {

        Map<String, String> replEnv = new HashMap<>();
        replEnv.put(GOOGLE_APPLICATION_CREDENTIALS, credFile);

        try {
            Class<?>[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for (Class<?> cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    @SuppressWarnings("unchecked")
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(replEnv);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed while setting " + GOOGLE_APPLICATION_CREDENTIALS + " environment variable.", e);
        }
    }


}
