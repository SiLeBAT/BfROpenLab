/*******************************************************************************
 * Copyright (c) 2014-2025 German Federal Institute for Risk Assessment (BfR)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.openkrise.db.itest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import de.bund.bfr.knime.openkrise.db.DBKernel;
import de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat.TraceImporter;
import de.bund.bfr.knime.ui.IProgressMonitor;

class ImporterTest {
	private ImporterTest() {};
	
	private static final String REL_IMPORT_RESOURCES_DIR = Paths.get("resources", "import").toString(); 
	private static final String REFERENCE_OUTPUT_DIR_NAME = "reference-output".toString();
	private static final String TEMPLATE_DIR_NAME = "templates";
	private static final String DIFF_DIR_NAME = "__diff-out__";
	private static final String TEMP_OUTPUT_DIR_NAME = "output";
	private static final String TEMP_DBS_DIR_NAME = "dbs";
	
	private File templateDir;
	private File referenceOutDir;
	private File diffDir;
	private File tempDir;
	private File tempOutDir;
	private File tempDbsDir;
	
	static boolean testFilesImport(File[] files, String outputDir, String tempDir) {
		DBSettings dbSettings = DBUtils.getCurrentDBSettings();
		List<String> tempDBList = new ArrayList<>();
		boolean dbGuiIsOpen = DBKernel.mainFrame != null;
		boolean result = true;
		try {
			if (!dbGuiIsOpen) DBKernel.openDBGUI();
			for (File file : files) {
				String inputPath = file.getPath();
				String outputPath = new File(outputDir, file.getName().replaceAll("\\.xlsx$", ".txt")).getPath();
				
				String tempDBPath = getNewTempDBPath(tempDir);
				DBSettings tmpDBSettings = new DBSettings(tempDBPath, dbSettings.username, dbSettings.password, dbSettings.readOnly);
				tempDBList.add(tempDBPath);
				
				DBUtils.switchToDB(tmpDBSettings);
				
				try { 
					testFileImport(inputPath, outputPath);
				} catch (Exception e) {
					result = false;
					e.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			result = false;
		} finally {
			DBUtils.switchToDB(dbSettings);
			if (!dbGuiIsOpen) DBKernel.mainFrame.dispose();
			
			tempDBList.forEach(dbPath -> {
				try {
					FileUtils.deleteDirectory(new File(dbPath));
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
		
		return result;
	}
	
	private static String html2Text(String html) {
		html = html.replaceAll("(?<=\\</[^/\\<\\>]+\\>)", "\n");
		return html;
	}
	
	private static String postProcessFilePaths(String html) {
		File bundleDir = new File(BundleUtils.getBundleDir());
		return html.replaceAll("(?<=')" + Pattern.quote(bundleDir.getPath()), bundleDir.getName());
	}
	
	public static void testFileImport(String inputXlsxPath, String outputPath) throws Exception {
		boolean result = false;
		
		DBUtils.startDBTransaction();
		TraceImporter traceImporter = createTraceImporter();
		
		try {
			// result = traceImporter.doImport(inputXlsxPath, null, false);
			result = traceImporter.importFile(
				inputXlsxPath, 
				DBKernel.mainFrame, 
				new IProgressMonitor() {

					@Override
					public void setMessage(String message) {}
	
					@Override
					public void setProgress(int progress) {}
	
					@Override
					public boolean isCanceled() {
						return false;
					}
				}
			);
		} catch(Throwable throwable) {
			throw throwable;
		} finally {
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
				writer.write("ImportResult: " + result + "\n\n");
				DBUtils.appendTextForDB(writer);
				writer.write("\n\nLogMessages:\n" + html2Text(postProcessFilePaths(traceImporter.getLogMessages())));
				writer.write("\n\nWarnMessages:\n" + html2Text(postProcessFilePaths(traceImporter.getLogWarnings())));
				writer.close();
			} catch (Exception e) {
				System.err.println("Creation of outfile \"" + outputPath + "\" failed.");
				throw e;
			} finally {
				if (!result) {
					DBUtils.rollbackTransaction();
				} else {
					DBUtils.commitDBTransaction();
				}
			}
		}
	}
	
	private static TraceImporter createTraceImporter() {
		// Station.reset(); Lot.reset(); Delivery.reset();
		
		return new TraceImporter();
	}
	
	
	private static String getNewTempDBPath(String dbsDir) throws Exception {
		String defaultTmpDBPath = Paths.get(dbsDir, "db").toString();
		String tmpDBPath = defaultTmpDBPath;
		int counter = 1;
		while (new File(tmpDBPath).exists()) {
			tmpDBPath = defaultTmpDBPath + ++counter;
		}
		return tmpDBPath;
	}
	
	private boolean runPreTestOps() {
		String importDir = Paths.get(BundleUtils.getBundleDir(), REL_IMPORT_RESOURCES_DIR).toString();
		this.templateDir = new File(importDir, TEMPLATE_DIR_NAME);
		this.referenceOutDir = new File(importDir, REFERENCE_OUTPUT_DIR_NAME);
		this.diffDir = new File(referenceOutDir.toString(), DIFF_DIR_NAME);
		
		
		if (!templateDir.isDirectory()) {
			System.err.println("Template dir \"" + templateDir.toString() + "\" does not exist or is no directory.");
			return false;
		}
		if (!referenceOutDir.isDirectory()) {
			System.err.println("Reference output dir \"" + referenceOutDir.toString() + "\" does not exist or is no directory.");
			return false;
		}
		try {
			FileUtils.deleteDirectory(diffDir);
		} catch (IOException e) {
			System.err.println("Deleting diff directory \"" + diffDir.toString() + "\" failed.");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean createTempFolders() {
		try {
			tempDir = new File(Files.createTempDirectory("fcl-da-itest-import-").toString());
			System.out.println("Tempdir \"" + tempDir.toString() + "\" was created.");
			
			tempOutDir = new File(tempDir, TEMP_OUTPUT_DIR_NAME);
			if (!tempOutDir.mkdir()) {
				System.err.println("Could not create directory \"" + tempOutDir.toString() + "\".");
				return false;
			}
			tempDbsDir  = new File(tempDir, TEMP_DBS_DIR_NAME);
			if (!tempDbsDir.mkdir()) {
				System.err.println("Could not create directory \"" + tempDbsDir.toString() + "\".");
				return false;
			}
			return true;
		} catch(Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	private void cleanupTempFolders() {
		try {
			FileUtils.deleteDirectory(tempDir);	
		} catch(IOException ex) {
			System.err.println("Removing tempdir \"" + tempDir + "\" failed.");
			ex.printStackTrace();
		}
	}
	
	private boolean run_() {
		if (!runPreTestOps())  return false;
		
		File[] templates = templateDir.listFiles((file) -> file.isFile() && file.getName().endsWith(".xlsx"));
		
		if (templates.length == 0) return true;
		
		boolean finalResult = false;
		
		if (createTempFolders()) {
			try {
				boolean result = testFilesImport(templates, tempOutDir.toString(),  tempDbsDir.toString());
				
				System.out.println("Starting post tests ops ...");
				System.out.println("Comparing temp output in '" + tempOutDir.toString() + "' with reference output '" + referenceOutDir.toString() + "' ...");
				result = DiffUtils.compareOrCopyFilesInDir(tempOutDir.toString(), referenceOutDir.toString(), diffDir.toString()) && result;
				result = LanguageDiffUtils.compareLanguageVariants(tempOutDir.toString(), diffDir.toString()) && result;
				finalResult = result;
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
		} 
		if (this.tempDir != null) this.cleanupTempFolders();
		
		return finalResult;
	}
	
	public static boolean run() {
		System.out.println("Running import Tests...");
		
		boolean result = new ImporterTest().run_();
		if (result) {
			System.out.println("ITest for imports succeeded.");
		} else {
			System.err.println("ITest for imports failed.");
		}
		
		return result;
	}
}
