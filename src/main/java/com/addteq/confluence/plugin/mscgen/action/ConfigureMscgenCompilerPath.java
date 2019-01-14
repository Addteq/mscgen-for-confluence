package com.addteq.confluence.plugin.mscgen.action;

import java.io.File;

import com.addteq.confluence.plugin.mscgen.bean.MscgenPathSettings;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.util.i18n.I18NBean;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import org.apache.commons.io.FilenameUtils;

public class ConfigureMscgenCompilerPath extends ConfluenceActionSupport{
	/**
	 * 
	 */
	public static String compilerPath;
	private boolean saveOrUpdateFlag;
	private String error="";
	private String errorHtml;
	private BandanaManager bandanaManager;
	private final I18NBeanFactory i18nBeanFactory;
	private final LocaleManager localeManager;
	private String placeHolder;
	private static final long serialVersionUID = 3200762197401981183L;
	
	public ConfigureMscgenCompilerPath(
			BandanaManager bandanaManager, I18NBeanFactory i18nBeanFactory, LocaleManager localeManager) {
		this.bandanaManager = bandanaManager;
		this.i18nBeanFactory = i18nBeanFactory;
		this.localeManager = localeManager;
	}
	
	@Override
	public String execute() throws Exception {
		if(saveOrUpdateFlag){
			
			File file = new File(compilerPath);
			if(file.toString().equalsIgnoreCase("")){
				MscgenPathSettings mscgenPathSettings = (MscgenPathSettings) bandanaManager.getValue(new ConfluenceBandanaContext(), "com.addteq.confluence.plugin.mscgen.compiler.path.key");
				if(mscgenPathSettings == null) {
					compilerPath = "";
				}
				else {
					compilerPath = mscgenPathSettings.getCompilerPath();
				}
				error = getI18NBean().getText("com.addteq.confluence.plugin.mscgen.compiler.EmptyFilePath");
				return ERROR;
			}
			else if(!file.exists()){
				MscgenPathSettings mscgenPathSettings = (MscgenPathSettings) bandanaManager.getValue(new ConfluenceBandanaContext(), "com.addteq.confluence.plugin.mscgen.compiler.path.key");				
                                if(mscgenPathSettings == null) {
					compilerPath = "";
				}
				else {
					compilerPath = mscgenPathSettings.getCompilerPath();
				}
				error = getI18NBean().getText("com.addteq.confluence.plugin.mscgen.compiler.mscgenExecutableNotFound");				
				return ERROR;
                            }
			else if(file.isFile()){
                                String fileNameWithOutExt = FilenameUtils.removeExtension(file.getName());
				if(!fileNameWithOutExt.equals("mscgen")) {
					MscgenPathSettings mscgenPathSettings = (MscgenPathSettings) bandanaManager.getValue(new ConfluenceBandanaContext(), "com.addteq.confluence.plugin.mscgen.compiler.path.key");
					if(mscgenPathSettings == null) {
						compilerPath = "";
					}
					else {
						compilerPath = mscgenPathSettings.getCompilerPath();
					}
					error = getI18NBean().getText("com.addteq.confluence.plugin.mscgen.compiler.mscgenExecutableNotFound");
					return ERROR;
				}
				else if (!file.canExecute()) {
					MscgenPathSettings mscgenPathSettings = (MscgenPathSettings) bandanaManager.getValue(new ConfluenceBandanaContext(), "com.addteq.confluence.plugin.mscgen.compiler.path.key");
					if(mscgenPathSettings == null) {
						compilerPath = "";
					}
					else {
						compilerPath = mscgenPathSettings.getCompilerPath();
					}
					error = getI18NBean().getText("com.addteq.confluence.plugin.mscgen.compiler.mscgenExecutableNotFound");
					return ERROR;
				}
			}
                        else if (file.isDirectory()) {
                                compilerPath = getMscGenPath(file);
                                if (compilerPath.isEmpty()) {
                                    error = getI18NBean().getText("com.addteq.confluence.plugin.mscgen.compiler.WrongFilePath");
                                return ERROR;
                                }
                        }
			else{
				MscgenPathSettings mscgenPathSettings = (MscgenPathSettings) bandanaManager.getValue(new ConfluenceBandanaContext(), "com.addteq.confluence.plugin.mscgen.compiler.path.key");
				if(mscgenPathSettings == null) {
					compilerPath = "";
				}
				else {
					compilerPath = mscgenPathSettings.getCompilerPath();
				}
				error = getI18NBean().getText("com.addteq.confluence.plugin.mscgen.compiler.WrongFilePath");
				return ERROR;
			}
			MscgenPathSettings path = new MscgenPathSettings();
			path.setCompilerPath(compilerPath);
			error = getI18NBean().getText("com.addteq.confluence.plugin.mscgen.compiler.validpath");
			bandanaManager.setValue(new ConfluenceBandanaContext(), "com.addteq.confluence.plugin.mscgen.compiler.path.key", path);
		}
		else{
			MscgenPathSettings mscgenPathSettings = (MscgenPathSettings) bandanaManager.getValue(new ConfluenceBandanaContext(), "com.addteq.confluence.plugin.mscgen.compiler.path.key");
			if(mscgenPathSettings == null) {
				compilerPath = "";
				error = getI18NBean().getText("com.addteq.confluence.plugin.mscgen.compiler.pathNotSet");
			}
			else {
				compilerPath = mscgenPathSettings.getCompilerPath();
			}
		}	
		return SUCCESS;
	}

