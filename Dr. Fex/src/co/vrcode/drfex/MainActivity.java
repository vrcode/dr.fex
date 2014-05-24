package co.vrcode.drfex;


import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Main Activity for Dr. Fex, an Open Source Android
 * File System Explorer
 * 
 * @author VRCode
 *
 */
public class MainActivity extends ActionBarActivity implements
	ActionBar.OnNavigationListener,
	FileListFragment.Callbacks
	{

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current path
	 */
	private static final String CURRENT_PATH = "current_path";
	private static final String SCROLLING_POSITIONS = "scrolling_positions";
	
	private static final int NAV_ITEM_ICON_ROOT_INDENT = 6;
	private static final int NAV_ITEM_ICON_FOLDER_INDENT = 30;
	
	private static final int NAV_ITEM_LABEL_INDENT = 15;
	
	private static final String APP_DEBUG_TAG = "--- Dr. Fex ---";

	private NavListAdapter navListAdapter;
	private LinkedList<String> navListItems;
	private FileListFragment fileListFragment;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		setContentView(R.layout.activity_main);

		Log.d(APP_DEBUG_TAG, " - onCreate");

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(
			new ColorDrawable(Color.parseColor("#aa000000"))
		);

    	navListItems = new LinkedList<String>();
    	navListItems.add("/");

		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);


		navListAdapter = new NavListAdapter(
			actionBar.getThemedContext(),
			R.layout.nav_item,
			R.id.action_name,
			navListItems
		);

		// Set up the Navigation List in the Action Bar.
		actionBar.setListNavigationCallbacks(
			navListAdapter,
			this
		);

		
		if (fileListFragment == null) {
			Log.d(APP_DEBUG_TAG, "Creating FileListFragment");

			fileListFragment = FileListFragment.newInstance();
	        getSupportFragmentManager()
				.beginTransaction()
				.replace(
					R.id.container,
					fileListFragment
				).commit();

		}

		// Forcing use of overflow menu per change of policy
		// in KitKat
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			// Ignoring Error
		}
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Save current Path so it can be restored if Activity is closed
		
		String currentPath = fileListFragment.getCurrentPath();

		Log.d(APP_DEBUG_TAG, "Saving State for current path: " + currentPath);

		outState.putString(
			CURRENT_PATH,
			currentPath
		);

//		Log.d(APP_DEBUG_TAG, "ScrollPositions: " + fileListFrag.getPathScrollPosition());
		outState.putString(
			SCROLLING_POSITIONS,
			fileListFragment.getPathScrollPosition()
		);
	}


	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore current Path so if the App is closed by the
		// System while in the background, the user can go back
		// to the directory being browsed previously

		Log.d(APP_DEBUG_TAG, " - Restoring State");

		if (savedInstanceState.containsKey(CURRENT_PATH)) {
			changeDir(
				savedInstanceState.getString(CURRENT_PATH)
			);
		}

		if (savedInstanceState.containsKey(SCROLLING_POSITIONS)) {
			fileListFragment.setPathScrollPosition(
				savedInstanceState.getString(SCROLLING_POSITIONS)
			);
		}

	}


