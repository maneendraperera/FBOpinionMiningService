package com.om.common;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class FileWriter {

	/**
	 * This method writes the list of fb comments/aspects to a file.
	 * 
	 * @param writeFilePath
	 *            - file path
	 * @param commentsList
	 *            - fb comments/aspects list
	 */
	public void writeToFile(String writeFilePath, List<String> stringList) {
		Path file = Paths.get(writeFilePath);
		try {
			Files.write(file, stringList, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
