//Java libraries
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
//Amazon AWS libraries
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.AmazonS3Exception;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.AmazonSQSException;
//Servlet libraries
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

public class ClientApp 
{		
	public static void main(String[] args) throws IOException {	
	BasicAWSCredentials credentials = null;
	String access_key_id = "";
	String secret_access_key = "";		
    try 
    {        	
        credentials = new BasicAWSCredentials(access_key_id, secret_access_key);        
    } catch (Exception e)
    {
    	throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (C:\\Users\\Diego\\.aws\\credentials), and is in valid format.",
                    e);
    }  	
	AmazonSQS sqs = new AmazonSQSClient(credentials);
	Region euWest1 = Region.getRegion(Regions.EU_WEST_1);			
	sqs.setRegion(euWest1);	
	//Creating queues...	
	CreateQueueRequest create_request_inbox = new CreateQueueRequest("Inbox")
	        .addAttributesEntry("DelaySeconds", "60")
	        .addAttributesEntry("MessageRetentionPeriod", "86400");
	try 
	{
	    sqs.createQueue(create_request_inbox);
	} catch (AmazonSQSException e) 
	{
	    if (!e.getErrorCode().equals("QueueAlreadyExists")) 
	    {
	        throw e;
	    }
	}
	CreateQueueRequest create_request_outbox = new CreateQueueRequest("Outbox")
	        .addAttributesEntry("DelaySeconds", "60")
	        .addAttributesEntry("MessageRetentionPeriod", "86400");
	try 
	{
	    sqs.createQueue(create_request_outbox);
	} catch (AmazonSQSException e) 
	{
	    if (!e.getErrorCode().equals("QueueAlreadyExists")) 
	    {
	        throw e;
	    }
	}
	System.out.println("The Inbox and Outbox Queue were created");
	String queue_url_inbox = sqs.getQueueUrl("Inbox").getQueueUrl();
	String queue_url_outbox = sqs.getQueueUrl("Outbox").getQueueUrl();
	//System.out.format("The URL is %s", queue_url);	
	//Sending messages
	SendMessageRequest send_msg_request = new SendMessageRequest()
	        .withQueueUrl(queue_url_inbox)
	        .withMessageBody("50@END")
	        .withDelaySeconds(5);
	sqs.sendMessage(send_msg_request);
	System.out.println("Waiting for the echo...");		 
	List<Message> messages = null;	
	do {
		messages = sqs.receiveMessage(queue_url_outbox).getMessages();		
	} while (messages.isEmpty());	
	Message message = messages.get(0);
	String echo = message.getBody();
	if (echo.equals("END"))
	{
		sqs.deleteMessage(queue_url_outbox, message.getReceiptHandle());
		//sqs.deleteQueue(queue_url_inbox);
		sqs.deleteQueue(queue_url_outbox);
		System.out.println("The conexion was closed");
	}
	else
	{
		System.out.format("The echo received is: %s", echo);
		sqs.deleteMessage(queue_url_outbox, message.getReceiptHandle());		
	}
		
}
}
