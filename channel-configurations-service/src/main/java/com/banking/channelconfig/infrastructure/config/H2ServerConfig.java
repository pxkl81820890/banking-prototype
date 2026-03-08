package com.banking.channelconfig.infrastructure.config;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

/**
 * Configuration to enable H2 TCP server for external database tool access.
 * This allows IntelliJ Database plugin or other tools to connect to the in-memory H2 database.
 */
@Configuration
public class H2ServerConfig {

    /**
     * Creates and starts H2 TCP server on port 9092.
     * This allows external tools to connect to the in-memory database.
     * 
     * Connection URL: jdbc:h2:tcp://localhost:9092/mem:channeldb
     * Username: sa
     * Password: (empty)
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2TcpServer() throws SQLException {
        return Server.createTcpServer(
            "-tcp",
            "-tcpAllowOthers",
            "-tcpPort", "9092"
        );
    }
}
