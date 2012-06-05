/**
 * 
 */
package org.android.calevent.frontend.fragments;

import org.android.calevent.frontend.MainActivity;
import org.android.calevent.frontend.R;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * @author Bjoern
 *
 */
public class SettingsFragment extends Fragment {
    private int mCategory = 0;
    private int mCurPosition = 0;
    private boolean mSystemUiVisible = true;
    private boolean mSoloFragment = false;


    /** This is where we initialize the fragment's UI and attach some
     * event listeners to UI components.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	View mSettingsView = inflater.inflate(R.layout.settings_welcome, container, false);       
        
        Spinner spinnerTheme = (Spinner) mSettingsView.findViewById(R.id.spinner_theme);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        		mSettingsView.getContext(), R.array.theme_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(adapter);
        spinnerTheme.setOnItemSelectedListener(new ThemeOnItemSelectedListener());
        
        Spinner spinnerLanguage = (Spinner) mSettingsView.findViewById(R.id.spinner_language);
        adapter = ArrayAdapter.createFromResource(
        		mSettingsView.getContext(), R.array.language_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);
        spinnerLanguage.setOnItemSelectedListener(new LanguageOnItemSelectedListener());

        return mSettingsView;
    }

    /** This is where we perform additional setup for the fragment that's either
     * not related to the fragment's layout or must be done after the layout is drawn.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set member variable for whether this fragment is the only one in the activity
        Fragment settingsFragment = getFragmentManager().findFragmentById(R.id.titles_frag);
        mSoloFragment = settingsFragment == null ? true : false;
        ActionBar bar = getActivity().getActionBar();   

        if (mSoloFragment) {
            // The fragment is alone, so enable up navigation
        	bar.setDisplayHomeAsUpEnabled(true);
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
            }
        }

        /*if (mSoloFragment) {
          String title = Directory.getCategory(mCategory).getEntry(mCurPosition).getName();
          bar.setTitle(title);
        }*/
        
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

    public class ThemeOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
            Toast.makeText(parent.getContext(), "The theme is " +
            parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();
            Toast.makeText(parent.getContext(), "Toggle theme...", Toast.LENGTH_SHORT).show();
        }

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }
    
    public class LanguageOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
            Toast.makeText(parent.getContext(), "The language is " +
              parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();
            Toast.makeText(parent.getContext(), "Toggle language...", Toast.LENGTH_SHORT).show();
        }

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }
}
