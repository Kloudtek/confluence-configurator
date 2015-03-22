/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.confluenceconfig;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * Created by yannick on 22/03/15.
 */
public class Main {
    public static final String SETSERVERID = "setserverid";
    public static final String CREATEDB = "createdb";
    public static final String CONFIGURE = "configure";

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        JCommander jc = new JCommander(main);
        jc.setProgramName("confluencecfg");
        SetServerId setServerId = new SetServerId();
        CreateDatabase createDatabase = new CreateDatabase();
        SetupConfluence setupConfluence = new SetupConfluence();
        jc.addCommand(SETSERVERID, setServerId);
        jc.addCommand(CREATEDB, createDatabase);
        jc.addCommand(CONFIGURE, setupConfluence);
        try {
            jc.parse(args);
            String cmd = jc.getParsedCommand();
            if( cmd != null ) {
                switch (cmd) {
                    case SETSERVERID:
                        setServerId.execute();
                        break;
                    case CREATEDB:
                        createDatabase.execute();
                        break;
                    case CONFIGURE:
                        setupConfluence.execute();
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
