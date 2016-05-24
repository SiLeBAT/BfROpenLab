import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

class ConvertCharset {

	static main(args) {
		convert(new File(".."));
	}

	static void convert(File dir) throws IOException {
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				convert(f);
			} else if (f.getName().toLowerCase().endsWith(".java")) {
				System.out.println(f.getAbsolutePath());

				List<String> lines = new ArrayList<>();
				List<String> lineEndings = new ArrayList<>();
				BufferedReader reader1 = new BufferedReader(new InputStreamReader(
						new FileInputStream(f), "windows-1252"));
				String line;

				while ((line = reader1.readLine()) != null) {
					lines.add(line);
				}

				reader1.close();

				BufferedReader reader2 = new BufferedReader(new InputStreamReader(
						new FileInputStream(f), "windows-1252"));
				int character;

				while ((character = reader2.read()) != -1) {
					if (character == '\n') {
						lineEndings.add("\n");
					} else if (character == '\r' && reader2.read() == '\n') {
						lineEndings.add("\r\n");
					}
				}

				reader2.close();

				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(f), "UTF-8"));

				for (int i = 0; i < lines.size(); i++) {
					if (i < lineEndings.size()) {
						writer.write(lines.get(i) + lineEndings.get(i));
					} else {
						writer.write(lines.get(i));
					}
				}

				writer.close();
			}
		}
	}
}
