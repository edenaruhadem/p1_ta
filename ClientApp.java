//Java libraries
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
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
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.AmazonS3Exception;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
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
	String access_key_id = "AKIAINCOQWCQKLL4SBQQ";
	String secret_access_key = "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";		
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
	Region euWest1 = Region.getRegion(Regions.EU_WEST_1);
	s3.setRegion(euWest1);
	String bucket_name = null; 
	System.out.println("Listing existing buckets...");
	for (Bucket bucket : s3.listBuckets())
	{
		System.out.println("Bucket : " + bucket.getName());
		bucket_name = bucket.getName();
	}
	if (bucket_name == null)
	{
		bucket_name = "bucketTA";
	    System.out.format("The %s is going to create.\n", bucket_name);
	    try
	    {
	        s3.createBucket(bucket_name);
	    } catch (AmazonS3Exception e)
	    {
	        System.err.println(e.getErrorMessage());
	    }
	}
	}
	
}
