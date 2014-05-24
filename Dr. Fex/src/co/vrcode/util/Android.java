package co.vrcode.util;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;



/**
 * Misc. Android Utilities
 * 
 * @author VRCode
 *
 */
public class Android {
	private static boolean[] sdCardState;


	/**
	 * Checks if there are Activities available to respond to
	 * the passed Intent
	 * 
	 * @param intent	Intent to check
	 * @param context	Context for Activity checking the Intent
	 * 
	 * @return 	true if at least one Activity is available to respond
	 * 			to the Intent
	 * 
	 */
	public static boolean willIntentOpen(Intent intent, Context context) {
		PackageManager pm = context.getPackageManager();

		List<ResolveInfo> activities = pm.queryIntentActivities(
			intent,
			PackageManager.MATCH_DEFAULT_ONLY
		);

		return activities.size() > 0;
	}


	/**
	 * Creates an Intent that can be used to start an Activity
	 * that will open the file passed
	 * 
	 * @param file		File for which the Intent is needed
	 * @param context	Context for the Activity requesting the Intent
	 * 
	 * @return 	An Intent that can be used to start an Activity that
	 * 			can process the file
	 * 
	 */
	public static Intent getViewIntentForFile(String file, Context context) {
		String mime = FileSystem.getMimeType(file);

		Intent intent =  new Intent()
			.setAction(android.content.Intent.ACTION_VIEW)
			.setDataAndType(
				Uri.fromFile(new File(file)),
				mime
			);

		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(
			intent,
			PackageManager.MATCH_DEFAULT_ONLY
		);

		if (activities.size() == 0) { 
			intent.setDataAndType(
				Uri.fromFile(new File(file)),
				"*/*"
			);
		}

		return intent;
	}


	/**
	 * Checks if the SD Card is available
	 * 
	 * @return true if it's possible to access the SD Card
	 * 
	 */
	public static boolean isSDCardAvailable() {
		return getSDCardState()[0];
	}


	/**
	 * Checks write access to the SD Card
	 * 
	 * @return true if it's possible to write to the SD Card
	 * 
	 */
	public static boolean canWriteToSDCard() {
		return getSDCardState()[1];
	}


	/**
	 * Returns SD Card's Access Info
	 * 
	 *  @return Array with element 0 being mounted (true) or not (false), 
	 * 			and element 1 weather there is Read-Write access (true)
	 * 			or Read-Only (false)
	 */
	private static boolean[] getSDCardState() {
		if (sdCardState != null) {
			return sdCardState;
		}

		sdCardState = new boolean[2];

		String cardState = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(cardState)) {
			sdCardState[0] = sdCardState[1] = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(cardState)) {
			sdCardState[0] = true;
			sdCardState[1] = false;
		} else {
			sdCardState[0] = sdCardState[1] = false;
		}

		return sdCardState;
	}


	/**
	 * Returns "true" if the current device is a tablet,
	 * false otherwise
	 * 
	 * @return	True if the current device is a tablet
	 */
	public static boolean isTablet() {
		return false;
		// TODO: Implement Tablet Detection

	}

}
