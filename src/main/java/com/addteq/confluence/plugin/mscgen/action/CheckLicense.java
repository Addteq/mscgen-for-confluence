package com.addteq.confluence.plugin.mscgen.action;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.license.PluginLicenseManager;

public class CheckLicense extends ConfluenceActionSupport {

    private static String LicenseMessage;
    private final PluginLicenseManager licenseManager;
    private String message = "";

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CheckLicense(PluginLicenseManager licenseManager) {
        this.licenseManager = licenseManager;
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
    public String execute() throws Exception {
        checkLicense();
        if (licenseMsg().equalsIgnoreCase("Mscgen is licensed")) {
            message = "LICENSED";
        } else {
            message = "UNLICENSED";
        }
        return message;
    }
}
