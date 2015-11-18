import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.codec.binary.StringUtils;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.cognitosync.model.Platform;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformApplicationRequest;
import com.amazonaws.services.sns.model.CreatePlatformApplicationResult;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.ListEndpointsByPlatformApplicationRequest;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Amazon {
	private static AmazonSNSClient snsClient;
	
	public static void main(String[] args) {
		try {
			snsClient = new AmazonSNSClient(new PropertiesCredentials(new File("AwsCredentials.properties")));
			
			//Creamos una nueva aplicación
			CreatePlatformApplicationResult platformResult = createPlatformApplication();
			
			//Guardamos el ARN del resultado de la petición en un String;
			String applicationARN = platformResult.getPlatformApplicationArn();
			System.out.println(applicationARN);
			
			//leemos el identificador
			List<String> platformTokens = FileUtils.readLines(new File("registro.txt"));
			System.out.println(platformTokens.get(0));
			
			//Creamos un nuevo Endpoint asociada a la aplicación
			CreatePlatformEndpointResult platformEndpointResult = createPlatformEndpoint(Platform.GCM,"CustomData - Useful to store endpint specific data", platformTokens.get(0), applicationARN);

			ListEndpointsByPlatformApplicationRequest listaDispositivos = new ListEndpointsByPlatformApplicationRequest();
			listaDispositivos.setPlatformApplicationArn(applicationARN);
			
			String endopointARN  = platformEndpointResult.getEndpointArn();

			//Ahora toca publicar algo al endpoint para ver si llega la notificación al dispositivo
			PublishResult publishResult = publish(endopointARN, Platform.GCM);
			System.out.println("Published! \n{MessageId=" + publishResult.getMessageId() + "}");
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
	
	private static PublishResult publish(String enpointARN, Platform platform){
		PublishRequest publishRequest = new PublishRequest();
		Map<String, MessageAttributeValue> notificationAttributes = getValidNotificationAttributes(null);
		
		if(notificationAttributes != null && !notificationAttributes.isEmpty()){
			publishRequest.setMessageAttributes(notificationAttributes);
		}
		publishRequest.setMessageStructure("json");
		String message = getSampleMessage();
		Map<String, String> messageMap = new HashMap<String, String>();
		messageMap.put(platform.name(), message);
		message = jsonify(messageMap);
		publishRequest.setTargetArn(enpointARN);
		
		System.out.println("{Message Body: " + message + "}");
		StringBuilder builder = new StringBuilder();
		builder.append("{Message Attributes: ");
		for(Map.Entry<String, MessageAttributeValue> entry: notificationAttributes.entrySet()){
			builder.append("(\"" + entry.getKey() +"\": \"" + entry.getValue().getStringValue() + "\"),");
		}
		builder.deleteCharAt(builder.length() - 1);
		builder.append("}");
		System.out.println(builder.toString());
		
		publishRequest.setMessage(message);
		return snsClient.publish(publishRequest);
	}
	
	public static Map<String, MessageAttributeValue> getValidNotificationAttributes(Map<String, MessageAttributeValue> notificationAttributes){
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
	
	public static String getSampleMessage() {
		Map<String, Object> androidMessageMap = new HashMap<String, Object>();
		androidMessageMap.put("collapse_key", "Welcome");
		androidMessageMap.put("data", getData());
		androidMessageMap.put("delay_while_idle", true);
		androidMessageMap.put("time_to_live", 125);
		androidMessageMap.put("dry_run", false);
		return jsonify(androidMessageMap);
	}
	
	private static Map<String, String> getData(){
		Map<String, String> payload = new HashMap<String, String>();
		payload.put("mensaje","hola");
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
