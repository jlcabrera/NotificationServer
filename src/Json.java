import java.io.Serializable;

public class Json implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4119280232130363309L;
	private Data data;
	private String to;
	
	public Json(Data data, String to){
		this.setData(data);
		this.setTo(to);
	}
	
	public Json(){
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}
	
	
}
