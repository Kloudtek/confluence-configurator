/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.confluenceconfig;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.kloudtek.util.xml.XPathUtils;
import com.kloudtek.util.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by yannick on 22/03/15.
 */
@Parameters(commandDescription = "Update server configuration file (server must be restarted after this)")
public class UpdateConfig {
    @Parameter(names = {"-d", "--dir"}, description = "Confluence directory (where confluence.cfg.xml is located)", required = true)
    private String confluenceDir;
    @Parameter(names = {"-id", "--serverid"}, description = "Server Id", required = true)
    private String serverId;
    @Parameter(names = {"-db", "--database-url"}, description = "Database url", required = true)
    private String dbUrl;

    public void execute() throws SetupException {
        File cfg = new File(confluenceDir, "confluence.cfg.xml");
        System.out.println("Updating configuration file " + cfg.getPath());
        try {
            if (cfg.exists()) {
                boolean changed = false;
                Document dom = XmlUtils.parse(cfg);
                Element existing = XPathUtils.evalXPathElement("confluence-configuration/properties/property[@name='confluence.setup.server.id']", dom);
                if (existing != null) {
                    String previousId = existing.getTextContent();
                    if (previousId.trim().equals(serverId)) {
                        System.out.println("Server id already set to " + serverId);
                    } else {
                        existing.setTextContent(serverId);
                        System.out.println("Server id was " + previousId + ", changed to " + serverId);
                        changed = true;
                    }
                } else {
                    System.out.println("Server id set to " + serverId);
                    Element propsEl = XPathUtils.evalXPathElement("confluence-configuration/properties", dom);
                    Element sidEl = XmlUtils.createElement("property", propsEl, "name", "confluence.setup.server.id");
                    sidEl.setTextContent(serverId);
                }
                Element currentDbUrl = XPathUtils.evalXPathElement("confluence-configuration/properties/property[@name='hibernate.connection.url']", dom);
                if (currentDbUrl != null) {
                    currentDbUrl.setTextContent(dbUrl);
                    System.out.println("Updated db url to " + dbUrl);
                    changed = true;
                } else {
                    System.out.println("Couldn't find hibernate.connection.url property, skipping");
                }
                if (changed) {
                    try (FileWriter os = new FileWriter(cfg)) {
                        XmlUtils.serialize(dom, os);
                    }
                    System.out.println("Don't forget to restart server");
                }
            } else {
                try (FileWriter os = new FileWriter(cfg)) {
                    os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "\n" +
                            "<confluence-configuration>\n" +
                            "  <setupStep>setupstart</setupStep>\n" +
                            "  <setupType>initial</setupType>\n" +
                            "  <buildNumber>0</buildNumber>\n" +
                            "  <properties>" +
                            "    <property name=\"confluence.setup.server.id\">" + serverId + "</property>" +
                            "  </properties>" +
                            "</confluence-configuration>");
                    System.out.println("Creating missing configuration file " + cfg.getPath());
                }
            }
        } catch (IOException | SAXException | XPathExpressionException e) {
            throw new SetupException("Unable to update configuration file " + cfg.getPath() + ": " + e.getMessage(), e);
        }
    }
}
