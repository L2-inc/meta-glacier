/*
 * @(#)VaultInventory.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier;

import com.amazonaws.services.glacier.model.GetJobOutputResult;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * This class is used for retrieving a list of archives from a given vault.
 * It assumes that a vault-inventory job is ready.
 * @author K Z Win
 */
public class VaultInventory {
    
    private static ObjectMapper mapper = new ObjectMapper();
    private final static Logger LGR = Main.getLogger(VaultInventory.class);
    
    private Date inventoryDate;
    private JsonNode jobDescNode;
    private ArrayList<Archive> archives;
    private final String vaultName;
    
    public VaultInventory(final GetJobOutputResult g, final String n)
            throws Exception{
        vaultName = n;
        Exception error = null;
        JsonParser jpDesc = null;
        try {
            jpDesc = mapper.getJsonFactory().createJsonParser(g.getBody());
        } catch (JsonParseException ex) {
            error = ex;
            LGR.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            error = ex;
            LGR.log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            error = ex;
            LGR.log(Level.SEVERE, null, ex);
        }
        if (error != null) {
            throw error;
        }
        try {
            jobDescNode = mapper.readTree(jpDesc);
        } catch (JsonProcessingException ex) {
            LGR.log(Level.SEVERE, null, ex);
            error = ex;
        } catch (IOException ex) {
            LGR.log(Level.SEVERE, null, ex);
            error = ex;
        } catch (Exception ex) {
            LGR.log(Level.SEVERE, null, ex);
            error = ex;
        }
        if (error != null) {
            throw error;
        }
        SimpleDateFormat formatter
                = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            inventoryDate
                    = formatter.parse(jobDescNode.get("InventoryDate")
                                        .getTextValue());
        } catch (ParseException ex) {
            LGR.log(Level.SEVERE, null, ex);
            error = ex;
        }
        if (error != null) {
            throw error;
        }
    }
    
    public ArrayList<Archive> getArchives(){
        if (archives != null) {
            return archives;
        }
        archives = new ArrayList<>();
        final Iterator<JsonNode> j = jobDescNode.get("ArchiveList").getElements();
        LGR.finest("Now building archive list");
        while (j.hasNext()) {
            try {
                archives.add(new Archive(mapper.readValue(j.next(), Map.class))
                        .withVaultName(vaultName)
                        );
            } catch (IOException ex) {
                LGR.log(Level.SEVERE, null, ex);
                break;
            } catch (Exception e) {
                LGR.log(Level.SEVERE, null, e);
                break;
            }
        }
        return archives; 
    }
    
    public Date getInventoryDate(){
        return inventoryDate;
    }
    
}