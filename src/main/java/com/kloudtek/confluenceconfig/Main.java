/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.confluenceconfig;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.kloudtek.util.UnexpectedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by yannick on 22/03/15.
 */
public class Main {
    public static final String VERSION;
    public static final String UPDATECONFIG = "updateconfig";
    public static final String CREATEDB = "createdb";
    public static final String CONFIGURE = "configure";

    static {
        InputStream pomProps = Main.class.getResourceAsStream("META-INF/maven/com.kloudtek.ktutils/ktutils-core/pom.properties");
        if (pomProps != null) {
            Properties p = new Properties();
            try {
                p.load(pomProps);
            } catch (IOException e) {
                throw new UnexpectedException(e);
            }
            VERSION = p.getProperty("version");
        } else {
            VERSION = "[dev]";
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Confluence Configuration v" + VERSION);
        Main main = new Main();
        JCommander jc = new JCommander(main);
        jc.setProgramName("confluencecfg");
        UpdateConfig updateConfig = new UpdateConfig();
        CreateDatabase createDatabase = new CreateDatabase();
        ConfigureConfluence configureConfluence = new ConfigureConfluence();
        jc.addCommand(UPDATECONFIG, updateConfig);
        jc.addCommand(CREATEDB, createDatabase);
        jc.addCommand(CONFIGURE, configureConfluence);
        try {
            jc.parse(args);
            String cmd = jc.getParsedCommand();
            if( cmd != null ) {
                switch (cmd) {
                    case UPDATECONFIG:
                        updateConfig.execute();
                        break;
                    case CREATEDB:
                        createDatabase.execute();
                        break;
                    case CONFIGURE:
                        configureConfluence.execute();
                        break;
                    default:
                        System.out.println("Invalid command: "+ cmd);
                        jc.usage();
                }
            } else {
                jc.usage();
            }
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            jc.usage();
        }
    }
}
