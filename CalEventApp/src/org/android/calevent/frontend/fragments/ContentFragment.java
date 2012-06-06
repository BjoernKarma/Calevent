/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.android.calevent.frontend.fragments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

import org.android.calevent.frontend.MainActivity;
import org.android.calevent.frontend.R;
import org.android.calevent.stub.Directory;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

/** Fragment that shows the content selected from the TitlesFragment.
 * When running on a screen size smaller than "large", this fragment is hosted in
 * ContentActivity. Otherwise, it appears side by side with the TitlesFragment
 * in MainActivity. */
public class ContentFragment extends Fragment {
    private View mContentView;
    private int mCategory = 0;
    private int mCurPosition = 0;
    private boolean mSystemUiVisible = true;
    private boolean mSoloFragment = false;
    
    
    private ImageButton mButtonCalendar = null;
    private ImageButton mButtonLocation = null;
    private ImageButton mButtonFavorite = null;
    private ImageButton mButtonShare = null;
    
    // The bitmap currently used by ImageView
    private Bitmap mBitmap = null;

    // Current action mode (contextual action bar, a.k.a. CAB)
    private ActionMode mCurrentActionMode;

    /** This is where we initialize the fragment's UI and attach some
     * event listeners to UI components.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.content_welcome, null);
        final ImageView imageView = (ImageView) mContentView.findViewById(R.id.image);
        mContentView.setDrawingCacheEnabled(false);
        
        
        mButtonCalendar = (ImageButton) mContentView.findViewById(R.id.button_add_calendar);
		// add a click listener to the button
        mButtonCalendar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getActivity(), "Adding event to calendar...", Toast.LENGTH_SHORT).show();	
			}
		});
        
        mButtonLocation = (ImageButton) mContentView.findViewById(R.id.button_location);
		// add a click listener to the button
        mButtonLocation.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getActivity(), "Opening event location in map...", Toast.LENGTH_SHORT).show();	
			}
		});
        
        mButtonFavorite = (ImageButton) mContentView.findViewById(R.id.button_favorite);
		// add a click listener to the button
        mButtonFavorite.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getActivity(), "Adding event to favorites...", Toast.LENGTH_SHORT).show();	
			}
		});
        
        mButtonShare = (ImageButton) mContentView.findViewById(R.id.button_share);
		// add a click listener to the button
        mButtonShare.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getActivity(), "Sharing event...", Toast.LENGTH_SHORT).show();	
			}
		});

        // Handle drag events when a list item is dragged into the view
        mContentView.setOnDragListener(new View.OnDragListener() {
            public boolean onDrag(View view, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_ENTERED:
                        view.setBackgroundColor(
                                getResources().getColor(R.color.drag_active_color));
                        break;

                    case DragEvent.ACTION_DRAG_EXITED:
                        view.setBackgroundColor(Color.TRANSPARENT);
                        break;

                    case DragEvent.ACTION_DRAG_STARTED:
                        return processDragStarted(event);

                    case DragEvent.ACTION_DROP:
                        view.setBackgroundColor(Color.TRANSPARENT);
                        return processDrop(event, imageView);
                }
                return false;
            }
        });

        // Show/hide the system status bar when single-clicking a photo.
        mContentView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (mCurrentActionMode != null) {
                  // If we're in an action mode, don't toggle the action bar
                  return;
                }

                if (mSystemUiVisible) {
                  setSystemUiVisible(false);
                } else {
                  setSystemUiVisible(true);
                }
            }
        });

        // When long-pressing a photo, activate the action mode for selection, showing the
        // contextual action bar (CAB).
        mContentView.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                if (mCurrentActionMode != null) {
                    return false;
                }

                mCurrentActionMode = getActivity().startActionMode(
                        mContentSelectionActionModeCallback);
                view.setSelected(true);
                return true;
            }
        });

        return mContentView;
    }

    /** This is where we perform additional setup for the fragment that's either
     * not related to the fragment's layout or must be done after the layout is drawn.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set member variable for whether this fragment is the only one in the activity
        Fragment listFragment = getFragmentManager().findFragmentById(R.id.titles_frag);
        mSoloFragment = listFragment == null ? true : false;

        if (mSoloFragment) {
            // The fragment is alone, so enable up navigation
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            // Must call in order to get callback to onOptionsItemSelected()
            setHasOptionsMenu(true);
        }

        // Current position and UI visibility should survive screen rotations.
        if (savedInstanceState != null) {
            setSystemUiVisible(savedInstanceState.getBoolean("systemUiVisible"));
            if (mSoloFragment) {
                // Restoring these members is not necessary when this fragment
                // is combined with the TitlesFragment, because when the TitlesFragment
                // is restored, it selects the appropriate item and sends the event
                // to the updateContentAndRecycleBitmap() method itself
                mCategory = savedInstanceState.getInt("category");
                mCurPosition = savedInstanceState.getInt("listPosition");
                updateContentAndRecycleBitmap(mCategory, mCurPosition);
            }
        }

        if (mSoloFragment) {
          String title = Directory.getCategory(mCategory).getEntry(mCurPosition).getName();
          ActionBar bar = getActivity().getActionBar();
          bar.setTitle(title);
        }
        
        // Attach a GlobalLayoutListener so that we get a callback when the layout
        // has finished drawing. This is necessary so that we can apply top-margin
        // to the ListView in order to dodge the ActionBar. Ordinarily, that's not
        // necessary, but we've set the ActionBar to "overlay" mode using our theme,
        // so the layout does not account for the action bar position on its own.
        ViewTreeObserver observer = getView().getViewTreeObserver();
        observer.addOnGlobalLayoutListener(layoutListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // This callback is used only when mSoloFragment == true (see onActivityCreated above)
        switch (item.getItemId()) {
        case android.R.id.home:
            // App icon in Action Bar clicked; go up
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Reuse the existing instance
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("listPosition", mCurPosition);
        outState.putInt("category", mCategory);
        outState.putBoolean("systemUiVisible", mSystemUiVisible);
    }
    
    @Override
    public void onDestroyView() {
      super.onDestroyView();
      // Always detach ViewTreeObserver listeners when the view tears down
      getView().getViewTreeObserver().removeGlobalOnLayoutListener(layoutListener);
    }
    
    // Because the fragment doesn't have a reliable callback to notify us when
    // the activity's layout is completely drawn, this OnGlobalLayoutListener provides
    // the necessary callback so we can add top-margin to the ListView in order to dodge
    // the ActionBar. Which is necessary because the ActionBar is in overlay mode, meaning
    // that it will ordinarily sit on top of the activity layout as a top layer and
    // the ActionBar height can vary. Specifically, when on a small/normal size screen,
    // the action bar tabs appear in a second row, making the action bar twice as tall.
    ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        public void onGlobalLayout() {
            int barHeight = getActivity().getActionBar().getHeight();
            View view = getView();
            FrameLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();
            // The list view top-margin should always match the action bar height
            if (params.topMargin != barHeight) {
                params.topMargin = barHeight;
                view.setLayoutParams(params);
            }
            // The action bar doesn't update its height when hidden, so make top-margin zero
            if (!getActivity().getActionBar().isShowing()) {
              params.topMargin = 0;
              view.setLayoutParams(params);
            }
        }
    };

    /** Toggle whether the system UI (status bar / system bar) is visible.
     *  This also toggles the action bar visibility.
     * @param show True to show the system UI, false to hide it.
     */
    void setSystemUiVisible(boolean show) {
        mSystemUiVisible = show;

        Window window = getActivity().getWindow();
        WindowManager.LayoutParams winParams = window.getAttributes();
        View view = getView();
        ActionBar actionBar = getActivity().getActionBar();

        if (show) {
            // Show status bar (remove fullscreen flag)
            window.setFlags(0, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            // Show system bar
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            // Show action bar
            actionBar.show();
        } else {
            // Add fullscreen flag (hide status bar)
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
            // Hide system bar
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            // Hide action bar
            actionBar.hide();
        }
        window.setAttributes(winParams);
    }

    boolean processDragStarted(DragEvent event) {
        // Determine whether to continue processing drag and drop based on the
        // plain text MIME type.
        ClipDescription clipDesc = event.getClipDescription();
        if (clipDesc != null) {
            return clipDesc.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
        }
        return false;
    }

    boolean processDrop(DragEvent event, ImageView imageView) {
        // Attempt to parse clip data with expected format: category||entry_id.
        // Ignore event if data does not conform to this format.
        ClipData data = event.getClipData();
        if (data != null) {
            if (data.getItemCount() > 0) {
                Item item = data.getItemAt(0);
                String textData = (String) item.getText();
                if (textData != null) {
                    StringTokenizer tokenizer = new StringTokenizer(textData, "||");
                    if (tokenizer.countTokens() != 2) {
                        return false;
                    }
                    int category = -1;
                    int entryId = -1;
                    try {
                        category = Integer.parseInt(tokenizer.nextToken());
                        entryId = Integer.parseInt(tokenizer.nextToken());
                    } catch (NumberFormatException exception) {
                        return false;
                    }
                    updateContentAndRecycleBitmap(category, entryId);
                    // Update list fragment with selected entry.
                    TitlesFragment titlesFrag = (TitlesFragment)
                            getFragmentManager().findFragmentById(R.id.titles_frag);
                    titlesFrag.selectPosition(entryId);
                    return true;
                }
            }
        }
        return false;
    }
    
    
    /**
     * Sets the current image visible.
     * @param category Index position of the image category
     * @param position Index position of the image
     */
    public void updateContentAndRecycleBitmap(int category, int position) {
        mCategory = category;
        mCurPosition = position;

        if (mCurrentActionMode != null) {
            mCurrentActionMode.finish();
        }

        if (mBitmap != null) {
            // This is an advanced call and should be used if you
            // are working with a lot of bitmaps. The bitmap is dead
            // after this call.
            mBitmap.recycle();
        }

        // Get the bitmap that needs to be drawn and update the ImageView
        mBitmap = Directory.getCategory(category).getEntry(position)
                .getBitmap(getResources());
        ((ImageView) getView().findViewById(R.id.image)).setImageBitmap(mBitmap);
    }

    /** Share the currently selected photo using an AsyncTask to compress the image
     * and then invoke the appropriate share intent.
     */
    void shareCurrentPhoto() {
        File externalCacheDir = getActivity().getExternalCacheDir();
        if (externalCacheDir == null) {
            Toast.makeText(getActivity(), "Error writing to USB/external storage.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Prevent media scanning of the cache directory.
        final File noMediaFile = new File(externalCacheDir, ".nomedia");
        try {
            noMediaFile.createNewFile();
        } catch (IOException e) {
        }

        // Write the bitmap to temporary storage in the external storage directory (e.g. SD card).
        // We perform the actual disk write operations on a separate thread using the
        // {@link AsyncTask} class, thus avoiding the possibility of stalling the main (UI) thread.

        final File tempFile = new File(externalCacheDir, "tempfile.jpg");

        new AsyncTask<Void, Void, Boolean>() {
            /**
             * Compress and write the bitmap to disk on a separate thread.
             * @return TRUE if the write was successful, FALSE otherwise.
             */
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    FileOutputStream fo = new FileOutputStream(tempFile, false);
                    if (!mBitmap.compress(Bitmap.CompressFormat.JPEG, 60, fo)) {
                        Toast.makeText(getActivity(), "Error writing bitmap data.",
                                Toast.LENGTH_SHORT).show();
                        return Boolean.FALSE;
                    }
                    return Boolean.TRUE;

                } catch (FileNotFoundException e) {
                    Toast.makeText(getActivity(), "Error writing to USB/external storage.",
                            Toast.LENGTH_SHORT).show();
                    return Boolean.FALSE;
                }
            }

            /**
             * After doInBackground completes (either successfully or in failure), we invoke an
             * intent to share the photo. This code is run on the main (UI) thread.
             */
            @Override
            protected void onPostExecute(Boolean result) {
                if (result != Boolean.TRUE) {
                    return;
                }

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tempFile));
                shareIntent.setType("image/jpeg");
                startActivity(Intent.createChooser(shareIntent, "Share photo"));
            }
        }.execute();
    }

    /**
     * The callback for the 'photo selected' {@link ActionMode}. In this action mode, we can
     * provide contextual actions for the selected photo. We currently only provide the 'share'
     * action, but we could also add clipboard functions such as cut/copy/paste here as well.
     */
    private ActionMode.Callback mContentSelectionActionModeCallback = new ActionMode.Callback() {
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.setTitle(R.string.photo_selection_cab_title);

            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.photo_context_menu, menu);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_share:
                    shareCurrentPhoto();
                    actionMode.finish();
                    return true;
            }
            return false;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            mContentView.setSelected(false);
            mCurrentActionMode = null;
        }
    };
}
