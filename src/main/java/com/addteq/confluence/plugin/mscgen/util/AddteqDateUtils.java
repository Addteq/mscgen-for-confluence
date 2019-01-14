package com.addteq.confluence.plugin.mscgen.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AddteqDateUtils {

	public static String formatDate(Date date, String format){
		SimpleDateFormat dateFormat = new SimpleDateFormat(format); 
		return dateFormat.format(date);
	}
	public synchronized static String getImageFileNameWithDateFormat(long miliseconds, String format){
		Date date = new Date(miliseconds);
		String datePartOfImageName = formatDate(date, format);
		return datePartOfImageName;
	}
}
