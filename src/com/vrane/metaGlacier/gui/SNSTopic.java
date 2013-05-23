/*
 * @(#)SNSTopic.java  0.6 2013 May 7
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.vrane.metaGlacier.Main;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author K Z Win
 */
public class SNSTopic {
    private final static Logger LGR = Main.getLogger(SNSTopic.class);
    
    private AmazonSNSClient snsClient = new AmazonSNSClient(Main.frame);
    private String topicARN;

    /**
     * Creates a SNS topic.
     *
     * @param topic string
     * @param email address to be notified when there is any event for this topic.
     * @return false if there is any error.
     * @throws Exception
     */
    public boolean createTopic(final String topic, final String email)
            throws Exception{
        CreateTopicRequest request = new CreateTopicRequest()
            .withName(topic);
        CreateTopicResult result = null;
        
        try {
            result = snsClient.createTopic(request);
        } catch (Exception e) {
            LGR.setUseParentHandlers(true);
            LGR.log(Level.SEVERE, null, e);
            LGR.setUseParentHandlers(false);
            return false;
        }
        
        topicARN = result.getTopicArn();
        LGR.log(Level.INFO, "topic arn is {0}", topicARN);

        SubscribeRequest request2 = new SubscribeRequest()
            .withTopicArn(topicARN).withEndpoint(email).withProtocol("email");
        SubscribeResult result2 = snsClient.subscribe(request2);
                
        LGR.log(Level.INFO, "subscription ARN {0}",
                result2.getSubscriptionArn());     
        
        return true;
    }

    /**
     * Gets topic ARN to be used when submitting jobs to AWS.
     *
     * @return topic arn string.
     */
    public String getTopicARN(){
        return topicARN;
    }
}
