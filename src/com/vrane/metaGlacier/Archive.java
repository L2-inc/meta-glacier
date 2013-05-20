/*
 * @(#)Archive.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.glacier.TreeHashGenerator;
import com.amazonaws.services.glacier.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.glacier.model.DeleteArchiveRequest;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.glacier.model.JobParameters;
import com.amazonaws.services.glacier.model.RequestTimeoutException;
import com.amazonaws.services.glacier.model.UploadMultipartPartRequest;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.util.BinaryUtils;
import com.vrane.metaGlacier.gui.GlacierFrame;
import com.vrane.metaGlacierSDK.APIException;
import com.vrane.metaGlacierSDK.SDKException;
import com.vrane.metaGlacierSDK.BasicFile;
import com.vrane.metaGlacierSDK.MArchive;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * This class represents an glacier Archive object.  
 * Important methods it exposes are <b>delete</b> and <b>upload</b> which
 * makes AWS calls. Various ways of instantiating are provided.
 * 
 * @author K Z Win
 */
public class Archive {
    private final static Logger LGR = Main.getLogger(Archive.class);
    private final static int BYTE_BUFFER = (int) HumanBytes.MEGA;
    private final static int MAX_ARCHIVE_DESCRIPTION_LENGTH
            = (int) HumanBytes.KILO;
    private final static long UPDATE_RATE_INTERVAL = 3000;
    private final static int RETRY_SEC = 120;
    
    private String archiveId;
    private long size;
    private String vaultName;
    private String localPath;
    private String descriptionString;
    private String downloadJobId;
    private String aws_create_time;
    private String sha256treehash;
    private int granularity;
    private long startEpoch;
    private long finishEpoch;
    private boolean deletedInAWS = false;
    private boolean fromAWSInventory = false;
    private String deleteError;
    private ArrayList<String> uploadedFileList;
    private FileProgressReporter progress_reporter;
    private String region;
    
    /**
     * This is created from vault inventory data.
     *
     * @param m
     */
    public Archive(Map<String, Object> m){
        archiveId = (String) m.get("ArchiveId");
        descriptionString = (String) m.get("ArchiveDescription");
        aws_create_time = (String) m.get("CreationDate");
        sha256treehash = (String) m.get("SHA256TreeHash");
        fromAWSInventory = true;
        
        /* XXX FIXME problem directly going to long */
        size = (long) (Integer) m.get("Size");
    }    
    
    /** 
     * This is for uploading to AWS.
     *
     * @param _vault_name
     * @param descString
     * @param _region
     * @param path
     */
    public Archive(final String _vault_name, final String descString,
            final String path, final String _region){
        if (descString == null ||
                descString.length() <= MAX_ARCHIVE_DESCRIPTION_LENGTH) {
            descriptionString = descString;
            vaultName = _vault_name;
            localPath = path;
            region = _region;
            size = new File(path).length();
            return;
        }
        LGR.warning("Description string is longer than 1024 characters.  "
                + "See AWS documentation");
    }
    
    /**
     * This is constructed for getting a download job.
     *
     * @param _vault_name
     * @param _archive_id
     * @param _region
     */
    public Archive(final String _vault_name, final String _archive_id,
            final String _region){
        region = _region;
        vaultName = _vault_name;
        archiveId = _archive_id;
    }
    
    /**
     * This is constructed with data returned from metadata provider.
     *
     * @param ma
     */
    public Archive(final MArchive ma){
        vaultName = ma.getVaultName();
        archiveId = ma.getArchiveId();
        size = ma.getSize();
        region = ma.getRegion();
        deletedInAWS = ma.isDeleted();
        descriptionString = ma.getDescription();
    }

    /**
     *Gets the region that this archive is located.
     *
     * @return string such as 'us-east-2', 'eu-west-1'.
     */
    public String getRegion(){
        return region == null ? GlacierFrame.getAWSRegion() : region;
    }
    /**
     * Sets archive download job id.
     * 
     * @param job_id
     * @return this object
     */
    public Archive withJobId(final String job_id){
        downloadJobId = job_id;
        return this;
    }
    
    private ArchiveTransferManager getATM(){
        return new ArchiveTransferManager(GlacierFrame.getClient(), Main.frame);
    }
    
    /**
     * Indicates if this object is likely to be in AWS inventory.
     * 
     * @return true if the archive is in AWS inventory. 
     */
    public boolean isInAWSInventory() {
        return fromAWSInventory;
    }

