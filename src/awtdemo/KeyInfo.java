package awtdemo;

import java.io.Serializable;

public class KeyInfo implements Serializable {

	private int event;
	private int key_code;
	
	
	public KeyInfo(int event,int key_code) {
		
		this.event=event;
		this.key_code=key_code;
	}
	
	public int getEvent() {
		return event;
	}
	public void setEvent(int event) {
		this.event = event;
	}
	public int getKey_code() {
		return key_code;
	}
	public void setKey_code(int key_code) {
		this.key_code = key_code;
	}
	
}
