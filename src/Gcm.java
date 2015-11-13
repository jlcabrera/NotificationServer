import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;


public class Gcm {
	public Gcm(){
		
	}
	public static String invocarServicioGCM(final String json,	final String url, final String apiKey) {

		try {
			HttpClient cliente = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(url);
			post.setHeader("Content-Type", "application/json");
			post.setHeader("project_id", "pruebassns");
			post.setHeader("Authorization", "key=" + apiKey);
			post.setEntity(new ByteArrayEntity(json.getBytes("UTF-8")));

			HttpResponse response = cliente.execute(post);
			return EntityUtils.toString(response.getEntity());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
