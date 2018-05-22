package io.pivotal.gcp.cloudsql;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class CloudsqlDataSourceAdapter {

    @Autowired
    private CloudsqlProperties cloudsqlProperties;

    @Bean
    public DataSource getCloudSqlDataSource() {

        log.info("Creating DataSource");

        /*
        String jdbcUrl = String.format(
            cloudsqlProperties.getJdbcUrl(),
            cloudsqlProperties.getDatabaseName(),
            cloudsqlProperties.getInstanceName());

        log.info(String.format("JDBC URL: %s", jdbcUrl));
        */


        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(cloudsqlProperties.getJdbcUrl());
        ds.setUsername(cloudsqlProperties.getUsername());
        ds.setPassword(cloudsqlProperties.getPassword());
        ds.setDriverClassName("com.mysql.jdbc.Driver");

        return ds;
    }
}
