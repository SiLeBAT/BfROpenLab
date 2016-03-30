package de.bund.bfr.busstopp.resources;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import de.bund.bfr.busstopp.Constants;
import de.bund.bfr.busstopp.dao.Dao;
import de.bund.bfr.busstopp.dao.ItemLoader;
import de.bund.bfr.busstopp.model.Item;
import de.bund.bfr.busstopp.model.ResponseX;

import javax.ws.rs.core.UriInfo;

public class ItemResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	Long id;

	public ItemResource(UriInfo uriInfo, Request request, Long id) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.id = id;
	}

	// Application integration
	@GET
	@Produces({ MediaType.APPLICATION_XML}) // , MediaType.APPLICATION_JSON 
	public Item getItem() {
		return getOutput();
	}

	// for the browser
	@GET
	@Produces(MediaType.TEXT_XML)
	public Item getItem4Browser() {
		return getOutput();
	}

	private Item getOutput() {
		ItemLoader item = Dao.instance.getModel().get(id);
		if (item == null)
			throw new RuntimeException("Get: ItemLoader with ID '" + id + "' not found");
		return item.getXml();
	}

	@DELETE
	@Produces({ MediaType.APPLICATION_XML})
	public ResponseX deleteItem() {
		ResponseX response = new ResponseX();
		response.setId(id);
		response.setAction("DELETE");
		ItemLoader c = Dao.instance.getModel().get(id);
		if (c != null) {
			try {
				c.delete();
			} catch (IOException e) {
				e.printStackTrace();
				response.setSuccess(false);
				response.setError(e.getMessage());
			}
			c = Dao.instance.getModel().remove(id);
			response.setSuccess(true);
		}
		else  {
			response.setSuccess(false);
			response.setError("ID not found");
		}
		return response;
	}
	
	private ResponseBuilder getDownloadResponse(String filename) {
	    ResponseBuilder response = Response.noContent();
	    File file = new File(filename);
	    if (file.exists()) {
		    response = Response.ok((Object) file);
		    response.header("Content-Disposition", "attachment; filename=" + file.getName());
	    }
		return response;
	}
	@GET
	@Path("workflow")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getWorkflow() {
		ItemLoader c = Dao.instance.getModel().get(id);
		if (c == null) return Response.noContent().build();
		String filename = Constants.SERVER_UPLOAD_LOCATION_FOLDER + c.getXml().getId() + "/" + c.getXml().getOut().getWorkflow();
		ResponseBuilder response = getDownloadResponse(filename);
	    return response.build();
	}
	@GET
	@Path("report")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getReport() {
		ItemLoader c = Dao.instance.getModel().get(id);
		if (c == null) return Response.noContent().build();
		String filename = Constants.SERVER_UPLOAD_LOCATION_FOLDER + c.getXml().getId() + "/" + c.getXml().getOut().getReport();
		ResponseBuilder response = getDownloadResponse(filename);
	    return response.build();
	}
	@GET
	@Path("comment")
	@Produces(MediaType.TEXT_PLAIN)
	public String getComment() {
		ItemLoader c = Dao.instance.getModel().get(id);
		if (c != null) {
		    return c.getXml().getOut().getComment();
		}
		else {
			return "";
		}
	}
	@GET
	@Path("zip")
	@Produces(MediaType.TEXT_PLAIN)
	public String getAllZipped() {
		return "todo...";
	}
	
}