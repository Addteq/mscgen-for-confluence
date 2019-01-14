package com.addteq.confluence.plugin.mscgen.macro;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import net.java.ao.Query;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.IOUtils;

import com.addteq.confluence.plugin.mscgen.service.FileService;
import com.addteq.confluence.plugin.mscgen.service.MscgenWrapper;
import com.addteq.confluence.plugin.mscgen.util.AddteqDateUtils;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.DefaultConversionContext;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.importexport.resource.DownloadResourceWriter;
import com.atlassian.confluence.importexport.resource.WritableDownloadResourceManager;
import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.plugin.services.VelocityHelperService;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.setup.BootstrapManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.util.i18n.I18NBean;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.TokenType;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.User;
import com.addteq.confluence.plugin.mscgen.bean.MscgenImageDB;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.atlassian.upm.api.license.entity.PluginLicense;

public class MscgenMacro extends BaseMacro implements Macro {

    private final VelocityHelperService velocityHelperService;
    private AttachmentManager attachmentManager;
    private ContentPropertyManager contentPropertyManager;
    private final ActiveObjects ao;
    private final WritableDownloadResourceManager downloadResourceManager;
    private final LocaleManager localeManager;
    private final I18NBeanFactory i18nBeanFactory;
    private BandanaManager bandanaManager;
    private static String LicenseMessage;
    private final PluginLicenseManager licenseManager;

    public MscgenMacro(VelocityHelperService velocityHelperService, ActiveObjects ao, AttachmentManager attachmentManager,
            WritableDownloadResourceManager downloadResourceManager, LocaleManager localeManager, I18NBeanFactory i18nBeanFactory,
            BandanaManager bandanaManager, PluginLicenseManager licenseManager) {
        this.ao = ao;
        this.velocityHelperService = velocityHelperService;
        this.attachmentManager = attachmentManager;
        this.downloadResourceManager = downloadResourceManager;
        this.i18nBeanFactory = i18nBeanFactory;
        this.localeManager = localeManager;
        this.bandanaManager = bandanaManager;
        this.licenseManager = licenseManager;
    }

    @Override
    public TokenType getTokenType(Map parameters, String body, RenderContext context) {
        return TokenType.INLINE;
    }

    public boolean hasBody() {
        return true;
    }

    public RenderMode getBodyRenderMode() {
        return RenderMode.allow(RenderMode.F_IMAGES);
    }

    private boolean checkLicense() {
        try {

            //Check and see if a license is currently stored.
            //This accessor method can be used whether or not a licensing-aware UPM is present.
            if (licenseManager.getLicense().isDefined()) {
                PluginLicense pluginLicense = licenseManager.getLicense().get();
                //Check and see if the stored license has an error. If not, it is currently valid.
                if (pluginLicense.getError().isDefined()) {
                    //A license is currently stored, however, it is invalid (e.g. expired or user count mismatch)
                    LicenseMessage = "License Error: " + pluginLicense.getError().get().name();
                    return false;
                } else {
                    //A license is currently stored and it is valid.
                    LicenseMessage = "Mscgen is licensed";
                    return true;
                }
            } else {
                //No license (valid or invalid) is stored.
                LicenseMessage = "No license.";
                return false;
            }
        } catch (Exception e) {
            //The current license status cannot be retrieved because the Plugin License Storage plugin is unavailable.
            LicenseMessage = "PluginLicenseException. Please speak to a system administrator.";
            return false;
        }
    }

    public String licenseMsg() {
        return LicenseMessage;
    }

    @Override
    public String execute(Map arg0, String arg1, RenderContext arg2) throws MacroException {
        try {
            return execute(arg0, arg1, new DefaultConversionContext(arg2));
        } catch (MacroExecutionException e) {
            throw new MacroException(e.getMessage());
        }
    }