    //<editor-fold defaultstate="collapsed" desc="upload related stuff">
    /**
     * Upload archive to AWS.
     * Also upload metadata if an account is set-up.
     * 
     * @return true if upload is successful.
     */
    public synchronized boolean upload(){
        boolean error = true;
        
        startEpoch = System.currentTimeMillis();
        if (granularity > 0) {
            progress_reporter.setFilePosition(0);
            try {
                final String uploadId = initiateMultipartUpload();
                final String checksum = uploadParts(uploadId);
                if (checksum == null) {
                    LGR.warning("failed to get checksum");
                    return false;
                }
                archiveId = CompleteMultiPartUpload(uploadId, checksum);
                error = false;
            } catch (AmazonServiceException ex) {
                LGR.log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException | IOException ex) {
                LGR.log(Level.SEVERE, null, ex);
            } catch (AmazonClientException ex) {
                LGR.log(Level.SEVERE, null, ex);
            }
        } else {
            // this branch not used currently
            progress_reporter.setFilePosition(-1);
            try {
                archiveId = getATM().upload(vaultName, 
                        descriptionString, new File(localPath)).getArchiveId();
                error = false;
            } catch (AmazonServiceException ex) {
                LGR.log(Level.SEVERE, null, ex);
            } catch (AmazonClientException | FileNotFoundException ex) {
                LGR.log(Level.SEVERE, null, ex);
            }
            progress_reporter.setFilePosition(size);
        }
        if (error) {
            LGR.warning("failed to upload to AWS");
            return false;
        }
        finishEpoch = System.currentTimeMillis();
        return uploadToMetadataProvider();
    }

    /**
     * Return number of seconds to do the upload.
     * @return the number of milliseconds to upload this archive
     */
    public long getTimeToUpload(){
        return (finishEpoch - startEpoch);
    }
    
    private String initiateMultipartUpload() {
        InitiateMultipartUploadRequest request
                = new InitiateMultipartUploadRequest()
                    .withVaultName(vaultName)
                    .withArchiveDescription(descriptionString)
                    .withPartSize(granularity + "");            

        LGR.log(Level.INFO, "uploading to vault {1} in region {0}", 
                new Object[]{region, vaultName});
        return GlacierFrame.getClient(region)
                .initiateMultipartUpload(request).getUploadId();
    }

    private String uploadParts(String uploadId)
            throws AmazonServiceException, NoSuchAlgorithmException,
            AmazonClientException, IOException {
        int filePosition = 0;
        long currentPosition = 0;
        byte[] buffer = new byte[(int)granularity];
        List<byte[]> binaryChecksums = new LinkedList<>();
        final File file = new File(localPath);
        String contentRange;
        int read = 0;
        
        try (FileInputStream fileToUpload = new FileInputStream(file)) {
            while (currentPosition < file.length()) {
                read = fileToUpload.read(buffer, filePosition, buffer.length);
                if (read == -1) {
                    break;
                }
                if (Thread.currentThread().isInterrupted()) {
                    LGR.warning("upload job is interrupted.");
                    return null;
                }
                LGR.log(Level.FINE, "reading position {0} for file {1}",
                        new Object[]{currentPosition, localPath});
                byte[] bytesRead = Arrays.copyOf(buffer, read);
                contentRange = String.format("bytes %s-%s/*", currentPosition, 
                        currentPosition + read - 1);
                String checksum = TreeHashGenerator.calculateTreeHash(
                        new ByteArrayInputStream(bytesRead));
                byte[] binaryChecksum = BinaryUtils.fromHex(checksum);
                binaryChecksums.add(binaryChecksum);
                            
                //Upload part.
                UploadMultipartPartRequest partRequest
                        = new UploadMultipartPartRequest()
                        .withVaultName(vaultName)
                        .withBody(new ByteArrayInputStream(bytesRead))
                        .withChecksum(checksum)
                        .withRange(contentRange)
                        .withUploadId(uploadId);
                try {
                    GlacierFrame.getClient(region).uploadMultipartPart(partRequest);
                } catch (RequestTimeoutException e){
                    LGR.log(Level.SEVERE,
                            "Request time out at {0}. Retrying in {1} s",
                            new Object[]{
                            HumanBytes.convert(currentPosition), RETRY_SEC});
                    LGR.log(Level.SEVERE, null, e);
                    try {
                        Thread.sleep(RETRY_SEC * 1000);
                    } catch (InterruptedException ex) {
                        LGR.log(Level.SEVERE, null, ex);
                        return null;
                    }
                    try {
                        GlacierFrame.getClient(region)
                                .uploadMultipartPart(partRequest);
                    } catch (RequestTimeoutException ex) {
                        LGR.log(Level.SEVERE, null, ex);
                        LGR.severe("2nd time out.  Giving up");
                        return null;
                    }
                } catch (Exception e) {
                    LGR.log(Level.SEVERE, null, e);
                    LGR.severe("Unanticipated error.  Giving up.");
                    return null;
                }
                if (Thread.currentThread().isInterrupted()) {
                    LGR.warning("upload job is interrupted.");
                    return null;
                }
                currentPosition = currentPosition + read;
                progress_reporter.setFilePosition(currentPosition);
            }
        }
        String checksum = TreeHashGenerator.calculateTreeHash(binaryChecksums);
        return checksum;
    }

