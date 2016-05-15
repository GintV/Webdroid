package citrusfresh.webdroid;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnChoiceListener} interface
 * to handle interaction events.
 * Use the {@link ConnectionChoiceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConnectionChoiceFragment extends Fragment implements View.OnClickListener {

    private OnChoiceListener mListener;

    public ConnectionChoiceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ConnectionChoiceFragment.
     */
    public static ConnectionChoiceFragment newInstance() {
        ConnectionChoiceFragment fragment = new ConnectionChoiceFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_connection_choice, container, false);
        Button button = (Button) layout.findViewById(R.id.button1);
        button.setOnClickListener(this);
        button = (Button) layout.findViewById(R.id.button2);
        button.setOnClickListener(this);
        return layout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChoiceListener) {
            mListener = (OnChoiceListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChoiceListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            switch(v.getId()) {
                case R.id.button1:
                    mListener.onChoiceMade(0);
                    break;
                case R.id.button2:
                    mListener.onChoiceMade(1);
                    break;
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnChoiceListener {
        void onChoiceMade(int choice);
    }
}
