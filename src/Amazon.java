import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.ListPlatformApplicationsResult;
import com.amazonaws.services.sns.model.PlatformApplication;


public class Amazon {

	public static void main(String[] args) {
		try {
			AmazonSNSClient snsClient = new AmazonSNSClient(new PropertiesCredentials(new File("AwsCredentials.properties")));
			//Recuperamos la lista de las aplicaciones
			ListPlatformApplicationsResult applicationsResult = snsClient.listPlatformApplications();
			System.out.println(applicationsResult.getNextToken());
			List<PlatformApplication> appList =  applicationsResult.getPlatformApplications();
			System.out.println(appList.get(0).getPlatformApplicationArn());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
