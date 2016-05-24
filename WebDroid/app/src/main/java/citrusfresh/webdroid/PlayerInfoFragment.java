package citrusfresh.webdroid;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlayerInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerInfoFragment extends Fragment {

    public PlayerInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlayerInfoFragment.
     */
    public static PlayerInfoFragment newInstance(String param1, String param2) {
        return new PlayerInfoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_player_info, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setPlayers(ArrayList<Data> players) {
        final ArrayList<Data> playersList = players;
        ArrayList<String> strings = new ArrayList<>();
        for(Data d : playersList) {
            strings.add("");
        }
        final ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.player_list_item, strings) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;
                if (convertView == null) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    view = inflater.inflate(R.layout.player_list_item, parent, false);
                } else {
                    view = convertView;
                }
                ImageView readyView = (ImageView) view.findViewById(R.id.imageViewReady);
                ImageView colorView = (ImageView) view.findViewById(R.id.imageViewListColor);
                TextView name = (TextView) view.findViewById(R.id.textViewPlayerName);
                TextView initials = (TextView) view.findViewById(R.id.textViewPlayerIni);
                Data playerData = playersList.get(position);
                if (playerData.getPlayerInfoChange().isPlayerIsReady()) {
                    readyView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ready_circle, null));
                }
                else {
                    readyView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.not_ready_circle, null));
                }
                Drawable colorCircle = ResourcesCompat.getDrawable(getResources(), R.drawable.color_circle, null);
                if (colorCircle != null) {
                    colorCircle = colorCircle.mutate();
                    colorCircle.setColorFilter(Color.parseColor(playerData.getPlayerInfoChange().getPlayerColor()), PorterDuff.Mode.SRC_IN);
                    colorView.setImageDrawable(colorCircle);
                }
                name.setText(playerData.getPlayerInfoChange().getPlayerName());
                initials.setText(playerData.getPlayerInfoChange().getPlayerInitials());
                return view;
            }
        };
        View v = getView();
        if (v != null) {
            final ListView listView = (ListView) v.findViewById(R.id.listView);;
            if (listView != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(adapter);
                    }
                });
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
}