// TODO: Implement App Menu for future functionality
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu
//		getMenuInflater().inflate(
//			R.menu.main, menu
//		);
//		return true;
//	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_go_to_data_data) {
			changeDir("/data/data");
			return true;

		} else if (id == android.R.id.home) {
			goUp();

			return true;
		}

		return super.onOptionsItemSelected(item);
	}


	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the a Nav. List item is selected, load the 
		// corresponding directory

		// If the selected item is not the last one,
		// remove all the items ahead of it and change
		// to that directory
		int itemsCount = navListItems.size() - 1; 
		if (position < itemsCount) {

			if (navListItems.size() > 1) {

				for (int i = itemsCount; i > position; i--) {
					navListItems.removeLast();
				}

				navListAdapter.notifyDataSetChanged();

				updateActionBar();
			}			

			fileListFragment.changeDir(TextUtils.join("/", navListItems));
			
			return true;
		}

		return false;
	}


	/**
	 * Callback used by the FilesListFragment to notify
	 * that a new directory is being loaded. The UI is
	 * then updated accordingly.
	 */
	@Override
	public void onDirectorySelected(final String newDir) {
		navListItems.add(newDir);
		navListAdapter.notifyDataSetChanged();

		ActionBar actionBar = getSupportActionBar();
		actionBar.setSelectedNavigationItem(navListItems.size());
		actionBar.setDisplayHomeAsUpEnabled(true);		
	}


	/**
	 * Custom handling of Back key taps
	 */
	@Override
	public void onBackPressed() {
		if (navListItems.size() > 1) {
			goUp();
		} else {
			// TODO:	If the root directory has been reached
			//			the user should be asked whether or not
			//			the App should be closed
			super.onBackPressed();
		}
	};


	/**
	 * Removes the last item from the Navigation Item
	 * List, and requests the File List Fragment to update
	 * its Files list
	 */
	private void goUp() {
		if (navListItems.size() > 1) {
			navListItems.removeLast();
			navListAdapter.notifyDataSetChanged();

			fileListFragment.goUp();

			updateActionBar();
		}
		
	}


	private void changeDir(String newDir) { 

		fileListFragment.changeDir(newDir);

		if (newDir != "/") {
			navListItems.clear();
			navListItems.addAll(
				Arrays.asList(newDir.split("/"))
			);

			if (navListItems.size() > 0) {
				navListItems.set(0, "/");
			}
		}

		navListAdapter.notifyDataSetChanged();

		updateActionBar();
	}


	/**
	 * Update the navigation bar according to
	 * the current path. Anything other than root (/)
	 * will enable the back button.
	 */
	private void updateActionBar() {

		ActionBar actionBar = getSupportActionBar();

		actionBar.setSelectedNavigationItem(navListItems.size());

		if (navListItems.size() > 1) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		} else {
			actionBar.setDisplayHomeAsUpEnabled(false);
			actionBar.setHomeButtonEnabled(false);
		}
	}

	/**
	 * Inner class for the ListView displayed when the Nav. List
	 * menu in the ActionBar is tapped.
	 * 
	 * @author VRCode
	 * 
	 */
	public class NavListAdapter extends ArrayAdapter<String> {

		Context myContext;
		private LinkedList<String> items;

		public NavListAdapter(Context context, int resource, int textViewResourceId,
				LinkedList<String> objects) {
			super(context, resource, textViewResourceId, objects); 
			myContext = context;
			this.items = objects;
		}


		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			return getViewForPosition(position, parent);
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return getViewForPosition(position, parent);
		}


		private View getViewForPosition(int position, ViewGroup parent) {
			LayoutInflater inflater = 
					(LayoutInflater)myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View row = inflater.inflate(R.layout.nav_item, parent, false);

			String label_str = this.items.get(position);
			TextView label = (TextView)row.findViewById(R.id.action_name);
			label.setText(label_str);

			ImageView icon = (ImageView)row.findViewById(R.id.action_icon);

			// For the top level (root directory) a smart phone icon
			// is being used, anything else will display a folder icon.
			// This will change in the future as additional
			// support is added
			if (label_str == "/") {
				icon.setImageResource(R.drawable.smartphone);
			} else {
				icon.setImageResource(R.drawable.folder);
			}

			// All folders under the root directory will be indented
			if (position > 0) {
				icon.setPadding(NAV_ITEM_ICON_FOLDER_INDENT, 0, 0, 0);
			} else {
				icon.setPadding(NAV_ITEM_ICON_ROOT_INDENT, 0, 0, 0);
			}

			label.setPadding(NAV_ITEM_LABEL_INDENT, 0, 0, 0);

			return row;			
		}
	}

}
