package com.example.VirtualMessenger.Configs;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Configuration
public class R2DBC {

    @Bean
    public ConnectionFactory connectionFactoryConfig() {
        ConnectionFactoryOptions options = builder()
                .option(DRIVER, "mysql")
                .option(HOST, "localhost")
                .option(PORT, 3306)
                .option(USER, "root")
                .option(PASSWORD, "20090620")
                .option(DATABASE, "virtualmessenger")
                .option(SSL, false)
                .build();
        ConnectionFactory connectionFactoryop = ConnectionFactories.get(options);
        return connectionFactoryop;
    }

    @Bean
    public DatabaseClient databaseClient(@Qualifier("connectionFactoryConfig") ConnectionFactory connectionFactory) {
        return DatabaseClient.create(connectionFactory);
    }
}
