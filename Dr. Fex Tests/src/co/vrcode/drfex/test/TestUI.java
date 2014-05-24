package co.vrcode.drfex.test;


import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import co.vrcode.drfex.MainActivity;
import co.vrcode.drfex.R;


public class TestUI extends ActivityInstrumentationTestCase2<MainActivity> {

	private MainActivity activity;


	public TestUI() {
		super(MainActivity.class);
	}


	/**
	 * Pauses execution for a number of seconds
	 * 
	 * @param seconds	Number of seconds to wait
	 * 
	 */
	private void waitSeconds(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setActivityInitialTouchMode(false);
		activity = getActivity();
	}


	public void testFilesListClick() {
		// Verify filesList was properly initiated
		final ListView filesList = (ListView) getActivity().findViewById(R.id.files_list);
		assertNotNull(filesList);

		// Verify we have at least 1 child
		View child0 = filesList.getChildAt(0);  //.performClick();
		assertNotNull(child0);

		// Perform click on first item and verify the operation
		// was successful
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				assertTrue(filesList.performItemClick(
					filesList,
					0,
					filesList.getItemIdAtPosition(0)
				));
			}
		});

		// Wait 1 second for click
		waitSeconds(1);

	}

	public void testFilesListGoBack() {
		this.sendKeys(KeyEvent.KEYCODE_BACK);

		waitSeconds(1);
	}
}
