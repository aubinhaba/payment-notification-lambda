package com.bino.payment.notifier.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * Builds a HikariCP pool tuned for single-threaded Lambda invocations.
 * Short lifetimes ensure a frozen container doesn't resume with a stale TCP connection.
 */
public final class DataSourceFactory {

    private DataSourceFactory() {}

    public static DataSource create(String jdbcUrl, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(0);
        config.setConnectionTimeout(10_000);
        config.setIdleTimeout(10_000);
        config.setMaxLifetime(30_000);
        config.setKeepaliveTime(0); // desactive keepalive — maxLifetime suffisant pour Lambda
        config.setAutoCommit(true);
        config.setPoolName("stripe-notifier-pool");
        return new HikariDataSource(config);
    }
}
