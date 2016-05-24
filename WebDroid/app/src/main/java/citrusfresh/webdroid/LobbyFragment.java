package citrusfresh.webdroid;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class LobbyFragment extends Fragment {

    private OnLobbyInflatedListener mListener;

    public LobbyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_lobby, container, false);
        mListener.onLobbyInflated();
        return layout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLobbyInflatedListener) {
            mListener = (OnLobbyInflatedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLobbyInflatedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnLobbyInflatedListener {
        void onLobbyInflated();
    }
}
