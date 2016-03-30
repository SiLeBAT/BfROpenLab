package de.bund.bfr.bus.stop.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Item {
	private Long id;
	private Inbound in = new Inbound();
	private Outbound out = new Outbound();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Inbound getIn() {
		return in;
	}

	public void setIn(Inbound in) {
		this.in = in;
	}

	public Outbound getOut() {
		return out;
	}

	public void setOut(Outbound out) {
		this.out = out;
	}
}
