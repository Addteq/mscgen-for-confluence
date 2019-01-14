package com.addteq.confluence.plugin.mscgen.servlet;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.File;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;



import com.addteq.confluence.plugin.mscgen.bean.MscgenPathSettings;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.config.util.BootstrapUtils;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import org.apache.commons.io.FilenameUtils;

public class MscgenEditorPreviewUtil {
	
	/**
	 * called when mscgen code has no error, this will convert process->inputStream 
	 * to b64 representing the result image.
	 */
	public static String convertImageInStdout(String mscgen_path, String codePath){
		
		ProcessBuilder pb = new ProcessBuilder();  
		if (mscgen_path == "MSCGEN PATH EMPTY") {
			return "0"+",ERROR:" +"MSCGen compiler path not set.";
		}
		
		else if (mscgen_path == "MSCGEN EXE NOT FOUND") {
			                 
			return "0"+",ERROR:" +"MSCGen compiler not found.";
		}
                String OS = System.getProperty("os.name");
                if(OS.startsWith("Windows")){
                    codePath = codePath.replace("\\","\\\\");
                    /* 
                     * Ref: PLUG-5262
                     * In case of windows environment if we have space in-between the compiler path 
                     * then we need to set parent directory as the directory of ProcessBuilder. 
                    */
                    File f = new File(mscgen_path);
                    pb.directory(f.getParentFile());
                    pb.command("cmd.exe", "/c",f.getName(),"-T","png","-i",codePath);
                }else{
                    pb.command(mscgen_path,"-T","png","-i",codePath);  
                }
                Process p = null;
		try {
			p = pb.start();
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		String error=checkError(p);
		if(error!=""){
			return getErrorLine(error)+",ERROR:"+error;
		}
		BufferedImage image=null;	
		try {
                        File f = new File(codePath+".png");
			image=ImageIO.read(f);
 
		} catch (IOException e) {
			System.out.println("IO exception in read");
		}    
            String imageString = null;
        ByteArrayOutputStream imageInBytes = new ByteArrayOutputStream();
	    try {
			ImageIO.write(image, "png", imageInBytes);
		} catch (IOException e) {
			System.out.println("IO exception in write");
		}
	    byte[] imageBytes = imageInBytes.toByteArray();
	    imageString = DatatypeConverter.printBase64Binary(imageBytes);
	    try {
			imageInBytes.close();
			p.getInputStream().close();
		} catch (IOException e) {
			System.out.println("IO exception in close");
                }
		return imageString;
	}
	/**
	 * catches error
	 * @param p the process
	 * @return error msg
	 */
	private static String checkError(Process p){
		String executionResult="";
		InputStreamReader mscgen_err = new InputStreamReader(p.getErrorStream());
		BufferedReader mscgen_err_buff = new BufferedReader(mscgen_err);
		String stderr;
		try {
			while((stderr = mscgen_err_buff.readLine()) != null){
			    executionResult+=stderr;
			}//end while
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//end while
		
		return executionResult;
	}
	/**
	 * get error line number
	 * only works for certain types
	 * @param errmsg
	 * 
	 */
	private static int getErrorLine(String errmsg){
		return Integer.parseInt(errmsg.substring(0,errmsg.indexOf(":")).substring(errmsg.substring(0,errmsg.indexOf(":")).lastIndexOf(" ")+1));
	}
	
	/**
	 * saves mscgen code from textarea, to a tempfile, for preview only.
	 * 
	 * @param fileName temp file name
	 * @param mscCode origin code text from textarea
	 * 
	 */
	public static String parseCode(String fileName,String mscCode) {
		String tempstring = BootstrapUtils.getBootstrapManager().getApplicationHome();
		File tempDir = new File(tempstring+"/AddteqMscgen");
		File tempcode=null;
		Writer writer;
		if (!tempDir.exists()){
			tempDir.mkdirs();
		}
		try {
			String mscCodeNoWhiteSpace=mscCode.replaceAll("[^\\nA-Za-z0-9;{}=:.*#-\\<\\>,\\|\"\'\\[\\] @%$&?!_()]", "");
			tempcode= new File(tempDir, fileName);
			FileOutputStream tempCodeOutputStream = new FileOutputStream(tempcode);
			writer = new BufferedWriter(new OutputStreamWriter(tempCodeOutputStream));
			writer.write(mscCodeNoWhiteSpace);
			writer.close();
		} catch (FileNotFoundException e) {
			//file not found
			e.printStackTrace();
		} catch (IOException e) {
			//io
			e.printStackTrace();
		}
		return tempcode.getAbsolutePath();	
	}//end saveImg
	
	
	/**
	 * was using this to save setting bean to a serialized file. not used in this version.
	 * @param mscgen_path
	 * @return
	 */
	public static String saveMscgenPath(String mscgen_path){
		//File pluginDir = new File(ConfluenceHomeGlobalConstants.PLUGINS_DIR+"/Addteq-Mscgen-Editor-Settings");
		File pluginDir = new File(BootstrapUtils.getBootstrapManager().getApplicationHome());
		File mscgenSettingFile=null;
		Writer writer;
		if (!pluginDir.exists()){
			pluginDir.mkdirs();
		}
		try {
			
			mscgenSettingFile= new File(pluginDir, "Addteq-MscgenEditorSettings.cfg");
			FileOutputStream mscgenSettingOutputStream = new FileOutputStream(mscgenSettingFile);
			writer = new BufferedWriter(new OutputStreamWriter(mscgenSettingOutputStream));
			writer.write(mscgen_path);
			writer.close();
		} catch (FileNotFoundException e) {
			//file not found
			System.out.println("File not found.");
			//e.printStackTrace();
		} catch (IOException e) {
			//io
			System.out.println("IOException..");
			//e.printStackTrace();
		}
		return mscgenSettingFile.getAbsolutePath();	
	}
	/**
	 * hard coded mscgen path for int.
	 * replace it with API from mscgen macro plugin once updated.
	 * @return
	 */
	public static String getMscgenPath(BandanaManager bandanaManager) throws NullPointerException{
		//replace this with request to addteq mscgen path provider 
		
		MscgenPathSettings mscgenPathSettings = (MscgenPathSettings) bandanaManager.getValue(new ConfluenceBandanaContext(), "com.addteq.confluence.plugin.mscgen.compiler.path.key");
		if (mscgenPathSettings == null) {
			return "MSCGEN PATH EMPTY";
		}
		String filePath = mscgenPathSettings.getCompilerPath();
		File file = new File(filePath);
		if (file.exists()) {
                            
                    String fileNameWithOutExt = FilenameUtils.removeExtension(file.getName());
                    if(!fileNameWithOutExt.equals("mscgen") || !file.canExecute()) {
				
                        return "MSCGEN EXE NOT FOUND";
                    }
                }else {
			return "MSCGEN EXE NOT FOUND";
		}                
		return filePath;
	}       
	}