/**
 * 
 */
package org.android.calevent.frontend.fragments;

import org.android.calevent.frontend.R;

import android.app.Activity;
import android.os.Bundle;

/**
 * @author Bjoern
 *
 */
public class SettingsActivity extends Activity {
	 private int mThemeId = 0;

	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);

	      Bundle extras = getIntent().getExtras();
	      if (extras != null) {
	          // The activity theme is the only state data that the activity needs
	          // to restore. All info about the content displayed is managed by the fragment
	          mThemeId = extras.getInt("theme");
	      } else if (savedInstanceState != null) {
	          // If there's no restore state, get the theme from the intent
	          mThemeId = savedInstanceState.getInt("theme");
	      }

	      if (mThemeId != 0) {
	          setTheme(mThemeId);
	      }

	      setContentView(R.layout.settings_activity);

	      if (extras != null) {
	        // Take the info from the intent and deliver it to the fragment so it can update
	        int category = extras.getInt("category");
	        int position = extras.getInt("position");
	        SettingsFragment frag = (SettingsFragment) getFragmentManager().findFragmentById(R.id.settings_frag);
	      }
	  }

	  @Override
	  protected void onSaveInstanceState(Bundle outState) {
	      super.onSaveInstanceState(outState);
	      outState.putInt("theme", mThemeId);
	  }
}
