package message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Message implements Serializable{
	
	private static final long serialVersionUID = -4995401686087165352L;
	
	private int id;
	private MessageType type;
	private List<Object> arguments = new ArrayList<Object>();
	
	public Message(int id, MessageType type){
		this.id = id;
		this.type = type;
	}
	
	public Message(int id, MessageType type, List<Object> arguments){
		this.id = id;
		this.type = type;
		this.arguments = arguments;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public List<Object> getArguments() {
		return arguments;
	}

	public void setArguments(List<Object> arguments) {
		this.arguments = arguments;
	}
	
}
