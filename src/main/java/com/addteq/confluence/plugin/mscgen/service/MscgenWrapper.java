package com.addteq.confluence.plugin.mscgen.service;

import java.io.IOException;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.addteq.confluence.plugin.mscgen.bean.MscgenPathSettings;
import java.io.File;

public class MscgenWrapper {
	
	private BandanaManager bandanaManager;
	
    
	public MscgenWrapper(BandanaManager bandanaManager) {
		
		this.bandanaManager = bandanaManager;

	}
	
	public int mscToImage(String sourceFilePath, String imageType, String destFilePath){
		ProcessBuilder pb = new ProcessBuilder();
		int retValue=-1;
		
		/*
		 * For RND purpose, I am using absolute path of mscgen executalbe and output directory.
		 * Need to change before running in other workstations. 
		 * */
		//BootstrapManager bootstrapManager = (BootstrapManager)ContainerManager.getComponent("bootstrapManager");
		
		//For the time being need to give executable permission on mscgen on the given path mannually
		//Need to bundle it with confluence resource or/else use cgi in confluence.
		MscgenPathSettings mscgenPathSettings = (MscgenPathSettings) bandanaManager.getValue(new ConfluenceBandanaContext(), "com.addteq.confluence.plugin.mscgen.compiler.path.key");
		
		String compilerPath = mscgenPathSettings.getCompilerPath(); 
                String OS = System.getProperty("os.name");
                if(OS.startsWith("Windows")){
                    /* 
                     * Ref: PLUG-5262
                     * In case of windows environment if we have space in-between the compiler path 
                     * then we need to set parent directory as the directory of ProcessBuilder. 
                    */
                    File f = new File(compilerPath);
                    pb.directory(f.getParentFile());
                    pb.command("cmd.exe", "/c",f.getName(),"-T",imageType,"-i",sourceFilePath,"-o",destFilePath);
                }else{
                    pb.command(compilerPath,"-T",imageType,"-i",sourceFilePath,"-o",destFilePath);  
                }
                
		Process process;
		try {
			process = pb.start();
			retValue = process.waitFor();
			//System.out.println("retValue: "+retValue);
			//System.out.println(""+pb.redirectErrorStream());
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (InterruptedException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return retValue;
	}
	

//	public static void main(String args[]){
//		new MscgenWrapper().mscToImage("/tmp/mscgen/msc/colour_sample.msc", "png", "/tmp/mscgen/png/test1");
//	}
}
