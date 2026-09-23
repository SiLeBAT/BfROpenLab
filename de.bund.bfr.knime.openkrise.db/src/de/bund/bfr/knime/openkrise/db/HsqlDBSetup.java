package de.bund.bfr.knime.openkrise.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


// if the system property value of key hsqldb.method_class_names is not empty 
// hsqldb lib requires the path of a called java function to be allowed by that value
// see https://hsqldb.org/doc/guide/guide.html for more information
public class HsqlDBSetup {
	
	private static final String HSQLDB_WHITELIST_KEY = "hsqldb.method_class_names";
	private static final String REQUIRED_METHOD_NAMES = String.join(";", new String[]{
			"de.bund.bfr.knime.openkrise.db.Levenshtein.LD",
			"de.bund.bfr.knime.openkrise.db.StringSimilarity.diceCoefficientOptimized",
			// "de.bund.bfr.knime.openkrise.db.*", // this can be used alternatively 
	});
	
	private static boolean runOnce = false;
	
	// the wrapper prevents an overwrite of the
	// the system property value for key hsqldb.method_class_names
	// instead of a value overwrite a value merge is done 
	public static class PropertiesWrapper extends Properties {
		
		private static final long serialVersionUID = 5281699245651383025L;
		
		public PropertiesWrapper(Properties underlying) {
	        super(underlying);
	    }

	    @Override
	    public Object setProperty(String key, String value) {
	    	if (HSQLDB_WHITELIST_KEY.equals(key)) return super.setProperty(key, mergeMethodNames(getProperty(key), value));
	    	return super.setProperty(key, value);
	        // throw new UnsupportedOperationException("System property '" + key + "' is frozen and cannot be changed.");
	    }

//	    @Override
//	    public Object put(Object key, Object value) {
//	        throw new UnsupportedOperationException("System properties are frozen.");
//	    }

//	    @Override
//	    public Object remove(Object key) {
//	        throw new UnsupportedOperationException("System properties are frozen.");
//	    }

	    // Override clear(), putAll(), etc., similarly if needed
	}
	
	
	public static void run() {
			
		if (runOnce) return;
		
		// allow only adding of values for key hsqldb.method_class_names
		// because another party might overwrite the names we are adding
		System.setProperties(new PropertiesWrapper(System.getProperties()));
			
		System.setProperty(HSQLDB_WHITELIST_KEY, REQUIRED_METHOD_NAMES);
			
		runOnce = true;
	}
	
	private static String mergeMethodNames(String oldNames, String newNames) {
		if (newNames == null) return oldNames;
		if (oldNames == null) return newNames;
		
		final String[] newPaths = newNames.split(";");
		final List<String> mergedPaths = oldNames == null ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(oldNames.split(";")));
		
		for (String newPath : newPaths) {
			if (!mergedPaths.contains(newPath)) mergedPaths.add(newPath);
		}
		
		return String.join(";", mergedPaths);
	}
}
