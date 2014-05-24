package co.vrcode.drfex;

import java.io.File;
import java.io.FileFilter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import co.vrcode.util.Android;
import co.vrcode.util.FileSystem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 * A Fragment that allows browsing the File System
 * and upon a file being selected start an activity
 * that can handle it.
 * 
 * @author VRCode
 *
 */
public class FileListFragment extends Fragment {
	
	private File currentPath;
	private FileAdapter fileAdapter;
	private ArrayList<File> files;
	private ListView filesList;
	private ArrayList<String> fileSelections;
	private HashMap<String, String> pathScrollPosition;

	// TODO: Implement Directory/File selection
	//private int selectedCount = 0;
	//private long selectedSize = 0;

	
	public String getPathScrollPosition() {
		Gson gson = new Gson();
		return gson.toJson(pathScrollPosition);
	}


	public void setPathScrollPosition(String pathScrollPosition) {
		Gson gson = new Gson();
		Type stringString = new TypeToken<HashMap<String, String>>(){}.getType();
		this.pathScrollPosition = gson.fromJson(pathScrollPosition, stringString);
	}


	private View readingView;


	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks _Callbacks = _DummyCallbacks;


	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onDirectorySelected(String id);
	}


	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks _DummyCallbacks = new Callbacks() {
		@Override
		public void onDirectorySelected(String id) {
		}
	};


	/**
	 * Returns a new instance of the File List Fragment
	 */
	public static FileListFragment newInstance() {
		
		FileListFragment fragment = new FileListFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}


	public FileListFragment() {

	}


	/**
	 * Sets the current path to its parent and updates
	 * the Files ListView
	 */
	public void goUp() {
		currentPath = new File(currentPath.getParent());
		loadDirectory();
	}


	/**
	 * Returns the current Path as a String
	 * @return	The current path in String form
	 */
	public String getCurrentPath() {
		return currentPath.toString();
	}


	/**
	 * Performs an immediate directory change to the
	 * provided path, updating the Files ListView.
	 * 
	 * @param newPath	Path to change to
	 */
	public void changeDir(String newPath) {
		currentPath = new File(newPath);
		loadDirectory();
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(
			R.layout.fragment_main,
			container,
			false
		);

		readingView = rootView.findViewById(R.id.dir_loading);

		currentPath = new File("/");
		pathScrollPosition = new HashMap<String, String>();

		files = new ArrayList<File>();
		fileSelections = new ArrayList<String>();

		filesList = (ListView) rootView
			.findViewById(R.id.files_list);

		filesList.setEmptyView(
			rootView.findViewById(R.id.dir_empty)
		);

		// On Scroll Listener used to keep track of the
		// current scrolling position, which is saved and restored
		// when the user goes back to a previous directory
		filesList.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
				if (scrollState == SCROLL_STATE_IDLE) {
					int index = view.getFirstVisiblePosition();
					View top_child = view.getChildAt(0);
					int top = (top_child == null) ? 0 : top_child.getTop();

					// TODO: 	Limit saved scroll positions to 100
					//			and only when position is > 0
					pathScrollPosition.put(
						currentPath.toString(),
						index + "," + top
					);

				}
				
			}

			@Override
			public void onScroll(AbsListView arg0, int position, int arg2, int arg3) {

			}

		});

		//
		// Files List Item Click Listener
		//
		// When a directory is clicked, currentPath is updated
		// and that folder is loaded
		//
		// Otherwise we create an Intent to request the
		// System to open that file if it's associated with
		// an installed App, if not, a list is presented
		// with all the available Apps that may be able
		// to handle the selected item
		//
		filesList.setOnItemClickListener(new OnItemClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {

				File newPath = new File(files.get(position).toString());
				Log.d("--- Dr. Fex ---", "Selected File: " + newPath);
				if (newPath.isDirectory()) {
					currentPath = newPath;
					loadDirectory(true);
				} else {
					Intent openFile = Android.getViewIntentForFile(
						newPath.toString(),
						getActivity()
					);
					startActivity(openFile);
				}

			}
			
		});

		// Perform the first directory load (for the root directory)
		loadDirectory();

		return rootView;
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
				"Activity must implement FileListFragment callbacks.");
		}

		_Callbacks = (Callbacks) activity;
	}


	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		_Callbacks = _DummyCallbacks;
	}


	/**
	 * Starts a loadFiles operation not started from an
	 * item selection (ie. one perfromed programatically)
	 */
	private void loadDirectory() {
		loadDirectory(false);
	}


	/**
	 * Loads files for currentPath.
	 * 
	 * @param fromSelection		Indicates that the operation is being
	 * 							performed as a result of an item
	 * 							selection from the ListView
	 */
	private void loadDirectory(final boolean fromSelection) {
		// Hide FilesList and show "Loading" view
		filesList.setVisibility(View.GONE);
		readingView.setVisibility(View.VISIBLE);		

		// Run the operation on a separate thread
		Thread t = new Thread(new Runnable() {
			public void run() {

				// Get Directories that aren't hidden
				FileFilter filter = new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return !pathname.isHidden() && pathname.isDirectory();
					}
				};
				File[] dirs_list = currentPath.listFiles(filter);
				if (dirs_list == null) {
					dirs_list = new File[0];
				}

				// Get Files that aren't hidden
				filter = new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return !pathname.isHidden() && !pathname.isDirectory();
					}
				};
				File[] files_list = currentPath.listFiles(filter);
				if (files_list == null) {
					files_list = new File[0];
				}

				// Sort Directory & Files alphabetically
				Arrays.sort(dirs_list);
				Arrays.sort(files_list);


				// Clear the current files Collection and
				// add Directories first and then Files
				files.clear();
				files.addAll(Arrays.asList(dirs_list));
				files.addAll(Arrays.asList(files_list));

				// When the files Collection is ready
				// update the UI to reflect the changes
				getActivity().runOnUiThread(new Runnable() {
		            public void run() {
		            	// Create the ListAdapter for the Files
		            	// ListView if it doesn't exist, otherwise
		            	// notify about the changes
						if (fileAdapter == null) {
							fileAdapter = new FileAdapter(
								getActivity(),
								R.layout.file_row,
								files
							);
			            	filesList.setAdapter(fileAdapter);								
						} else {
							fileAdapter.notifyDataSetChanged();
						}

						// If path had been previously visited,
						// restore the scrolling position
						String path = currentPath.toString();
						if (pathScrollPosition.containsKey(path)) {
							String idx_top = pathScrollPosition.get(path);
							String [] index_top = idx_top.split(",");
							int index = Integer.parseInt(index_top[0]);
							int top = Integer.parseInt(index_top[1]);
							
							filesList.setSelectionFromTop(index, top);
						} else {
							if (currentPath.toString() != "/") {
								filesList.setSelection(0);
							}
						}

						// If the operation is the result of the user
						// selecting an item, then notify the Activity
						// so the ActionBar is updated
						if (fromSelection) {
							_Callbacks.onDirectorySelected(currentPath.getName());
						}

						// Hide "Loading" view and show FilesList
						readingView.setVisibility(View.GONE);
						filesList.setVisibility(View.VISIBLE);
		            }
				});
			}
		});

		t.start();
	}


	/**
	 * An ArrayAdapter subclass to handle Directories & Files
	 * 
	 * @author VRCode
	 *
	 */
	private class FileAdapter extends ArrayAdapter<File> {
	    private Activity _activity;
	    private List<File> _items;
	    private int _resourceID;


	    public FileAdapter(Activity activity, int textViewResourceId, List<File> items) {
	        super(activity, textViewResourceId, items);
	        _activity = activity;
	        _items = items;
	        _resourceID = textViewResourceId;
	    }


	    public View getView(int position, View convertView, ViewGroup parent) {
	        View row = convertView;

	        if (row == null) {
	            LayoutInflater inflater = _activity.getLayoutInflater();
	            row = inflater.inflate(_resourceID, null);
	        }

	        TextView file_text = (TextView) row.findViewById(R.id.file_text);
	        ImageView file_icon = (ImageView) row.findViewById(R.id.file_icon);
	        CheckBox file_checkbox = (CheckBox) row.findViewById(R.id.file_checkbox);
            TextView file_date = (TextView) row.findViewById(R.id.file_date);
            TextView file_size = (TextView) row.findViewById(R.id.file_size);

	        final File file = _items.get(position);


        	if (fileSelections.contains(file.getAbsolutePath())) {
        		file_checkbox.setChecked(true);
        	} else {
        		file_checkbox.setChecked(false);
        	}


	        Resources res = _activity.getResources();

	        // Set Directory/File Date
        	Date last_modified = new Date(file.lastModified());
            file_date.setText(
            	String.format("%tb %<td, %<tY", last_modified)
            );

            // Set Directory/File Icon & Size
	        if (file.isDirectory()) {
		        file_icon.setImageDrawable(
		        	res.getDrawable(R.drawable.folder)
		        );

		        file_size.setText("");

	        } else {
	        	String file_name = file.getName(); 

	        	// Assign icon to File according to
	        	// its extension
	        	if (file_name.endsWith(".gz")) {
		        	file_icon.setImageDrawable(
		        		res.getDrawable(R.drawable.gz)
		        	);	        		
	        	} else if (file_name.endsWith(".zip")) {
		        	file_icon.setImageDrawable(
		        		res.getDrawable(R.drawable.zip)
		        	);	        		
	        	} else if (file_name.endsWith(".jar")) {
		        	file_icon.setImageDrawable(
		        		res.getDrawable(R.drawable.jar)
		        	);	        		
	        	} else if (file_name.endsWith(".bmp")) {
		        	file_icon.setImageDrawable(
		        		res.getDrawable(R.drawable.bmp)
		        	);	        		
	        	} else if (file_name.matches(".*\\.(jpg|jpeg)$")) {
		        	file_icon.setImageDrawable(
		        		res.getDrawable(R.drawable.jpg)
		        	);	        		
	        	} else if (file_name.endsWith(".png")) {
		        	file_icon.setImageDrawable(
		        		res.getDrawable(R.drawable.png)
		        	);	        		
	        	} else if (file_name.matches(".*\\.(m4a|mp3|ogg)$")) {
		        	file_icon.setImageDrawable(
		        		res.getDrawable(R.drawable.audio)
		        	);
	        	} else if (file_name.matches(".*\\.(avi|flv|mov|mp4|mpeg|mpg)$")) {
		        	file_icon.setImageDrawable(
		        		res.getDrawable(R.drawable.video)
		        	);	        		
	        	} else {
		        	file_icon.setImageDrawable(
		        		res.getDrawable(R.drawable.empty)
		        	);
	        	}

				file_size.setText(
					FileSystem.getFormattedBytesString(file.length())
				);

	        }

	        // Set Directory/File Name
	        try {
				file_text.setText(
					URLDecoder.decode(file.getName(), "UTF-8")
				);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

	        return row;
	    }
	    
	}

}
