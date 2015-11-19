import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.codec.binary.StringUtils;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.cognitosync.model.Platform;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformApplicationRequest;
import com.amazonaws.services.sns.model.CreatePlatformApplicationResult;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.Endpoint;
import com.amazonaws.services.sns.model.ListEndpointsByPlatformApplicationRequest;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PlatformApplication;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Amazon {
	private static AmazonSNSClient snsClient;
	private List<String> applications; 
	
	public Amazon(){
		iniciarSesionAmazonSNS();
		this.applications = listApplications();
	}
	
	public List<String> getEndpoints(String applicationName){
		String arnApplication = getApplication(applicationName);
		List<String> endpoints = new ArrayList<String>();
		
		if(!arnApplication.isEmpty()){
			ListEndpointsByPlatformApplicationRequest listEndpointsByPlatformApplicationRequest = new ListEndpointsByPlatformApplicationRequest();
			listEndpointsByPlatformApplicationRequest.setPlatformApplicationArn(arnApplication);
			List<Endpoint> listEndpoints = this.snsClient.listEndpointsByPlatformApplication(listEndpointsByPlatformApplicationRequest).getEndpoints();
			for(Endpoint e : listEndpoints){
				endpoints.add(e.getEndpointArn());
			}
		}
		return endpoints;
	}
	
	public void publishAllDevices(Platform platform, String message){
		List<String> devices = getEndpoints("PruebaServidor");
		for(String device : devices){
			publish(device, platform, message);
		}
	}
	
	public void publishToDevice(String device, Platform platform, String message){
		publish(device, platform, message);
	}
	
	public List<String> listApplications(){
		List<PlatformApplication> applicationList = this.snsClient.listPlatformApplications().getPlatformApplications();
		List<String> arnApplicationList = new ArrayList<String>();
		
		for(PlatformApplication p : applicationList){
			arnApplicationList.add(p.getPlatformApplicationArn());
		}
		return arnApplicationList;
	}
	
	public List<String> getApplications(){
		return this.applications;
	}
	
	public String getApplication(String applicationName){
		String arn = "";
		for(String s : this.applications){
			if(s.contains(applicationName)){
				arn = s;
			}
		}
		return arn;
	}
	
	private static void iniciarSesionAmazonSNS(){
		try {
			snsClient = new AmazonSNSClient(new PropertiesCredentials(new File("/Users/Zeky/Documents/espacioTrabajo/NotificationServer/AwsCredentials.properties")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//metodo para crear una nueva aplicación
	private static CreatePlatformApplicationResult createPlatformApplication(){
		CreatePlatformApplicationRequest platformApplicationRequest = new CreatePlatformApplicationRequest();
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("PlatformPrincipal", "");
		attributes.put("PlatformCredential", "AIzaSyC0_DzxRPyWRYtVUQBi4C3iWTTYZ_4bglc" );
		platformApplicationRequest.setAttributes(attributes);
		platformApplicationRequest.setName("PruebaServidor");
		platformApplicationRequest.setPlatform(Platform.GCM.name());
		return snsClient.createPlatformApplication(platformApplicationRequest);
	}
	
	//metodo para crear un endpoint correspondiente a la aplicación
	private static CreatePlatformEndpointResult createPlatformEndpoint(Platform platform, String customData, String platformToken, String aplicationArn){
		CreatePlatformEndpointRequest platformEndpointRequest = new CreatePlatformEndpointRequest();
		platformEndpointRequest.setCustomUserData(customData);
		String token = platformToken;
		String userId = null;
		platformEndpointRequest.setToken(token);
		platformEndpointRequest.setPlatformApplicationArn(aplicationArn);		
		return snsClient.createPlatformEndpoint(platformEndpointRequest);
	}
	
	//metodo para publicar un mensaje de ejemplo en un dispositivo concreto
	private static PublishResult publish(String endpointARN, Platform platform, String mes){
		PublishRequest publishRequest = new PublishRequest();
		publishRequest.setMessageStructure("json");
		String message = getSampleMessage(mes);

		Map<String, String> messageMap = new HashMap<String, String>();
		messageMap.put(platform.name(), message);
		message = jsonify(messageMap);
		publishRequest.setTargetArn(endpointARN);
		
		System.out.println("{Message Body: " + message + "}");
		
		publishRequest.setMessage(message);
		
		return snsClient.publish(publishRequest);
	}
	
	//metodo para crear un topic
	private static CreateTopicResult createTopicResult(){
		CreateTopicRequest topic = new CreateTopicRequest();
		topic.setName("SNStopic");
		return snsClient.createTopic(topic);
	}

	//metodo para crear la suscripción a un topic
	private static void subscribeTopic(String topicARN, String protocol, String endpoint){
		SubscribeRequest subRequest = new SubscribeRequest(topicARN, protocol, endpoint);
	}
	
	private static Map<String, MessageAttributeValue> getValidNotificationAttributes(Map<String, MessageAttributeValue> notificationAttributes){
		Map<String, MessageAttributeValue> validAttributes = new HashMap<String, MessageAttributeValue>();
		
		if(notificationAttributes == null){
			return validAttributes;
		}
		
		for(Map.Entry<String, MessageAttributeValue> entry : notificationAttributes.entrySet()){
			if(!isBlank(entry.getValue().getStringValue())){
				validAttributes.put(entry.getKey(), entry.getValue());
			}
		}
		return validAttributes;
	}
	
	private static boolean isBlank(String s){
		return s.isEmpty();
		
	}
	public static String getSampleMessage(String message) {
		Map<String, Object> androidMessageMap = new HashMap<String, Object>();
		androidMessageMap.put("collapse_key", "Welcome");
		androidMessageMap.put("data", setMessage(message));
		androidMessageMap.put("delay_while_idle", true);
		androidMessageMap.put("time_to_live", 125);
		androidMessageMap.put("dry_run", false);
		return jsonify(androidMessageMap);
	}
	
	private static Map<String, String> setMessage(String message){
		Map<String, String> payload = new HashMap<String, String>();
		payload.put("mensaje", message);
		return payload;
	}
	
	private static String jsonify(Object s){
		try {
			return new ObjectMapper().writeValueAsString(s);
		} catch (Exception e) {
			throw (RuntimeException) e;
		}
	}
}
