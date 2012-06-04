/**
 * 
 */
package org.android.calevent.frontend.dialogs;

import org.android.calevent.frontend.R;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

/**
 * @author Bjoern
 *
 */
public class FilterDialogFragment extends DialogFragment {
	
	private static FilterDialogFragment frag;
	
	public static FilterDialogFragment newInstance(String title) {
		frag = new FilterDialogFragment();
        Bundle args = new Bundle();
        args.putString("text", title);
        frag.setArguments(args);
        return frag;
    }
	

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	String text = getArguments().getString("text");
    	if (frag != null) {
    		frag.getDialog().setTitle(text);
    	}
        View v = inflater.inflate(R.layout.filter_dialog, container, false);       
        
        Spinner spinner = (Spinner) v.findViewById(R.id.spinner_location);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                v.getContext(), R.array.locations_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        Button buttonOk = (Button) v.findViewById(R.id.button_ok);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });
        Button buttonCancel = (Button) v.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });

        return v;
    }
}
