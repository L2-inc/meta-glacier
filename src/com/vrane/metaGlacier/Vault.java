/*
 * @(#)Vault.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier;

import com.amazonaws.services.glacier.model.CreateVaultRequest;
import com.amazonaws.services.glacier.model.DeleteVaultRequest;
import com.amazonaws.services.glacier.model.DescribeVaultRequest;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.GlacierJobDescription;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.JobParameters;
import com.amazonaws.services.glacier.model.ListJobsRequest;
import com.amazonaws.services.glacier.model.ListJobsResult;
import com.amazonaws.services.glacier.model.ResourceNotFoundException;
import com.vrane.metaGlacier.gui.GlacierFrame;
import com.vrane.metaGlacierSDK.APIException;
import com.vrane.metaGlacierSDK.SDKException;
import com.vrane.metaGlacierSDK.MVault;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * This represents an AWS vault object.
 * <b>create()</b>, <b>describe</b>, and <b>delete()</b> methods provide simple
 * interfaces to the underlying AWS API calls.
 *
 * @author K Z Win
 */
public class Vault extends MVault{

    private final static Logger LGR = Main.getLogger(Vault.class);
    private boolean doesNotExist = true;
    private boolean badVaultName = false;
    private final String name;
    private final String region;

    /**
     * Instantiates a vault object in the default region.
     *
     * @param _vault_name
     */
    public Vault(final String _vault_name){
        super(_vault_name, GlacierFrame.getAWSRegion());
        region = GlacierFrame.getAWSRegion();
        name = _vault_name;
    }

    /**
     * Instantiates a vault object given a vault name and region.
     *
     * @param _vault_name
     * @param _region
     */
    public Vault(final String _vault_name, final String _region){
        super(_vault_name, _region);
        region = _region;
        name = _vault_name;
    }

    /**
     * Creates a vault object at AWS.
     *
     * @return true only if there is no error.
     */
    public boolean create() {
        final CreateVaultRequest createVaultRequest = new CreateVaultRequest()
            .withVaultName(name);
        
        try {            
            GlacierFrame.getClient(region).createVault(createVaultRequest);
        } catch (java.lang.IllegalArgumentException e){
            LGR.severe("Bad vault name");
            badVaultName = true;
            LGR.log(Level.SEVERE, null, e);
            return false;
        } catch(Exception e){
            LGR.log(Level.SEVERE, null, e);
            return false;
        }
        
        if (!GlacierFrame.haveMetadataProvider()) {
            return true;
        }
        
        try {
            return withComputerId(GlacierFrame.getComputerId()).save();
        } catch (SDKException ex) {
            LGR.log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            LGR.log(Level.SEVERE, null, e);
        }
        return false;
    }

    /**
     * Check if vault name is bad after calling <code>create</code> or
     * <code>describe</code>.
     *
     * @return
     */
    public boolean vaultNameIsBad() { 
        return badVaultName;
    }

    /**
     * Check if vault exists.  Must call <code>describe</code> first.
     * 
     * @return true if vault exists in this region
     */
    public boolean exists(){
        return !doesNotExist;
    }

    /**
     * This calls <code>describeVault</code> at AWS and sets two flags:
     * <code>doesNotExist</code> and <code>badVaultName</code>.
     * Call this method before calling <code>exists</code> and
     * <code>vaultNameIsBad</code>
     */
    public void describe() {
        final DescribeVaultRequest describeVaultRequest
                = new DescribeVaultRequest().withVaultName(name);
        
        doesNotExist = true;
        badVaultName = false;
        try{
            GlacierFrame.getClient(region).describeVault(describeVaultRequest);
            doesNotExist = false;
        } catch (java.lang.IllegalArgumentException e){
            badVaultName = true;
        } catch (ResourceNotFoundException e){
            doesNotExist = true;
        } catch (Exception e){
            LGR.log(Level.SEVERE, null, e);
        }
    }

    /**
     * Deletes a vault from AWS.  If metadata credentials are available, then
     * it is marked as deleted there.
     *
     * @return true only if there is no error.
     */
    public boolean delete() {
        boolean success = true;
        final DeleteVaultRequest request = new DeleteVaultRequest()
            .withVaultName(name);
        
        try {
            success = false;
            if (GlacierFrame.haveMetadataProvider() && !isEmpty()) {
                int selectedButton = JOptionPane.showConfirmDialog(
                        Main.frame,
                        "Metadata provider says there are archives in this " +
                        "vault.  Proceed?\nChoosing yes will delete all data " +
                        "for this vault name from the metadata provider",
                        name,
                        JOptionPane.YES_NO_OPTION);            
                if (selectedButton != JOptionPane.YES_OPTION) {
                    return false;
                }
            }
            if (!ridContents()) {
                JOptionPane.showMessageDialog(null,
                        "Failed to delete archive metadata in this vault");
                return false;
            }
            success = true;
        } catch (SDKException | APIException ex) {
            LGR.log(Level.SEVERE, null, ex);
        }
        if (!success) {
            // here means that there is a metadata provider
            JOptionPane.showMessageDialog(null,
                    "Error deleting archive metadata in this vault");
            return false;
        }
        GlacierFrame.getClient(region).deleteVault(request);
        if (GlacierFrame.haveMetadataProvider()) {
            success = false;
            try {
                if (rid()) {
                    return true;
                }
            } catch (SDKException | APIException ex) {
                LGR.log(Level.SEVERE, null, ex);
            }
        }
        // reaching here means that there is a metadata provider
        JOptionPane.showMessageDialog(null,
                "Error deleting vault from metadata provider");
        return false;
    }

    /**
     * Gets a list of jobs attached to this vault.
     *
     * @return a list of <code>GlacierJobDescription</code>
     */
    public List<GlacierJobDescription> listJobs(){
        final ListJobsRequest ljr = new ListJobsRequest();
        final ListJobsResult ljres = GlacierFrame.getClient(region)
                .listJobs(ljr.withVaultName(name).withCompleted("true"));
        
        return ljres.getJobList();
    }

    /**
     * Gets job output result from AWS.
     *
     * @param jid job id string
     * @return
     */
    public GetJobOutputResult getJobOutput(final String jid){
        GetJobOutputRequest jor = new GetJobOutputRequest()
                        .withVaultName(name)
                        .withJobId(jid);
        return GlacierFrame.getClient(region).getJobOutput(jor);
    }

    /**
     * Start a job to get the inventory of a vault.
     *
     * @param sns_topic_string region appropriate SNS topic string
     * @return job id string if successful; null on error
     */
    public String makeVaultInventoryJob(final String sns_topic_string){
        final JobParameters jp = new JobParameters()
                .withType("inventory-retrieval")
                .withSNSTopic(sns_topic_string);
        final InitiateJobRequest initJobRequest = new InitiateJobRequest()
            .withVaultName(name)
            .withJobParameters(jp);
        InitiateJobResult JobResult = null;
        
        try{
            JobResult = GlacierFrame
                     .getClient(region).initiateJob(initJobRequest);
        } catch(Exception e){
            LGR.log(Level.SEVERE, null, e);           
            return null;
        }
        return JobResult.getJobId();
    }
}
