package de.bund.bfr.bus.stop.dao;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.bund.bfr.bus.stop.Constants;

public enum Dao {
	instance;

	private Map<Long, ItemLoader> contentProvider = new HashMap<>();

	private Dao() {
		try {
			// create new file
			File f = new File(Constants.SERVER_UPLOAD_LOCATION_FOLDER);

			// returns pathnames for files and directory
			File[] paths = f.listFiles();

			// for each pathname in pathname array
			for (File path : paths) {
				// prints file and directory paths
				//System.out.println(path);
				if (path.isDirectory()) {
					long l = Long.parseLong(path.getName());
					try {
						ItemLoader item = new ItemLoader(l, path);
						if (!item.isDeleted()) contentProvider.put(l, item);						
					}
					catch (Exception e) {e.printStackTrace();}
				}
			}
		} catch (Exception e) {
			// if any error occurs
			e.printStackTrace();
		}
	}

	public Map<Long, ItemLoader> getModel() {
		return contentProvider;
	}

}
