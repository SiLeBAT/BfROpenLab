/*******************************************************************************
 * Copyright (c) 2014-2022 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.update.p2.deploy

class DeployToGitlab {
	
	static REPO = "https://gitlab.bfr.berlin/binrepos/fcl.git";
	static BRANCH = "auto";

	static ARTIFACTS_JAR = "artifacts.jar"
	static CONTENT_JAR = "content.jar"
	static FEATURES = "features"
	static PLUGINS = "plugins"
	
	static isRelease = false;

	static String UPDATE_SITE = "../de.bund.bfr.knime.update.p2"
	

	static main(args) {
		// This code is work in progress
		
		def artifactsFile = new File("${UPDATE_SITE}/${ARTIFACTS_JAR}")
		def contentFile = new File("${UPDATE_SITE}/${CONTENT_JAR}")
		def featuresDir = new File("${UPDATE_SITE}/${FEATURES}")
		def pluginsDir = new File("${UPDATE_SITE}/${PLUGINS}")

		if (!artifactsFile.exists() || !contentFile.exists()) {
			println "p2 files cannot be found"
			return
		}

		String tmpDir = "/tmp"; // System.getProperty("java.io.tmpdir");
		if (!(new File(tmpDir).exists())) {
			throw new Exception("The temp folder '${tmpDir}' does not exist.");
		}
		System.out.println("OS current temporary directory: " + tmpDir);
		
		File buildDir = new File(tmpDir, "fcl-build");
		
		// println "branch-name:"
		// def branch = new Scanner(System.in).nextLine()
		
		boolean resetBranch = true;
		
		//delete directory
		if (buildDir.exists()) {
			System.out.println("Deleting old dir ${buildDir.absolutePath}")
			deleteDirectory(buildDir);
		}
		
		// create dir
		// buildDir.mkdirs();
		// buildDir.absolutePath
		
		
		if (resetBranch) {
			System.out.print("Initializing Git Dir ... ");
			buildDir.mkdir();
			// "cd ${buildDir.absolutePath}"
			executeCmd("git init", buildDir);
			
			System.out.print("Adding remote ... ");
			executeCmd("git remote add origin ${REPO}", buildDir);
			// 	"git init\n" + 
			//	"git remote add origin ${REPO}"
			//);
			
		}
		
		System.out.print("Copying content ... ");
		// copy Content
		copyFile(artifactsFile, new File(buildDir, ARTIFACTS_JAR));
		copyFile(contentFile, new File(buildDir, CONTENT_JAR));
		copyDirectory(featuresDir, new File(buildDir, FEATURES));
		copyDirectory(pluginsDir, new File(buildDir, PLUGINS));
		
		System.out.println("done.");
		
		String commitMsg = "\"FCL commit\"";
		
		System.out.print("Adding files ... ");
		executeCmd("git add .", buildDir);  
		
		System.out.print("Commiting files ... ");
		executeCmd("git commit -m ${commitMsg}", buildDir);
		
		System.out.print("Pushing commit ... ");
		executeCmd("git push -f origin ${BRANCH}", buildDir);
	}
	
	static boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}
	
	private static void copyFile(File sourceFile, File destinationFile)
	throws IOException {
	   
	  InputStream inStream = new FileInputStream(sourceFile);
	  OutputStream outStream = new FileOutputStream(destinationFile); 
	  byte[] buf = new byte[1024];
	  int length;
	  while ((length = inStream.read(buf)) > 0) {
		  outStream.write(buf, 0, length);
	  }
	  inStream.close();
	  outStream.close();
  }
	
	private static void copyDirectory(File sourceDirectory, File destinationDirectory) throws IOException {
		if (!destinationDirectory.exists()) {
			destinationDirectory.mkdir();
		}
		for (String f : sourceDirectory.list()) {
			copyFileOrDirectory(new File(sourceDirectory, f), new File(destinationDirectory, f));
		}
	}
	
	private static void copyFileOrDirectory(File source, File destination) throws IOException {
		if (source.isDirectory()) {
			copyDirectory(source, destination);
		} else {
			copyFile(source, destination);
		}
	}
	
	private static executeCmd(String cmd, File workingDir) {
		def processBuilder=new ProcessBuilder(cmd)
		processBuilder.directory(workingDir)
		processBuilder.inheritIO();
		def process = processBuilder.start()
		
		def result = new StringBuilder(); 
		def error  = new StringBuilder();
		process.consumeProcessOutput(result, error); 
		process.waitForOrKill(15*60*1000); 
		
		if (!error.toString().equals("")) { 
			System.out.println("Error: ${error.toString()}");
			throw new Exception(error.toString());
		}
		System.out.println("done" + (result.length()==0?"":"(${result})") + ".");
		// return result.toString();
	}
}
