/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.confluenceconfig;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.postgresql.util.PSQLException;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.sql.*;

/**
 * Created by yannick on 22/03/15.
 */
@Parameters(commandDescription = "Create database")
public class CreateDatabase {
    @Parameter(names = {"-a", "--address"}, description = "Database address", required = true)
    private String dbAddress;
    @Parameter(names = {"-p", "--port"}, description = "Database port")
    private int dbPort = 5432;
    @Parameter(names = "-n", description = "Master database name", required = true)
    private String masterDbName;
    @Parameter(names = "-au", description = "Db admin username", required = true)
    private String dbMasterUsername;
    @Parameter(names = "-ap", description = "Db admin password", required = true)
    private String dbMasterPassword;
    @Parameter(names = "-cu", description = "Confluence db role name")
    private String confluenceDbUsername = "confluence";
    @Parameter(names = "-cp", description = "Confluence db role password", required = true)
    private String confluenceDbPassword;
    @Parameter(names = "-cn", description = "Confluence database name")
    private String confluenceDbName = "confluence";
    @Parameter(names = {"-r", "--retry"}, description = "Number of seconds to keep trying to connect or resolve the server")
    private long retry = 600;
    @Parameter(names = {"-rg", "--retrygap"}, description = "Gap between retries (in seconds)")
    private int retryGap = 10;

    public void execute() throws SQLException {
        boolean retry = true;
        long timeout = System.currentTimeMillis() + (this.retry * 1000L);
        while (retry) {
            String url = "jdbc:postgresql://" + dbAddress + ":" + dbPort + "/" + masterDbName;
            System.out.println("Creating database on server " + url);
            try (Connection db = DriverManager.getConnection(url, dbMasterUsername, dbMasterPassword)) {
                try (Statement st = db.createStatement()) {
                    ResultSet rs = st.executeQuery("SELECT 1 AS result FROM pg_database WHERE datname='" + confluenceDbName + "'");
                    if (!rs.next()) {
                        st.execute("CREATE ROLE " + confluenceDbUsername + " WITH LOGIN PASSWORD '" + confluenceDbPassword + "' valid until 'infinity';");
                        st.execute("CREATE ROLE tmpusers NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOREPLICATION;");
                        st.execute("GRANT tmpusers TO " + dbMasterUsername + ";\n");
                        st.execute("GRANT " + confluenceDbUsername + " TO " + dbMasterUsername + ";\n");
                        st.execute("CREATE DATABASE " + confluenceDbName + " WITH ENCODING='UTF8' OWNER=" + confluenceDbUsername + " connection limit=-1;");
                        st.execute("REVOKE ADMIN OPTION FOR tmpusers FROM " + dbMasterUsername + ";\n");
                        st.execute("REVOKE ADMIN OPTION FOR tmpusers FROM " + confluenceDbUsername + ";\n");
                        st.execute("DROP ROLE tmpusers\n");
                        System.out.println("Database " + confluenceDbName + " created");
                    } else {
                        System.out.println("Database " + confluenceDbName + " already exists");
                    }
                }
                retry = false;
            } catch (PSQLException e) {
                Throwable cause = e.getCause();
                boolean transientError = cause != null && (cause instanceof UnknownHostException || cause instanceof ConnectException);
                if (!transientError || System.currentTimeMillis() > timeout) {
                    throw e;
                } else {
                    System.out.println("Unable to connect to server (retrying in " + retryGap + " seconds): " + cause);
                    try {
                        Thread.sleep(retryGap * 1000L);
                    } catch (InterruptedException e1) {
                        return;
                    }
                }
            }
        }
    }
}
