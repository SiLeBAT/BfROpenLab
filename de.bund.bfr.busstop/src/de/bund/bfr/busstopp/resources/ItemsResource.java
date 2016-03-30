package de.bund.bfr.busstopp.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import de.bund.bfr.busstopp.dao.Dao;
import de.bund.bfr.busstopp.dao.ItemLoader;
import de.bund.bfr.busstopp.model.Item;

// Will map the resource to the URL items
@Path("/items")
public class ItemsResource {

	// Allows to insert contextual objects into the class,
	// e.g. ServletContext, Request, Response, UriInfo
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	// Return the list of items to the user in the browser
	@GET
	@Produces(MediaType.TEXT_XML)
	public List<Item> getItems4Browser() {
		return getOutputs();
	}

	// Return the list of items for applications
	@GET
	@Produces({ MediaType.APPLICATION_XML}) // , MediaType.APPLICATION_JSON 
	public List<Item> getItems() {
		return getOutputs();
	}
	private List<Item> getOutputs() {
		List<Item> items = new ArrayList<Item>();
		for (ItemLoader u : Dao.instance.getModel().values()) {
			items.add(u.getXml());
		}
		return items;
	}

	// retuns the number of items
	@GET
	@Path("count")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCount() {
		int count = Dao.instance.getModel().size();
		return String.valueOf(count);
	}

	// Defines that the next path parameter after items is
	// treated as a parameter and passed to the ItemResources
	// Allows to type http://localhost:8080/de.bund.bfr.busstopp/rest/app/1
	@Path("{id}")
	public ItemResource getItem(@PathParam("id") Long id) {
		return new ItemResource(uriInfo, request, id);
	}

	// jersey.config.server.wadl.disableWadl=true

	/**
	 * ItemLoader a File
	 */

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response itemFile(@FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
			@FormDataParam("comment") String comment) {

		try {
			long newId = System.currentTimeMillis();
			String filename = contentDispositionHeader.getFileName();

			ItemLoader item = new ItemLoader(newId, filename, comment);
			item.save(fileInputStream);
			Dao.instance.getModel().put(newId, item);

			String output = "Id for your uploaded file: " + newId;
			return Response.status(Response.Status.OK).entity(output).build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}
}