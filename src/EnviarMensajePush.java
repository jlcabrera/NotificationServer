
import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Servlet implementation class EnviarMensajePush
 */
@WebServlet("/EnviarMensajePush")
public class EnviarMensajePush extends HttpServlet {

	public static String URL_GOOGLE_CLOUD_MESSAGE = "https://android.googleapis.com/gcm/send"; 
	public static String API_KEY = "AIzaSyC0_DzxRPyWRYtVUQBi4C3iWTTYZ_4bglc";

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public EnviarMensajePush() {
		super();
		// TODO Auto-generated constructor stub
		//esto es un test entre ramas
	}
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// Recuperamos el mensaje de la notificación introducido y enviado a
		// traves del formulario web de index.jsp
		String mensaje = request.getParameter("mensaje");
		// SE lee el identificador de registro guardado previamente a través del
		// servicio REST
		String idRegistro = recuperarIdRegistro();

		Json json = new Json(new Data(mensaje), idRegistro);
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();

		System.out.println(gson.toJson(json));
		
		String respuesta = Gcm.invocarServicioGCM(gson.toJson(json), URL_GOOGLE_CLOUD_MESSAGE, API_KEY);
		System.out.println(respuesta);

		getServletContext().getRequestDispatcher("/index.html").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	// metodo que permite recuperar el identificador de registro que ha sido
	// previamente guardado en registro.txt por el servicio REST
	// implementado
	// por la clase RegisterIdService.

	private static final String recuperarIdRegistro() {
		String registroId = "";
		try {
			registroId = FileUtils.readLines(new File(Server.PATH)).get(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return registroId;
	}
}
