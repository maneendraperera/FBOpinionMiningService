package com.om.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;

/**
 * This class is handling the general file related functions.
 * 
 * @author Maneendra
 *
 */
@Component
public class FileLoader {

	public BufferedReader readFile(String path) throws IOException {

		FileReader in = new FileReader(path);
		BufferedReader br = new BufferedReader(in);

		String line;
		while ((line = br.readLine()) != null) {
			System.out.println(line);
		}
		in.close();

		System.out.println(line);
		return br;
	}

	public List<String> readFileAsWords(String path) throws IOException {

		FileReader in = new FileReader(path);
		BufferedReader br = new BufferedReader(in);

		String line;
		List<String> words = new ArrayList<String>();

		while ((line = br.readLine()) != null) {
			words.add(line.toLowerCase());
		}
		in.close();

		return words;
	}

	public String readFileAsString(String path) throws IOException {

		FileReader in = new FileReader(path);
		BufferedReader br = new BufferedReader(in);

		String line;
		StringBuilder sb = new StringBuilder();

		while ((line = br.readLine()) != null) {
			System.out.println(line);
			sb.append(line);
		}
		in.close();

		System.out.println(sb.toString());
		return sb.toString();
	}

	public String fileToPath(String filename) throws UnsupportedEncodingException {
		URL url = FileLoader.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}

	public void writeItemsetToFile(String outputFilePath, List<Itemset> itemset) {

		File file = new File(outputFilePath);

		FileWriter fw = null;
		BufferedWriter bw = null;

		try {

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			for (Itemset set : itemset) {
				bw.write(set.toString());
				bw.newLine();
			}

		} catch (Exception e) {
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

}
