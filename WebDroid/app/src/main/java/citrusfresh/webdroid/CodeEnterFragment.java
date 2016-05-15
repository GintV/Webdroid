package citrusfresh.webdroid;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnCodeEnteredListener} interface
 * to handle interaction events.
 * Use the {@link CodeEnterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CodeEnterFragment extends Fragment implements View.OnClickListener {

    private OnCodeEnteredListener mListener;

    public CodeEnterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CodeEnterFragment.
     */
    public static CodeEnterFragment newInstance() {
        return new CodeEnterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_code_enter, container, false);
        Button button = (Button) layout.findViewById(R.id.button3);
        button.setOnClickListener(this);
        return layout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCodeEnteredListener) {
            mListener = (OnCodeEnteredListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCodeEnteredListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        if (getView() != null) {
            EditText textEditor = (EditText) getView().findViewById(R.id.editText);
            if (mListener != null) {
                mListener.onCodeEntered(textEditor.getText().toString());
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
    public interface OnCodeEnteredListener {
        void onCodeEntered(String code);
    }
}
