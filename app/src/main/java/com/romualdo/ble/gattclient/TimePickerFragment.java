package com.romualdo.ble.gattclient;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TimePickerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TimePickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimePickerFragment extends DialogFragment
                                implements TimePickerDialog.OnTimeSetListener{

    // Container Activity must implement this interface
    public interface OnDataFromTimePickerFragment {
        public void OnDataFromTimePickerFragment(String data);
    }

    OnDataFromTimePickerFragment onDataPass;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onDataPass = (OnDataFromTimePickerFragment) context;
    }

    public void passData(String data) {
        onDataPass.OnDataFromTimePickerFragment(data);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String s_h = Integer.toString(hourOfDay); // string_hourOfDay
        String s_m = Integer.toString(minute); // string_minute
        String _hour = new String(hourOfDay < 10 ? "0" + s_h : s_h);
        String _minute = new String(minute < 10 ? "0" + s_m : s_m);
        passData(_hour + ":" + _minute);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
