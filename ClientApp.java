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
	AmazonS3 s3 = new AmazonS3Client(credentials);
	AmazonSQS sqs = new AmazonSQSClient(credentials);
	Region euWest1 = Region.getRegion(Regions.EU_WEST_1);
	s3.setRegion(euWest1);		
	sqs.setRegion(euWest1);
	//String nameFile = "index_file.txt";
	//File file = new File("/src/main/java" + nameFile);
	//String key_name = file.getName();
	File file = new File("C:\\Users\\Diego\\Desktop\\aws\\assignment1\\src\\main\\java\\index\\Index_File.txt");
	String key_name = file.getName();
	String bucket_name = null;	
	System.out.println("Listing existing buckets...");
	boolean existBucket = false;
	boolean exist_index = false;
	for (Bucket bucket : s3.listBuckets())
	{
		System.out.println("Bucket : " + bucket.getName());
		bucket_name = bucket.getName();
	}
	if (bucket_name == null && existBucket == false)
	{
		bucket_name = "bucketatest2018";	    
	    try //Creating a bucket
	    {
	        s3.createBucket(bucket_name);
	        System.out.format("Creating %s.\n", bucket_name);
	        //s3.putObject(bucket_name,key_name,file);
	        existBucket = true;
	        //existFileIndex = true;
	        
	    } catch (AmazonS3Exception e)
	    {
	        System.err.println(e.getErrorMessage());
	    }
	    /*ObjectListing object_listing = s3.listObjects(bucket_name);	
		for (S3ObjectSummary objectSummary : object_listing.getObjectSummaries()) 
		{			
			if (objectSummary.getKey().equals(nameFile)){
				System.out.format("The file %s was created and stored in bucket.\n", nameFile);
				existFileIndex = true;
			}
		}*/
	    
	 }
	/*else
	{
		try //Deleting the bucket
		{
		    s3.deleteBucket(bucket_name);
		    System.out.format("Deleting %s", bucket_name);		    
		} catch (AmazonS3Exception e) 
		{
		    System.err.println(e.getErrorMessage());
		}
	}*/
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
	        .withMessageBody("2@lavida es bella")
	        .withDelaySeconds(5);
	sqs.sendMessage(send_msg_request);
	System.out.println("Waiting for the echo...");		 
	List<Message> messages = null;	
	do {
		messages = sqs.receiveMessage(queue_url_outbox).getMessages();		
	} while (messages.isEmpty());
	
	Message message = messages.get(0);
	String echot = message.getBody();
	String[] receivedMessage = message.getBody().split("@"); 
	String session = receivedMessage[0];  
	String echo = receivedMessage[1];
	System.out.format("The echo received is: %s", echot);
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	ObjectListing object_listing = s3.listObjects(bucket_name);	
	for (S3ObjectSummary objectSummary : object_listing.getObjectSummaries()) 
	{			
		if (objectSummary.getKey().equals("Index_File.txt")){
			System.out.format("The file %s is already stored in the bucket.\n", "Index_File.txt");
			exist_index = true;
		}
	}
	if (exist_index == false){		
		createIndexFile(session, echo);
		System.out.println("Uploading index file object to the bucket\n");
		s3.putObject(bucket_name,key_name,file);		
	}
	
	//Download the index file to update
	S3Object o = s3.getObject(bucket_name,key_name);
	InputStream readero = new BufferedInputStream(o.getObjectContent());
	File fileindex = new File("C:\\Users\\Diego\\Desktop\\aws\\assignment1\\src\\main\\java\\download\\Index_File.txt");	
	OutputStream writero = new BufferedOutputStream(new FileOutputStream(fileindex));
	int read1 = -1;
	while ((read1 = readero.read()) != -1) 
	{			
		writero.write(read1);
	}		
	writero.flush();
	writero.close();
	readero.close();	
	createIndexFile(session, echo); //Se actualiza con el nuevo mensaje
	System.out.println("Uploading an update index to S3 from a file\n"); //Se vuelve a subir el fileindex
	s3.putObject(bucket_name,key_name,file);	
	sqs.deleteMessage(queue_url_outbox, message.getReceiptHandle());
	//sqs.deleteQueue(queue_url_inbox);
	//sqs.deleteQueue(queue_url_outbox);
	//Deleting queues
	/*sqs.deleteQueue(queue_url);
	System.out.format("The Inbox queue with URL %s was deleted", queue_url);*/	
	}
	public static void createIndexFile(String session, String echo) throws IOException 
	{		
		String savestr = "C:\\Users\\Diego\\Desktop\\aws\\assignment1\\src\\main\\java\\index\\Index_File.txt";
		File file = new File(savestr);
		String linea;
		if (file.exists() && !file.isDirectory()) 
		{
			FileReader rfile = null;			
			try 
			{
				rfile = new FileReader(savestr);
			} catch (FileNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedReader b = new BufferedReader(rfile);
			while ((linea = b.readLine()) != null) {
				if (linea.equals(session + "-" + echo)) 
				{
					return;
				}
			}
			b.close();
		}
		PrintWriter out = null;
		if (file.exists() && !file.isDirectory()) 
		{
			out = new PrintWriter(new FileOutputStream(new File(savestr), true));
			out.println(session + ":" + echo);
			out.close();
		} else 
		{
			out = new PrintWriter(savestr);
			out.println(session + ":" + echo);
			out.close();
		}
	}
}
