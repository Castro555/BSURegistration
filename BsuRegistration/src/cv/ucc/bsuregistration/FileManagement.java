package cv.ucc.bsuregistration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FileManagement {
	
	public static void splitFile(String inputFile, int numParts) throws IOException {
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			reader.close();
		}
		
		int totalLines = lines.size();
		int linesPerFile = (int) Math.ceil( (double) totalLines / numParts);
		
		for (int i = 0; i< numParts; i++) {
			int start = i * linesPerFile;
			int end = Math.min(start + linesPerFile, totalLines);
			
			try (BufferedWriter writer = new BufferedWriter(new FileWriter("part_"+i+".txt"))) {
				for(int j = start; j < end; j++) {
					writer.write(lines.get(j));
					writer.newLine();
				}
				writer.close();
			}
		}
		
		System.out.println("Ficheiro dividido em " + numParts + " partes");
	}
	
	public static void mergeFile(int numParts) throws IOException {
		Path output = Paths.get("merge.txt");
		
		Files.deleteIfExists(output);
		
		for(int i = 0; i < numParts; i++) {
			
			if (Files.exists(Paths.get("registration_"+i+".txt"), LinkOption.NOFOLLOW_LINKS)) {
				Files.write(output, Files.readAllBytes(Paths.get("registration_"+i+".txt")), 
						StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			}
		}
		
		System.out.println("Merge conluído.");
		
		for(int i = 0; i < numParts; i++) {
			Files.deleteIfExists(Paths.get("part_"+i+".txt"));
			Files.deleteIfExists(Paths.get("registration_"+i+".txt"));
		}
		System.out.println("Ficheiros complementares eliminados.");
	}
}
