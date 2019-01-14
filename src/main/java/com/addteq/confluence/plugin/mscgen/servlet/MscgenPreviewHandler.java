package com.addteq.confluence.plugin.mscgen.servlet;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.util.ConfluenceHomeGlobalConstants;

/**
 * a httpservlet that handles preview requests.
 * very simple and raw.
 * @author mian.yang
 *
 */
public class MscgenPreviewHandler extends HttpServlet{
	
    /**
	 * 
	 */
	
	private static final long serialVersionUID = 1276519239240783L;
	private BandanaManager bandanaManager;
	
	public MscgenPreviewHandler(BandanaManager bandanaManager){
		this.bandanaManager = bandanaManager;
		
	}
	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
		
//		String config=req.getParameter("config");
//		String settingFile=MscgenEditorPreviewUtil.saveMscgenPath(config);
//		
//        resp.setContentType("text/html");
//        resp.getWriter().write("<html><body>saved: "+config+"\n"
//        		+"at "+settingFile+"\n"
//        		+"</body>"
//        		+ "</html>");
        
    }
    
	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {

		StringBuilder mscgenCode = new StringBuilder();
	    BufferedReader MscgenCodeReader = req.getReader();
	    try {
	        String line;
	        while ((line = MscgenCodeReader.readLine()) != null) {
	        	mscgenCode.append(line).append('\n');
	        }
	    } finally {
	    	MscgenCodeReader.close();
	    }
	    String cuname=req.getParameter("cuname");    
	    String fpath=MscgenEditorPreviewUtil.parseCode(cuname,mscgenCode.toString());
	  
	    
	    String imageString=MscgenEditorPreviewUtil.convertImageInStdout(MscgenEditorPreviewUtil.getMscgenPath(bandanaManager), fpath);   
	    
        resp.setContentType("text");
        resp.getWriter().write(imageString);
        File fpathFile = new File(fpath);
        fpathFile.delete();
        File imageFile = new File(fpath+".png");
        imageFile.delete();
    }
    
    
 
}