    private String CompleteMultiPartUpload(String uploadId, String checksum)
            throws NoSuchAlgorithmException, IOException {
        File file = new File(localPath);
        CompleteMultipartUploadRequest compRequest
                = new CompleteMultipartUploadRequest()
                .withVaultName(vaultName)
                .withUploadId(uploadId)
                .withChecksum(checksum)
                .withArchiveSize(String.valueOf(file.length()));
        
        if (Thread.currentThread().isInterrupted()) {
            LGR.warning("upload job is interrupted.");
            return null;
        }
        return GlacierFrame.getClient(region).completeMultipartUpload(compRequest)
                .getArchiveId();
    }        

    private boolean uploadToMetadataProvider() {
        if (!GlacierFrame.haveMetadataProvider()) {
            return true;
        }
        boolean save_failed = true;
        Vault mv = new Vault(vaultName);
        MArchive mar = new MArchive(mv, getArchiveId())
                .withSize(getSize())
                .withComputerId(GlacierFrame.getComputerId())
                .withDescription(getDescription());
        
        if (uploadedFileList.size() > 1) {
            mar.addContent(new BasicFile(localPath));
        }
        for (final String oldPath: uploadedFileList) {
            final BasicFile oldFile = new BasicFile(oldPath);
            if (uploadedFileList.size() == 1 && !oldPath.equals(localPath)) {
                mar.addContent(new BasicFile(localPath));
            }
            mar.addContent(oldFile);
        }
        try {
            mar.save(Main.frame.canSavePhotoMetadata());
            save_failed = false;
        } catch (SDKException | APIException ex) {
            LGR.log(Level.SEVERE, null, ex);
        }
        if (save_failed) {
            JOptionPane.showMessageDialog(null, "Cannot save archive metadata");
            return false;
        }
        return true;
    }
    //</editor-fold>

    /**
     *Sets the progress reporter.  This reporter is normally a component
     * containing <code>JProgressBar</code>
     * 
     * @param _reporter
     * @return this object
     */
    public Archive withProgressReporter(final FileProgressReporter _reporter) {
        progress_reporter = _reporter;
        return this;
    }

    //<editor-fold defaultstate="collapsed" desc="download stuff">
    public void setJobId(final String _j){
        downloadJobId = _j;
    }
    
    public boolean download(final File fileObj) throws Exception {
        GetJobOutputRequest getJobOutputRequest = new GetJobOutputRequest()
            .withVaultName(vaultName)
            .withJobId(downloadJobId);
        boolean error = true;
        Exception exception = null;
        String error_message = null;
        OutputStream output = null;

        if (downloadJobId == null) {
            return false;
        }

        GetJobOutputResult getJobOutputResult
                = GlacierFrame.getClient(getRegion())
                    .getJobOutput(getJobOutputRequest);
        InputStream input
                = new BufferedInputStream(getJobOutputResult.getBody());
        progress_reporter.setFileSize(size);
        long total_read = 0;
        long start = System.currentTimeMillis();
        try {
            output = new BufferedOutputStream(new FileOutputStream(fileObj));
            byte[] buffer = new byte[BYTE_BUFFER];
            int bytesRead = 0;
            do {
                bytesRead = input.read(buffer);
                if (bytesRead <= 0) {
                    break;
                }
                total_read += bytesRead;
                long now = System.currentTimeMillis();
                long delta = now - start;
                if (delta > UPDATE_RATE_INTERVAL) {
                    start = now;
                    progress_reporter.setFilePosition(total_read);
                }
                output.write(buffer, 0, bytesRead);
            } while (bytesRead > 0);
            error = false;
        } catch (IOException e) {
            error_message = "Unable to save archive";
            exception = e;
        } catch (Exception e) {
            error_message = "Error saving archive";
            exception = e;
        } finally {
            try {
                input.close();
            }  catch (Exception e) {
                error_message = "Unable to close input";
                exception = e;
            }
            try {
                if (null != output) {
                    output.close();
                }
            } catch (Exception e) {
                error_message = "Unable to close output";
                exception = e;
            }
        }
        if (error) {
            if (error_message != null) {
                LGR.severe(error_message);
                throw exception;
            }
            return false;
        }
        return true;
    }

