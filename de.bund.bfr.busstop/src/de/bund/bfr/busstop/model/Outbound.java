package de.bund.bfr.busstop.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Outbound {
	private String workflow = "";
	private String report = "";
	private String comment = "";
	private Images images = new Images();

	public Images getImages() {
		return images;
	}

	public void setImages(Images images) {
		this.images = images;
	}

	public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(String workflow) {
		this.workflow = workflow;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}