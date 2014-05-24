package co.vrcode.util;


import java.net.FileNameMap;
import java.net.URLConnection;
import java.text.DecimalFormat;
import android.annotation.SuppressLint;
import android.webkit.MimeTypeMap;






/**
 * File System Utilities
 * 
 * @author VRCode
 *
 */

public class FileSystem {

	
	/**
	 * Returns a human readable byte number representation.<br/>
	 * Numbers larger than 999 TB are currently not supported.
	 * 
	 * @param bytesNumber	Plain bytes number
	 * 
	 * @return A human readable bytes representation
	 * 
	 */
	public static String getFormattedBytesString(long bytesNumber) {
		if (bytesNumber < 0) {
			return "Unknown";
		}

		String ret = "";
		DecimalFormat decimal = new DecimalFormat("0.00");
		Number size = bytesNumber;
		int step = 0;

		if (size.longValue() >= 1024) {
			size = size.longValue() / 1024;
			step++;
		}

		if (size.longValue() >= 1024) {
			size = size.floatValue() / 1024;
			step++;
		}

		if (size.longValue() >= 1024) {
			size = size.floatValue() / 1024;
			step++;
		}

		if (size.longValue() >= 1024) {
			size = size.floatValue() / 1024;
			step++;
		}

		switch (step) {
			case 0:
				ret = size + " bytes";
				break;
			case 1:
				ret = size + " KB";
				break;
			case 2:
				ret = decimal.format(size) + " MB";
				break;
			case 3:
				ret = decimal.format(size) + " GB";
				break;
			case 4:
				ret = decimal.format(size) + " TB";
				break;
		}

		return ret;
	}


	/**
	 * Returns the Mime type for a file string.
	 * @param file	A file String
	 * @return	The detected Mime type for the passed file string
	 * 			or "application/octet-stream" otherwise
	 */
	public static String getMimeType(String file) {
		String ext = getExtensionForFile(file);
		String mime = MimeTypeMap
			.getSingleton()
			.getMimeTypeFromExtension(ext);

		if (mime == null) {
			FileNameMap fnm = URLConnection.getFileNameMap();
			mime = fnm.getContentTypeFor(file);
		}

		if (mime == null) {
			mime = "application/octet-stream";
		}

		return mime;
	}


	/**
	 * Returns extension for the passed File
	 * 
	 * @param file	File to get the extension from 
	 * 
	 * @return File extension
	 */
	@SuppressLint("DefaultLocale")
	public static String getExtensionForFile(String file) {
		String ret = "";
		String fileName = file.substring(file.lastIndexOf("/") + 1); //  fileObj.getName(); 
		int dotIndex = fileName.lastIndexOf(".");

		// Dot (.) must not be the first character
		// If it is, it's not an extension, but part
		// of the file name
		if (dotIndex > 0) {
			ret = fileName.substring(dotIndex + 1).toLowerCase();
		}

		return ret;
	}

}
