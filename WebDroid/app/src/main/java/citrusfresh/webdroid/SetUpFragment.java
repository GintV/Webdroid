package citrusfresh.webdroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ToggleButton;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnPlayerInfoChangeListener} interface
 * to handle interaction events.
 * Use the {@link SetUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetUpFragment extends Fragment implements View.OnClickListener {

    private ArrayList<String> availableColors;
    private String selectedColor;
    private String previousColor;
    private OnPlayerInfoChangeListener mListener;
    private EditText name;
    private EditText initials;
    private ToggleButton ready;
    private Button update;
    private ImageView color;

    private int readyCnt;

    public SetUpFragment() {
        // Required empty public constructor
        readyCnt = 0;
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
        color = (ImageView) layout.findViewById(R.id.imageViewColor);
        color.setOnClickListener(this);
        color.getDrawable().mutate().setColorFilter(Color.parseColor(selectedColor), PorterDuff.Mode.SRC_IN);
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
            availableColors = mListener.getAvailableColors();
            selectedColor = availableColors.get(0);
            previousColor = selectedColor;
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
                    if (readyCnt == 0) {
                        name.setEnabled(false);
                        initials.setEnabled(false);
                        update.setText(getString(R.string.calibrate));
                        color.setEnabled(false);
                        readyCnt++;
                    }
                    else {
                        name.setEnabled(true);
                        initials.setEnabled(true);
                        update.setText(getString(R.string.update_info));
                        color.setEnabled(true);
                        readyCnt--;
                    }
                    break;
                case R.id.buttonUpdate:
                    if (readyCnt == 1) {
                        mListener.onPlayerInfoChange(name.getText().toString(), initials.getText().toString(), selectedColor, true, true);
                        return;
                    }
                    break;
                case R.id.imageViewColor: {
                    previousColor = selectedColor;
                    openColorSelect();
                    return;
                }
            }
            boolean isReady = (readyCnt == 1);
            mListener.onPlayerInfoChange(name.getText().toString(), initials.getText().toString(), selectedColor, isReady, false);
        }
    }

    private void openColorSelect() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity())
                .setTitle(Html.fromHtml("<font color='#deddd6'>Choose a color</font>"))
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (availableColors.contains(selectedColor)) {
                            color.getDrawable().mutate().setColorFilter(Color.parseColor(selectedColor), PorterDuff.Mode.SRC_IN);
                            mListener.onColorChange(selectedColor);
                        }
                        else {
                            selectedColor = previousColor;
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        selectedColor = previousColor;
                    }
                });
        ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.color_select_item, availableColors) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;
                if (convertView == null) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    view = inflater.inflate(R.layout.color_select_item, parent, false);
                } else {
                    view = convertView;
                }
                ImageView imageView = (ImageView) view.findViewById(R.id.image);
                String color = getItem(position);
                Drawable toAdd = ResourcesCompat.getDrawable(getResources(), R.drawable.color_circle, null);
                if (toAdd != null) {
                    toAdd = toAdd.mutate();
                    toAdd.setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN);
                    imageView.setImageDrawable(toAdd);
                }
                imageView.setTag(position);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedColor = availableColors.get((Integer)v.getTag());
                        notifyDataSetChanged();
                    }
                });
                RadioButton r = (RadioButton) view.findViewById(R.id.radioButton);
                r.setChecked(getItem(position).equals(selectedColor));
                r.setTag(position);
                r.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedColor = availableColors.get((Integer)view.getTag());
                        notifyDataSetChanged();
                    }
                });
                return view;
            }
        };

        dialogBuilder.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        /*
        dialogBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        */
        dialogBuilder.show();
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
        ArrayList<String> getAvailableColors();
        void onColorChange(String color);
    }
}
