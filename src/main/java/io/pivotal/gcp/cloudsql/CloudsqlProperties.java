package io.pivotal.gcp.cloudsql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CloudsqlProperties {

    private String username;
    private String password;
    private String instanceName;
    private String databaseName;
    private String jdbcUrl;
}
