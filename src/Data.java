import java.io.Serializable;

public class Data implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7178392214561417259L;
	private String mensaje;
	
	public Data(String mensaje){
		this.setMensaje(mensaje);
	}
	
	public Data(){
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
}