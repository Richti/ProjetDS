package framework.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
 * This class is used to make services talks to each others (in order to perform stateful replication)
 */
public class Message implements Serializable{
	
	private static final long serialVersionUID = -4995401686087165352L;
	
	private int id = -1;
	private MessageType type;
	/*
	 * The data list represents the content of the message
	 */
	private List<Object> data = new ArrayList<Object>();
	
	public Message(int id, MessageType type){
		this(type);
		this.id = id;	
	}
	
	public Message(MessageType type){
		this.type = type;
	}
	
	public Message(int id, MessageType type, List<Object> arguments){
		this.id = id;
		this.type = type;
		this.data = arguments;
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
		return data;
	}

	public void setArguments(List<Object> arguments) {
		this.data = arguments;
	}
	
}