        private String getMscGenPath(File file) {
            /*
              To set the executable file extention based on OS.  
            */
            String OS = System.getProperty("os.name");
            String ext = "";
            if (OS.startsWith("Windows")) {
                ext = ".exe";
            }
            String fileNameWithOutExt = FilenameUtils.removeExtension(file.getName());
            
            /* 
             *If the compiler path is set to directory then return the mscgen executable file path under that directory. 
             */
            if (file.isDirectory()) {
                File mscgenFile = new File(file, "mscgen" + ext);
                if (mscgenFile.exists() && mscgenFile.canExecute()) {
                    return mscgenFile.getAbsolutePath();
                }
            } else if (fileNameWithOutExt.toLowerCase().contains("mscgen") && file.canExecute()) {
                return file.getAbsolutePath()+ext;
            } else {
                /*
                    return the last stored mscgen executable file path if found.
                */
                MscgenPathSettings mscgenPathSettings = (MscgenPathSettings) bandanaManager.getValue(new ConfluenceBandanaContext(), "com.addteq.confluence.plugin.mscgen.compiler.path.key");
                if (mscgenPathSettings == null) {
                    return "";
                } else {
                    return mscgenPathSettings.getCompilerPath();
                }
            }
            return "";
        }

	public String getCompilerPath() {
		return compilerPath;
	}

	public void setCompilerPath(String compilerPath) {
		this.compilerPath = compilerPath;
	}

	public String getMessage() {
		return error;
	}

	public void setMessage(String error) {
		this.error = error;
	}
	private I18NBean getI18NBean() {
		return i18nBeanFactory.getI18NBean(localeManager
				.getLocale(AuthenticatedUserThreadLocal.getUser()));
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getErrorHtml() {
		return errorHtml;
	}

	public void setErrorHtml(String errorHtml) {
		this.errorHtml = errorHtml;
	}

	public String getPlaceHolder() {
		return placeHolder;
	}

	public void setPlaceHolder(String placeHolder) {
		this.placeHolder = placeHolder;
	}
	public boolean isSaveOrUpdateFlag() {
		return saveOrUpdateFlag;
	}
	public void setSaveOrUpdateFlag(boolean saveOrUpdateFlag) {
		this.saveOrUpdateFlag = saveOrUpdateFlag;
	}
	
}