package de.bund.bfr.busstopp.dao;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import de.bund.bfr.busstopp.Constants;
import de.bund.bfr.busstopp.model.Item;

public class ItemLoader {
	private Item xml = new Item();
	private boolean deleted = false;

	public ItemLoader(Long id, String filename, String comment) {
		xml.setId(id);
		xml.getIn().setFilename(filename);
		xml.getIn().setComment(comment);
	}
	public ItemLoader(Long id, File folder) {
		xml.setId(id);
		File[] paths = folder.listFiles();
		for (File path : paths) {
			if (path.getName().equals(Constants.COMMENT_FILENAME)) {
				try {
					xml.getIn().setComment(loadFile(path));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if (path.getName().equals(Constants.DELETED_FILENAME)) {
				deleted = true;
			}
			else {
				xml.getIn().setFilename(path.getName());
				break;				
			}
		}
	}
	
	public Item getXml() {
		return xml;
	}
	public void setXml(Item xml) {
		this.xml = xml;
	}
	public boolean isDeleted() {
		return deleted;
	}

	private String loadFile(File f) throws IOException {
		String everything;
		BufferedReader br = new BufferedReader(new FileReader(f));
		try {
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    everything = sb.toString();
		} finally {
		    br.close();
		}
		return everything;
	}
	public void save(InputStream fileInputStream) throws IOException {
		String filePath = Constants.SERVER_UPLOAD_LOCATION_FOLDER + xml.getId() + "/" + xml.getIn().getFilename();
		String commentPath = Constants.SERVER_UPLOAD_LOCATION_FOLDER + xml.getId() + "/" + Constants.COMMENT_FILENAME;
		// save the file to the server
		saveFile(fileInputStream, filePath);
		saveFile(xml.getIn().getComment(), commentPath);
	}

	private void saveFile(String input, String filePath) throws IOException {
		if (input == null) input = "";
		InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
		saveFile(stream, filePath);
	}
	// save uploaded file to a defined location on the server
	private void saveFile(InputStream uploadedInputStream, String serverLocation) throws IOException {
			File f = new File(serverLocation);
			f.getParentFile().mkdirs();
			OutputStream outpuStream = new FileOutputStream(f);
			int read = 0;
			byte[] bytes = new byte[1024];

			outpuStream = new FileOutputStream(new File(serverLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				outpuStream.write(bytes, 0, read);
			}
			outpuStream.flush();
			outpuStream.close();
	}
	public void delete() throws IOException {
		String deletedPath = Constants.SERVER_UPLOAD_LOCATION_FOLDER + xml.getId() + "/" + Constants.DELETED_FILENAME;
		saveFile("", deletedPath);
	}
}