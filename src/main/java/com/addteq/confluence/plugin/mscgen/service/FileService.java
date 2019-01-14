package com.addteq.confluence.plugin.mscgen.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.atlassian.confluence.setup.BootstrapManager;
import com.atlassian.confluence.util.ConfluenceHomeGlobalConstants;
import com.atlassian.spring.container.ContainerManager;

public class FileService {
	FileOutputStream fop = null;
	File file;

	public String createFile(String input, File tempDirectory, String fileName) {
		String content = input;
		String filePath="";
		try {
			
			
			//filePath = tempDirectory+fileName;
			
			file = new File(tempDirectory, fileName);
			filePath = file.getAbsolutePath();
			fop = new FileOutputStream(file);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// get the content in bytes
			byte[] contentInBytes = content.getBytes();

			fop.write(contentInBytes);
			fop.flush();
			fop.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fop != null) {
					fop.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return filePath;
	}
	public static File getFile(String filePath){
		File imageFile = new File(filePath);
		return imageFile;
	}
}
