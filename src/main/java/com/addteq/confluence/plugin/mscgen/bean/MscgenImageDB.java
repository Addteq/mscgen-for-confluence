package com.addteq.confluence.plugin.mscgen.bean;

import java.io.InputStream;
import net.java.ao.Entity;
import net.java.ao.Preload;

@Preload
public interface MscgenImageDB extends Entity{

	public Long getMacroId();
	public void setMacroId(Long macroId);
	
	public InputStream getData();
	public void setMacroData(InputStream macroData);

	public String getCreated();
	public void setCreated(String created);
	
	public String getModified();
	public void setModified(String modified);
	
	public String getCheckSum();
	public void setCheckSum(String checkSum);
	
	public Long getPageId();
	public void setPageId(Long pageId);
	
}