    String getJobId() {
        return downloadJobId;
    }

    public String initArchiveDownloadJob(final String snsTopic){
        JobParameters jobParameters = new JobParameters()
                .withArchiveId(archiveId)
                .withSNSTopic(snsTopic)
                //.withRetrievalByteRange("*** provide a retrieval range***")
                .withType("archive-retrieval");
        InitiateJobResult initiateJobResult = null;
        InitiateJobRequest iJobRequest = new InitiateJobRequest()
                    .withJobParameters(jobParameters)
                    .withVaultName(vaultName);
        
        LGR.log(Level.FINE, "requesting job for vault {0} and archive {1}",
                new Object[]{vaultName, archiveId});
        try{
            initiateJobResult
                = GlacierFrame.getClient(getRegion())
                    .initiateJob(iJobRequest);
        } catch (IllegalArgumentException e) {
            LGR.severe("This archive does not exist at AWS");
            LGR.log(Level.SEVERE, null, e);
        } catch (Exception e) {
            LGR.log(Level.SEVERE, null, e);
        }
        final String jobId =
                initiateJobResult == null ? "" : initiateJobResult.getJobId();
        if (jobId != null) {
            LGR.log(Level.INFO, "download job id is {0}{1}",
                   new Object[]{jobId.substring(0, 22), "..."});
        } else {
            LGR.severe("Job id not found");
        }
        return jobId;
    }
    //</editor-fold>
    
    public Archive withVaultName(final String n){
        vaultName = n;
        return this;
    }
    
    public String getVaultName(){
        return vaultName;
    }
    
    public long getSize(){
        return size;
    }

    public Archive withGranularity(int bytes){
        granularity = bytes;
        return this;
    }
    
    public String getArchiveId(){
        return archiveId;
    }
    
    public String getAWSCreateTime(){
        return aws_create_time;
    }
    
    public String getSHA256TreeHash(){
        return sha256treehash;
    }
        
    public String getDescription(){
        return descriptionString;
    }
    
    /**
     * Delete an archive from AWS.
     * Also mark any metadata object as deleted; metadata marked as such will be
     * deleted after 24 hours.  Because AWS inventory list is not realtime, this
     * application does not delete metadata object immediately because otherwise
     * it is reinserted when the archive lists are synchronized soon after
     * deletion. To delete it manually in your custom application, see metadata
     * API or SDK.
     * 
     * @return true on successful deletion
     */
    public boolean delete(){
        DeleteArchiveRequest request = new DeleteArchiveRequest()
                .withVaultName(vaultName)
                .withArchiveId(archiveId);
        final MArchive ma = new MArchive(new Vault(vaultName), archiveId);
        
        if (!isInAWSInventory()) {
            
            /* This code path should not be reached because delete button should
            * be greyed out for this case
            */
            deleteError = "Cannot delete because it is not in AWS";
            return false;
        }
        try{
            GlacierFrame.getClient().deleteArchive(request);
        } catch (Exception e) {
             LGR.log(Level.SEVERE, null, e);
             deleteError = "AWS Error in deleting";
             return false;
        }
        if (!GlacierFrame.haveMetadataProvider()) {
            return true;
        }
        try {
            return ma.markDeleted();
        } catch (SDKException | APIException ex){
            LGR.log(Level.SEVERE, null, ex);
        }
        deleteError = "Error in deleting from Metadata Provider";
        return false;
    }
    
    public String getDeleteError(){
        return deleteError;
    }
       
    public Archive withDeletedFlag(final boolean flag) { 
        deletedInAWS = flag;
        return this;
    }
    
    public boolean deletedInAWS(){
        return deletedInAWS;
    }

    public Archive withDescription(final String description) {
        descriptionString = description;
        return this;
    }

    public Archive withFiles(final ArrayList<String> fileList) {
        uploadedFileList = fileList;
        return this;
    }
}
