package com.addteq.confluence.plugin.mscgen.bean;

import java.io.Serializable;

public class MscgenPathSettings implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4992450637021438838L;
	private String compilerPath;

	public String getCompilerPath() {
		return compilerPath;
	}

	public void setCompilerPath(String compilerPath) {
		
		this.compilerPath = compilerPath;
	}

}
