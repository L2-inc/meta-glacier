/*
 * @(#)AllVaults.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier;

import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;
import com.vrane.metaGlacier.gui.GlacierFrame;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This class exposes two methods to get a list of all vaults from AWS.
 * Use the default constructor without any argument to instantiate an object.
 * 
 * @author K Z Win
 */
public class AllVaults {
    private static HashMap<String, HashMap<String, Set<String>>> 
            downloadableArchives
            = new HashMap<>();
    private final static Logger LGR = Main.getLogger(AllVaults.class);

    private List<DescribeVaultOutput> allVaults;
    
    /**
     * Caches archive job id per region per vault.  Retrieve the last per vault
     * with <code>getDownloadJobIds</code>
     * 
     * @param vault_name
     * @param job_id 
     */
    public static void addDownloadableArchiveJobId(final String vault_name,
            final String job_id){
        final String current_region = GlacierFrame.getAWSRegion();
        
        if (!downloadableArchives.containsKey(current_region)) {
            downloadableArchives
                    .put(current_region, new HashMap<String, Set<String>>());
        }
        if (!downloadableArchives
                        .get(current_region).containsKey(vault_name)) {
            downloadableArchives
                    .get(current_region).put(vault_name, new HashSet<String>());
        }
        downloadableArchives.get(current_region).get(vault_name).add(job_id);
    }
    
    /**
     * Gets a set of job ids for downloadig archives.
     * 
     * @param vault_name
     * @return set of archive download job id
     */
    public static Set<String> getDownloadJobIds(final String vault_name){
        final String current_region = GlacierFrame.getAWSRegion();
        final Set<String> empty_set = new HashSet<>();
        
        if (!downloadableArchives.containsKey(current_region)) {
            return empty_set;
        }
        if (!downloadableArchives.get(current_region).containsKey(vault_name)) {
            return empty_set;
        }
        return downloadableArchives.get(current_region).get(vault_name);
    }
    
    /**
     * Returns a list of <b>DescribeVaultOutput</b> for the AWS account.
     * This always make a call to AWS.
     *
     * @return list of <code>DescribeVaultOutput>
     * @throws Exception
     */
    public List<DescribeVaultOutput> list() throws Exception{
        String marker = null;
        ListVaultsResult listVaultsResult = null;
        
        do {
            LGR.fine("getting vaults up to 100");
            ListVaultsRequest listVaultsRequest
                    = new ListVaultsRequest()
                        .withLimit("100").withMarker(marker);       
            listVaultsResult
                    = GlacierFrame.getClient().listVaults(listVaultsRequest);
            LGR.fine("got list");
            List<DescribeVaultOutput> vaultList
                    = listVaultsResult.getVaultList();
            marker = listVaultsResult.getMarker();
            if (allVaults == null) {
                allVaults = vaultList;
                continue;
            }
            allVaults.addAll(vaultList);
        } while (marker !=null);
        return allVaults;
    }

    /**
     * Returns a set of vault names.
     * This method makes use of data cached by <b>list()</b> and call that
     * method first internally.
     * 
     * @return a list of vault names
     * @throws Exception 
     */
    public Set<String> listNames() throws Exception{
        list();
        String [] vaultNameArray = new String[allVaults.size()];
        short i = 0;
        
        for (final DescribeVaultOutput dvo: allVaults) {
            vaultNameArray[i++] = dvo.getVaultName();
        }
        return new HashSet<>(Arrays.asList(vaultNameArray));
    }    
}
