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
@Parameters(commandDescription = "Set server id (server must be restarted after this)")
public class SetServerId {
    @Parameter(names = {"-d","--dir"}, description = "Confluence directory (where confluence.cfg.xml is located)",required = true)
    private String confluenceDir;
    @Parameter(names = {"-id","--serverid"}, description = "Server Id",required = true)
    private String serverId;

    public void execute() throws SetupException {
        File cfg = new File(confluenceDir, "confluence.cfg.xml");
        try {
            Document dom = XmlUtils.parse(cfg);
            Element existing = XPathUtils.evalXPathElement("confluence-configuration/properties/property[@name='confluence.setup.server.id']", dom);
            if( existing != null ) {
                String previousId = existing.getTextContent();
                if( previousId.trim().equals(serverId) ) {
                    System.out.println("Server id already set to "+serverId);
                    return;
                } else {
                    existing.setTextContent(serverId);
                    System.out.println("Server id was "+previousId+", changed to "+serverId);
                }
            } else {
                System.out.println("Server id set to "+serverId);
                Element propsEl = XPathUtils.evalXPathElement("confluence-configuration/properties", dom);
                Element sidEl = XmlUtils.createElement("property", propsEl, "name", "confluence.setup.server.id");
                sidEl.setTextContent(serverId);
            }
            try (FileWriter fw = new FileWriter(cfg)) {
                XmlUtils.serialize(dom, fw);
            }
            System.out.println("Don't forget to restart server");
        } catch (IOException | SAXException | XPathExpressionException e) {
            throw new SetupException("Unable to read configuration file " + cfg.getPath() + ": " + e.getMessage(), e);
        }
    }
}
