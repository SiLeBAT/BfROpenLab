package de.bund.bfr.bus.stop.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Inbound {
	private String filename = "";
	private String comment = "";

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}