/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.confluenceconfig;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.kloudtek.util.xml.XPathUtils;
import com.kloudtek.util.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by yannick on 22/03/15.
 */
@Parameters(commandDescription = "Configure confluence")
public class SetupConfluence {
    @Parameter(names = {"-sid","--serverid"}, description = "Server Id")
    private String serverId;
    @Parameter(names = {"-l","--license"}, description = "Server License", required = true)
    private String serverLicense;
    @Parameter(names = {"-u","--url"}, description = "Server URL", required = true)
    private String serverUrl;
    @Parameter(names = "-da", description = "Database address", required = true)
    private String dbAddress;
    @Parameter(names = "-dpt", description = "Database port")
    private int dbPort = 5432;
    @Parameter(names = "-du", description = "Database username", required = true)
    private String dbUser;
    @Parameter(names = "-dp", description = "Database password", required = true)
    private String dbPassword;
    @Parameter(names = "-dn", description = "Database name", required = true)
    private String dbName;
    @Parameter(names = "-au", description = "Confluence admin username",required = true)
    private String adminUsername;
    @Parameter(names = "-an", description = "Confluence admin name",required = true)
    private String adminName;
    @Parameter(names = "-ae", description = "Confluence admin email address",required = true)
    private String adminEmail;
    @Parameter(names = "-ap", description = "Confluence admin password",required = true)
    private String adminPassword;

    public void execute() throws IOException, SetupException {
        Logger.getLogger("").setLevel(Level.WARNING);
        WebClient webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setTimeout(600000);
        HtmlPage page = webClient.getPage(serverUrl);
        boolean inprogress = true;
        while (inprogress) {
            String url = page.getUrl().toString();
            if (url.contains("setup/setupstart.action")) {
                System.out.println("Setting up confluence");
                page = ((HtmlSubmitInput) page.getElementByName("setupTypeCustom")).click();
                checkExpectedPage(page, "setup/setuplicense.action");
            } else if (url.contains("setup/setuplicense.action")) {
                System.out.println("Setting up license");
                String foundServerId = page.getElementById("serverId").getAttribute("value");
                if (serverId != null && !foundServerId.equals(serverId)) {
                    throw new SetupException("server id " + foundServerId + " does not match expected " + serverId);
                }
                ((HtmlTextArea) page.getElementById("licenseString")).type(serverLicense);
                page = ((HtmlSubmitInput) page.getElementByName("setupTypeCustom")).click();
                checkExpectedPage(page, "setup/setupdbchoice-start.action");
            } else if (url.contains("setup/setupdbchoice-start.action")) {
                System.out.println("Selecting database type");
                ((HtmlSelect)page.getElementByName("dbChoiceSelect")).setSelectedAttribute("postgresql", true);
                page = ((HtmlSubmitInput)page.getElementById("select-db")).click();
                checkExpectedPage(page, "setup/setupdb-start.action");
            } else if (url.contains("setup/setupdb-start.action")) {
                System.out.println("Configure database");
                page = ((HtmlSubmitInput) page.getByXPath("//input[@value='Direct JDBC']").get(0)).click();
                checkExpectedPage(page, "setup/setupstandarddb-start.action");
            } else if (url.contains("setup/setupstandarddb-start.action")) {
                System.out.println("Configure database settings");
                page.getElementById("dbConfigInfo.databaseUrl").setAttribute("value", "jdbc:postgresql://" + dbAddress + ":"+dbPort+ "/" + dbName);
                page.getElementById("dbConfigInfo.userName").setAttribute("value", dbUser);
                page.getElementById("dbConfigInfo.password").setAttribute("value", dbPassword);
                page = ((HtmlSubmitInput)page.getElementById("edit")).click();
                checkExpectedPage(page, "setup/setupdata-start.action");
            } else if (url.contains("setup/setupdata-start.action")) {
                page = ((HtmlSubmitInput) page.getElementByName("dbchoiceSelect")).click();
                checkExpectedPage(page, "setup/setupusermanagementchoice-start.action");
            } else if (url.contains("setup/setupusermanagementchoice-start.action")) {
                page = ((HtmlSubmitInput) page.getElementByName("internal")).click();
                checkExpectedPage(page, "setup/setupadministrator-start.action");
            } else if (url.contains("setup/setupadministrator-start.action")) {
                page.getElementById("username").setAttribute("value", adminUsername);
                page.getElementById("fullName").setAttribute("value", adminName);
                page.getElementById("email").setAttribute("value", adminEmail);
                page.getElementById("password").setAttribute("value", adminPassword);
                page.getElementById("confirm").setAttribute("value", adminPassword);
                page = ((HtmlSubmitInput) page.getElementByName("edit")).click();
                checkExpectedPage(page,"setup/finishsetup.action");
                System.out.println("Setup completed (url=" + url + ")");
                inprogress = false;
            } else {
                System.out.println("Server already setup (redirection url=" + url + ")");
                inprogress = false;
            }
        }
    }

    private void checkExpectedPage(HtmlPage page, String path) throws SetupException {
        if (!page.getUrl().toString().contains(path)) {
            failSetup(page);
        }
    }

    private void failSetup(HtmlPage page) throws SetupException {
        System.out.println("Setup failed");
        System.out.println(page.asXml());
        throw new SetupException("Setup failed");
    }
}
