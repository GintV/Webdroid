package citrusfresh.webdroid;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnPlayerInfoChangeListener} interface
 * to handle interaction events.
 * Use the {@link SetUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetUpFragment extends Fragment implements View.OnClickListener {

    private OnPlayerInfoChangeListener mListener;
    private EditText name;
    private EditText initials;
    private ToggleButton ready;
    private Button update;

    public SetUpFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SetUpFragment.
     */
    public static SetUpFragment newInstance() {
        return new SetUpFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_set_up, container, false);
        ready = (ToggleButton) layout.findViewById(R.id.buttonReady);
        ready.setOnClickListener(this);
        update = (Button) layout.findViewById(R.id.buttonUpdate);
        update.setOnClickListener(this);
        name = (EditText) layout.findViewById(R.id.editTextName);
        initials = (EditText) layout.findViewById(R.id.editTextInitials);
        return layout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPlayerInfoChangeListener) {
            mListener = (OnPlayerInfoChangeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPlayerInfoChangeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        if(mListener != null) {
            switch(v.getId()) {
                case R.id.buttonReady:
                    if (ready.isChecked()) {
                        name.setEnabled(false);
                        initials.setEnabled(false);
                        update.setText(getString(R.string.calibrate));
                    }
                    else {
                        name.setEnabled(true);
                        initials.setEnabled(true);
                        update.setText(getString(R.string.update_info));
                    }
                    break;
                case R.id.buttonUpdate:
                    if (ready.isChecked()) {
                        mListener.onPlayerInfoChange(name.getText().toString(), initials.getText().toString(), "FFFFFF", ready.isChecked(), true);
                        return;
                    }
                    break;
            }
            mListener.onPlayerInfoChange(name.getText().toString(), initials.getText().toString(), "FFFFFF", ready.isChecked(), false);
        }
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
    public interface OnPlayerInfoChangeListener {
        // TODO: Update argument type and name
        void onPlayerInfoChange(String name, String initials, String color, boolean isReady, boolean isCalibrating);
    }
}