    @Override
    public String execute(Map<String, String> parameters, String body, ConversionContext conversionContext) throws MacroExecutionException {
        long mscId = System.currentTimeMillis();
        User user = AuthenticatedUserThreadLocal.get();
        ContentEntityObject contentEntityObject = conversionContext.getEntity();
        Long contentEntityObjectID = contentEntityObject.getId();
        attachmentManager = (AttachmentManager) ContainerManager.getComponent("attachmentManager");
        Map<String, Object> contextMap = MacroUtils.defaultVelocityContext();
        String checkSumFromMacro = null;
        body = body.replaceAll(String.valueOf((char) 160), "");

        if (!checkLicense()) {
            String errorMessage = licenseMsg();
            contextMap.put("body", errorMessage);
            return VelocityUtils.getRenderedTemplate("template/LicenseError.vm", contextMap);
        }

        if (body != null && !body.isEmpty() && !body.trim().isEmpty()) {
            if (body.indexOf("msc") == -1) {
                String errorMessage = getMacroError("com.addteq.confluence.plugin.mscgen.macro.invalidMscCode");
                contextMap.put("body", errorMessage);
                return VelocityUtils.getRenderedTemplate("template/mscgen.vm", contextMap);
            }
            String[] mscCodeArray = body.split("msc");
            StringBuilder macroBody = new StringBuilder();

            for (int index = 0; index < mscCodeArray.length; index++) {
                String mscCodeBlock = mscCodeArray[index];
                if (mscCodeBlock.contains(" {") && mscCodeBlock.contains("}")) {
                    String mscCodeOnly = mscCodeBlock.substring(0, mscCodeBlock.lastIndexOf("}") + 1);
                    String mscCode = "msc" + mscCodeOnly;
                    String newLine = System.getProperty("line.separator");

                    String[] mscCodeSplited = mscCode.split(newLine);
                    StringBuilder sb = new StringBuilder();
                    for (int index1 = 0; index1 < mscCodeSplited.length; index1++) {
                        String msc = mscCodeSplited[index1];
                        Pattern pAlphaNumeri = Pattern.compile("[a-zA-Z0-9]");

                        String special = "!@$%^&*()_|{}[]";
                        String pSpecialChar = ".*[" + Pattern.quote(special) + "].*";
                        boolean hasChar = pAlphaNumeri.matcher(msc).find();
                        boolean hasSpecialChar = msc.matches(pSpecialChar);
                        if (hasChar || hasSpecialChar) {
                            msc = msc.replaceAll(String.valueOf((char) 160), "  ");
                            msc = msc.replaceAll("\t+", "");
                            msc = msc.replaceAll("\\s+$", "");
                            msc = msc.replaceAll("\\s+^", "  ");
                            sb.append(msc).append(System.getProperty("line.separator"));
                        }
                    }

                    checkSumFromMacro = DigestUtils.md5Hex(sb.toString());
                    MscgenImageDB[] mscImageDbList = ao.find(MscgenImageDB.class, Query.select().where("CHECK_SUM = ? AND PAGE_ID = ?", checkSumFromMacro, contentEntityObjectID));

                    String fileName = Long.toString(mscId) + ".msc";
                    BootstrapManager bootstrapManager = (BootstrapManager) ContainerManager.getComponent("bootstrapManager");
                    File tempDirectory = new File(bootstrapManager.getLocalHome(), "/AddteqMscgen");
                    SettingsManager settingsManager = (SettingsManager) ContainerManager.getComponent("settingsManager");
                    String baseUrl = settingsManager.getGlobalSettings().getBaseUrl();
                    String destFileType = "png";
                    if (mscImageDbList.length == 0) {

                        /*
                         * Create image and attach it to the page
                         */
                        String sourceFilePath = new FileService().createFile(
                                mscCode, tempDirectory, fileName);
                        String pngFileName = AddteqDateUtils.getImageFileNameWithDateFormat(mscId, getI18NBean().getText("com.addteq.confluence.plugin.mscgen.macro.nameFormatForImage"));
                        String destFilePath = tempDirectory + File.separator + pngFileName;
                        mscCodeBlock = generateMacroBody(mscCodeBlock, sourceFilePath, destFileType, destFilePath, baseUrl, pngFileName,
                                contentEntityObjectID, mscId, user, checkSumFromMacro, contentEntityObject, mscCode);
                    } else {
                        /*
                         * Do nothing, because the checkSum in database and one
                         * which is calculate from macro body is same. Implies
                         * that the msccode has not been edited neither has been
                         * added
                         */
                        String pngFileName = AddteqDateUtils.getImageFileNameWithDateFormat(mscImageDbList[0].getMacroId(),
                                getI18NBean().getText("com.addteq.confluence.plugin.mscgen.macro.nameFormatForImage"));
                        Attachment tempatt = attachmentManager.getAttachment(contentEntityObject, pngFileName);
                        //if attachment still exists
                        if (tempatt != null) {
                            BufferedImage bufferedImage;
                            int height = 400;
                            int width = 300;
                            try {
                                bufferedImage = ImageIO.read(attachmentManager.getAttachmentData(tempatt));
                                height = bufferedImage.getHeight();
                                width = bufferedImage.getWidth();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String src = baseUrl + "/download/attachments/" + contentEntityObjectID + File.separator + pngFileName;
                            String srctag = "<img src='" + src + "' width='" + width + "' height='" + height + "'>";
                            mscCodeBlock = srctag;
                        } //attachment doesnt exist
                        else {
                            String sourceFilePath = new FileService().createFile(mscCode, tempDirectory, fileName);
                            String destFilePath = tempDirectory + File.separator + pngFileName;
                            mscCodeBlock = generateMacroBody(mscCodeBlock, sourceFilePath, destFileType, destFilePath, baseUrl, pngFileName,
                                    contentEntityObjectID, mscId, user, checkSumFromMacro, contentEntityObject, mscCode);
                        }
                    }
                }

                macroBody.delete(0, macroBody.length());
                macroBody.append(mscCodeBlock);
            }
            contextMap.put("body", macroBody);
        } else {
            /*
             * Throw error "macro is empty"
             */
            contextMap.put("body", "");
        }

        return VelocityUtils.getRenderedTemplate("template/mscgen.vm", contextMap);
    }

    private String generateMacroBody(String mscCodeBlock, String sourceFilePath, String destFileType, String destFilePath, String baseUrl, String pngFileName,
            long contentEntityObjectID, long mscId, User user, String checkSumFromMacro, ContentEntityObject contentEntityObject, String mscCode) {

        int retVal = new MscgenWrapper(bandanaManager).mscToImage(sourceFilePath, destFileType, destFilePath);

        if (retVal == 0) {
            try {
                MscgenImageDB mscImageDb = createMSCImageDb(mscCode, checkSumFromMacro, mscId, contentEntityObjectID);
                InputStream inputStream = new FileInputStream(destFilePath);
                Attachment attachment = attachImage(contentEntityObject, pngFileName, inputStream, user);
                BufferedImage bufferedImage = ImageIO.read(attachmentManager.getAttachmentData(attachment));
                int height = bufferedImage.getHeight();
                int width = bufferedImage.getWidth();
                String src = baseUrl + "/download/attachments/" + contentEntityObject.getId() + File.separator + pngFileName;
                String srctag = "<img src='" + src + "' width='" + width + "' height='" + height + "'>";
                mscCodeBlock = srctag;
                inputStream.close(); //Close the inputStream of destFile first before deleting the file.
                File destFile = new File(destFilePath);
                destFile.delete();
                File sourceFile = new File(sourceFilePath);
                sourceFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (retVal == 1) {
            String errorMessage = getMacroError("com.addteq.confluence.plugin.mscgen.macro.invalidMscCode");
            mscCodeBlock = errorMessage;
        } else if (retVal == 127) {
            String errorMessage = getMacroError("com.addteq.confluence.plugin.mscgen.macro.mscgenNotFound");
            mscCodeBlock = errorMessage;
        }

        return mscCodeBlock;
    }

    private String getMacroError(String macroErrorHeader, String macroError) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("<div class=\"aui-message error\">")
                    .append("<p class=\"title\">")
                    .append("<span class=\"aui-icon icon-error\"></span>")
                    .append("<strong>")
                    .append(getI18NBean().getText(macroErrorHeader))
                    .append("</strong>")
                    .append("</p>")
                    .append("<p>")
                    .append(getI18NBean().getText(macroError))
                    .append("</p>")
                    .append("</div>");
        return errorMessage.toString();
    }

    private String getMacroError(String macroError) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("<div class=\"aui-message error\">")
                    .append("<p class=\"title\">")
                    .append("<span class=\"aui-icon icon-error\"></span>")
                    .append("<strong>").append(getI18NBean().getText(macroError))
                    .append("</strong>")
                    .append("</p>");
        return errorMessage.toString();
    }

    private StringBuilder renderImage(Map parameters, ContentEntityObject contentObject, String imageName) {
        String imageFormat = getStringParameter(parameters, "imageformat", "png");
        DownloadResourceWriter downloadResourceWriter = downloadResourceManager.getResourceWriter(StringUtils.defaultString(AuthenticatedUserThreadLocal.getUsername()), imageName, '.' + imageFormat);
        OutputStream outputStream = null;
        outputStream = downloadResourceWriter.getStreamForWriting();
        Attachment attachment = attachmentManager.getAttachment(contentObject, imageName);
        int height = 100;
        int width = 100;
        try {
            BufferedImage bufferedImage = ImageIO.read(attachmentManager.getAttachmentData(attachment));
            height = bufferedImage.getHeight();
            width = bufferedImage.getWidth();
            ImageIO.write(bufferedImage, imageFormat, outputStream);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder imageBuilder = new StringBuilder();
        imageBuilder.append(String.format("<img src=\"%s\" width=\"%d\" height=\"%d\">", downloadResourceWriter.getResourcePath(), width, height));
        return imageBuilder;
    }

    private MscgenImageDB createMSCImageDb(final String body, final String checkSum, final long id, final long pageId) {
        final MscgenImageDB mscImage = ao.create(MscgenImageDB.class);
        mscImage.setMacroData(new ByteArrayInputStream(body.getBytes()));
        mscImage.setMacroId(id);
        mscImage.setPageId(pageId);
        mscImage.setCreated(AddteqDateUtils.formatDate(Calendar.getInstance().getTime(),
                getI18NBean().getText("com.addteq.confluence.plugin.mscgen.macro.ImageDbDateFormat")));
        mscImage.setCheckSum(checkSum);
        mscImage.save();
        return mscImage;
    }

    private synchronized Attachment attachImage(ContentEntityObject contentEntityObject, String fileName, InputStream imageInputStream, User user) throws IOException {

        Attachment attachment = null;

        if (attachment == null) {
            attachment = new Attachment();
        }
        attachment.setMediaType("image/png");
        attachment.setFileName(fileName);
        byte[] content = IOUtils.toByteArray(imageInputStream);
        attachment.setFileSize(content.length);
        Date date = Calendar.getInstance().getTime();
        attachment.setCreationDate(date);
        attachment.setLastModificationDate(date);
        attachment.setCreator((ConfluenceUser) user);
        contentEntityObject.addAttachment(attachment);
        ByteArrayInputStream bais = new ByteArrayInputStream(content);

        try {
            attachmentManager.saveAttachment(attachment, null, bais);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return attachment;
    }

    String getStringParameter(Map parameters, String param, String def) {
        String result = def;
        if (!StringUtils.isEmpty((String) parameters.get(param))) {
            result = (String) parameters.get(param);
        }
        return result;
    }

    private I18NBean getI18NBean() {
        return i18nBeanFactory.getI18NBean(localeManager.getLocale(AuthenticatedUserThreadLocal.get()));
    }

    private Map<String, Object> getMacroVelocityContext() {
        return velocityHelperService.createDefaultVelocityContext();
    }

    @Override
    public BodyType getBodyType() {
        return BodyType.PLAIN_TEXT;
    }

    @Override
    public OutputType getOutputType() {
        return OutputType.INLINE;
    }

    public AttachmentManager getAttachmentManager() {
        return attachmentManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public ContentPropertyManager getContentPropertyManager() {
        return contentPropertyManager;
    }

    public void setContentPropertyManager(ContentPropertyManager contentPropertyManager) {
        this.contentPropertyManager = contentPropertyManager;
    }

}