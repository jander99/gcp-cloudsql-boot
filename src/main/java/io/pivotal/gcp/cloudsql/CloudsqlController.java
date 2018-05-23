package io.pivotal.gcp.cloudsql;


import java.net.InetAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@RestController
@Slf4j
public class CloudsqlController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CloudsqlProperties cloudsqlProperties;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String jdbcUser;

    @Value("${spring.datasource.password}")
    private String jdbcPass;


    @RequestMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @RequestMapping("/dataSourceTest")
    public List<String> now() throws SQLException {

        log.debug(jdbcUrl + " " + jdbcUser + " " + jdbcPass);
        List<String> rv = new ArrayList<>();
        rv.add("Greetings from now");
        Connection connection = dataSource.getConnection();

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT NOW()");
            while (resultSet.next()) {
                rv.add(resultSet.getString(1));
            }
        }
        return rv;
    }

    @RequestMapping("/connectionTest")
    public List<String> now2() throws SQLException {

        List<String> rv = new ArrayList<>();
        rv.add("Greetings from now");
        /*
        Connection connection = DriverManager.getConnection(cloudsqlProperties.getJdbcUrl(),
                cloudsqlProperties.getUsername(),
                cloudsqlProperties.getPassword());


        log.debug(cloudsqlProperties.getJdbcUrl() + " " + cloudsqlProperties.getUsername() + " " + cloudsqlProperties.getPassword() + " " +
            InetAddress.getLoopbackAddress());
           */
        log.debug(jdbcUrl + " " + jdbcUser + " " + jdbcPass);
        Connection connection = DriverManager.getConnection(jdbcUrl,
            jdbcUser,
            jdbcPass);


        log.debug(jdbcUrl + " " + jdbcUser + " " + jdbcPass);


        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT NOW()");
            while (resultSet.next()) {
                rv.add(resultSet.getString(1));
            }
        }
        return rv;
    }
}
