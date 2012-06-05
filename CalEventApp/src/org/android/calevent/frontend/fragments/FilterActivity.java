/**
 * 
 */
package org.android.calevent.frontend.fragments;

import java.util.Calendar;

import org.android.calevent.frontend.R;
import org.android.calevent.frontend.MainActivity.MyDialogFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

/**
 * @author Bjoern
 * 
 */
public class FilterActivity extends Activity {
	private int mThemeId = 0;

	private TextView mDateDisplay;
	private Button mPickDate;
	private int mYear;
	private int mMonth;
	private int mDay;

	static final int DATE_DIALOG_ID = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			// The activity theme is the only state data that the activity needs
			// to restore. All info about the content displayed is managed by
			// the fragment
			mThemeId = extras.getInt("theme");
		} else if (savedInstanceState != null) {
			// If there's no restore state, get the theme from the intent
			mThemeId = savedInstanceState.getInt("theme");
		}

		if (mThemeId != 0) {
			setTheme(mThemeId);
		}

		setContentView(R.layout.filter_activity);

		if (extras != null) {
			// Take the info from the intent and deliver it to the fragment so
			// it can update
			int category = extras.getInt("category");
			int position = extras.getInt("position");
			FilterFragment frag = (FilterFragment) getFragmentManager()
					.findFragmentById(R.id.filter_frag);
		}
		
		// capture our View elements
		mDateDisplay = (TextView) findViewById(R.id.text_view_date_display);
		mPickDate = (Button) findViewById(R.id.button_pick_date);

		// add a click listener to the button
		mPickDate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);	
			}
		});

		// get the current date
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);

		// display the current date (this method is below)
		updateDisplay();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("theme", mThemeId);
	}
	
	// updates the date in the TextView
		private void updateDisplay() {
			mDateDisplay.setText(new StringBuilder()
					// Month is 0 based so add 1
					.append(mDay).append(".").append(mMonth + 1).append(".")
					.append(mYear).append(" "));
		}

		// the callback received when the user "sets" the date in the dialog
		private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				mYear = year;
				mMonth = monthOfYear;
				mDay = dayOfMonth;
				updateDisplay();
			}
		};

		protected Dialog onCreateDialog(int id) {
			switch (id) {
			case DATE_DIALOG_ID:
				return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
						mDay);
			}
			return null;
		}

}
