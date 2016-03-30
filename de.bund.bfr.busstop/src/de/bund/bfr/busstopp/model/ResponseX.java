package de.bund.bfr.busstopp.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "response")
public class ResponseX {
	
	private boolean success;	
	private Long id;
	private String error;
	private String action;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

}
