/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.confluenceconfig;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * Created by yannick on 22/03/15.
 */
public class Configurator {
    @Parameter(names = "-dir",description = "Confluence directory (where confluence.cfg.xml is located). If not specified serverId will not be performed")
    private String confluenceDir;
    @Parameter(names = "-sid",description = "Server Id ( -dir must have been specified )")
    private String serverId;
    @Parameter(names = "-surl",description = "Server URL",required = true)
    private String serverUrl;
    @Parameter(names = "-durl",description = "Database JDBC URL",required = true)
    private String dbUrl;
    @Parameter(names = "-dmu",description = "Database Master Username",required = true)
    private String dbMasterUsername;
    @Parameter(names = "-dmp",description = "Database Master Password",required = true)
    private String dbMasterPassword;
    @Parameter(names = "-dmp",description = "Database Confluence Username (if not specified the master username will be used, and the confluence user will not be created in the database)")
    private String confluenceDbUsername;
    @Parameter(names = "-dmp",description = "Database Confluence Password (if not specified the master password will be used, and the confluence user will not be created in the database)")
    private String confluenceDbPassword;
    @Parameter(names = "-dmn",description = "Database Confluence database name (if not specified the master database will be used, and the confluence user will not be created in the database)")
    private String confluenceDbName;

    private void runSetup() {

    }

    public static void main(String[] args) {
        Configurator configurator = new Configurator();
        JCommander jc = new JCommander(configurator);
        try {
            jc.parse(args);
            configurator.runSetup();
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            jc.usage();
        }
    }
}
