import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
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
import com.fasterxml.jackson.databind.ObjectMapper;

public class Amazon {
	private AmazonSNSClient snsClient;
	private List<String> applications; 
	
	public Amazon(){
		iniciarSesionAmazonSNS();
		this.applications = listApplications();
	}
	
	public Amazon(String accessKey, String secretPass){
		iniciarSesionAmazonSNS(accessKey, accessKey);
		this.applications = listApplications();
	}
	
	public Amazon(File AwsCredentials){
		try {
			this.snsClient = new AmazonSNSClient(new PropertiesCredentials(AwsCredentials));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> getEndpoints(String applicationName){
		String arnApplication = getApplication(applicationName);
		List<String> endpoints = new ArrayList<String>();
		
		if(!arnApplication.isEmpty()){
			ListEndpointsByPlatformApplicationRequest listEndpointsByPlatformApplicationRequest = new ListEndpointsByPlatformApplicationRequest();
			listEndpointsByPlatformApplicationRequest.setPlatformApplicationArn(arnApplication);
			List<Endpoint> listEndpoints = this.snsClient.listEndpointsByPlatformApplication(listEndpointsByPlatformApplicationRequest).getEndpoints();
			//Comprobacion de los tokens para la lista de los endpoints
			System.out.println(listEndpointsByPlatformApplicationRequest.getNextToken());
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
	
	private void iniciarSesionAmazonSNS(){
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
	
	private void iniciarSesionAmazonSNS(String user, String pass){
		BasicAWSCredentials credenciales = new BasicAWSCredentials(user, pass);
		snsClient = new AmazonSNSClient(credenciales);
		
	}

//	public static void main(String[] args) {
//		try {
//			
//			//snsClient = new AmazonSNSClient(new PropertiesCredentials(new File("AwsCredentials.properties")));
//			
//			//Creamos una nueva aplicación
//			CreatePlatformApplicationResult platformResult = createPlatformApplication();
//			
//			//Guardamos el ARN del resultado de la petición en un String;
//			String applicationARN = platformResult.getPlatformApplicationArn();
//			System.out.println(applicationARN);
//			
//			//leemos el identificador
//			List<String> platformTokens = FileUtils.readLines(new File("registro.txt"));
//			System.out.println(platformTokens.get(0));
//			
//			//Creamos un nuevo Endpoint asociada a la aplicación
//			CreatePlatformEndpointResult platformEndpointResult = createPlatformEndpoint(Platform.GCM,"CustomData - Useful to store endpint specific data", platformTokens.get(0), applicationARN);
//			System.out.println(platformEndpointResult.getEndpointArn());
//			String endopointARN  = platformEndpointResult.getEndpointArn();
//			
//			//Creamos un nuevo topic
//			CreateTopicResult topicResult = createTopicResult();
//			String topicARN = topicResult.getTopicArn();
//			System.out.println(topicARN);
//			
//			//Suscribimos la aplicación al topic
//			subscribeTopic(endopointARN, "application", endopointARN);
//			
//			//Ahora toca publicar algo al endpoint para ver si llega la notificación al dispositivo
//			PublishResult publishResult = publish(endopointARN, Platform.GCM, "test");
//			System.out.println("Published! \n{MessageId=" + publishResult.getMessageId() + "}");
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	//metodo para crear una nueva aplicación
	public String createPlatformApplication(String serverKey){
		CreatePlatformApplicationRequest platformApplicationRequest = new CreatePlatformApplicationRequest();
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("PlatformPrincipal", "");
		attributes.put("PlatformCredential",serverKey);
		platformApplicationRequest.setAttributes(attributes);
		platformApplicationRequest.setName("PruebaServidor");
		platformApplicationRequest.setPlatform(Platform.GCM.name());
		return snsClient.createPlatformApplication(platformApplicationRequest).getPlatformApplicationArn();
	}
	
	//metodo para crear un endpoint correspondiente a la aplicación
	private CreatePlatformEndpointResult createPlatformEndpoint(Platform platform, String customData, String platformToken, String aplicationArn){
		CreatePlatformEndpointRequest platformEndpointRequest = new CreatePlatformEndpointRequest();
		platformEndpointRequest.setCustomUserData(customData);
		String token = platformToken;
		String userId = null;
		platformEndpointRequest.setToken(token);
		platformEndpointRequest.setPlatformApplicationArn(aplicationArn);		
		return snsClient.createPlatformEndpoint(platformEndpointRequest);
	}
	
	//metodo para publicar un mensaje de ejemplo en un dispositivo concreto
	private PublishResult publish(String endpointARN, Platform platform, String mes){
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
	private CreateTopicResult createTopicResult(){
		CreateTopicRequest topic = new CreateTopicRequest();
		topic.setName("SNStopic");
		return snsClient.createTopic(topic);
	}

	//metodo para crear la suscripción a un topic
	private  void subscribeTopic(String topicARN, String protocol, String endpoint){
		SubscribeRequest subRequest = new SubscribeRequest(topicARN, protocol, endpoint);
	}
	
	private  Map<String, MessageAttributeValue> getValidNotificationAttributes(Map<String, MessageAttributeValue> notificationAttributes){
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
