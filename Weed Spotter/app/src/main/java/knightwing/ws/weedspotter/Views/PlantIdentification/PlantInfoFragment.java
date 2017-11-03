package knightwing.ws.weedspotter.Views.PlantIdentification;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.io.IOException;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import knightwing.ws.weedspotter.Models.PlantIdentification.CurrentSubmission;
import knightwing.ws.weedspotter.Models.PlantIdentification.PlantGrowth;
import knightwing.ws.weedspotter.Models.PlantIdentification.PlantType;
import knightwing.ws.weedspotter.Models.PlantIdentification.Utilities;
import knightwing.ws.weedspotter.R;
import knightwing.ws.weedspotter.Views.Widgets.PageIndicatorView;

/**
 * A fragment for prompting the user to take photos.
 */
public class PlantInfoFragment extends Fragment {
    /**
     * The fragment argument representing the message.
     */
    private static final String ARG_PAGE_NUMBER = "page_number";
    private static final String ARG_MAX_PAGES = "max_pages";

    /**
     * GUI elements present in this fragment.
     */
    CurrentSubmission submission = CurrentSubmission.getInstance();
    EditText nameBox;
    EditText closestLocationBox;
    Spinner plantTypeChoices;
    Spinner plantGrowthChoices;
    SharedPreferences nameStorage;

    public PlantInfoFragment() {
    }

    /**
     * Create an email prompt fragment for filling out a form (excluding notes).
     * @param page where the email prompt is to be displayed
     * @param maxPages the numbe of pages that make up the submission process
     * @require page > 0 && page <= maxPages
     * @return the email prompt to be displayed in the app
     */
    public static PlantInfoFragment newInstance(int page, int maxPages) {
        PlantInfoFragment fragment = new PlantInfoFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE_NUMBER, page);
        args.putInt(ARG_MAX_PAGES, maxPages);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_info, container, false);

        // Get today's date for dating the submission
        DateFormat format = DateFormat.getDateInstance(DateFormat.DATE_FIELD
                | DateFormat.MONTH_FIELD
                | DateFormat.YEAR_FIELD);
        String date = format.format((new java.util.Date()));
        submission.setSendDate(date);

        // Setup text boxes
        nameBox = setupNameBox(rootView);
        nameStorage = Utilities.getNameStorage(rootView.getContext());
        Utilities.refillName(nameStorage, nameBox, rootView.getContext());
        closestLocationBox = setupClosestLocationBox(rootView);

        // Setup plant spinners and set initial choices
        plantTypeChoices = setupSpinner(R.id.plantTypeSpinner, R.array.plant_types, rootView);
        setSpinnerChoice(plantTypeChoices);
        plantGrowthChoices = setupSpinner(R.id.plantGrowthSpinner, R.array.plant_growths, rootView);
        setSpinnerChoice(plantGrowthChoices);

        // autopopulate the nearest location
        CurrentSubmission submission = CurrentSubmission.getInstance();
        if (submission.locationHasBeenSet()) {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            try {
                List<Address> addressList = geocoder.getFromLocation(submission.getLatitude(), submission.getLongitude(), 1);
                closestLocationBox.setText(addressList.get(0).getLocality());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // set up page indicator
        PageIndicatorView indicator = (PageIndicatorView) rootView.findViewById(R.id.page_indicator);
        indicator.setTotalSteps(getArguments().getInt(ARG_MAX_PAGES));
        indicator.setCurrentStep(getArguments().getInt(ARG_PAGE_NUMBER));

        ImageButton backbtn = (ImageButton) rootView.findViewById(R.id.backbtn);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ReportActivity)getActivity()).previousFragment();
            }
        });

        ImageButton forwardbtn = (ImageButton) rootView.findViewById(R.id.forwardbtn);
        forwardbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ReportActivity)getActivity()).nextFragment();
            }
        });

        return rootView;
    }

    /**
     * Sets up the name textbox to automatically save the name as it is being typed in.
     * @param view - where the name box is located
     * @require view != null
     * @return a fully functional name textbox.
     */
    public EditText setupNameBox(View view) {
        final EditText nameBox = (EditText)(view.findViewById(R.id.nameField));
        nameBox.addTextChangedListener(new TextWatcher() {
            // Not needed, but definition needs to be present
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                submission.setSenderName(nameBox.getText().toString().trim());
            }

            // Not needed, but definition needs to be present
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        return nameBox;
    }

    /**
     * Sets up the closest location textbox to automatically save the closest location as it is
     * being typed in.
     * @param view - where the closest location box is located
     * @require view != null
     * @return a fully functional closest location textbox.
     */
    public EditText setupClosestLocationBox(View view) {
        final EditText closestLocationBox = (EditText)(view.findViewById(R.id.closestLocationField));
        closestLocationBox.addTextChangedListener(new TextWatcher() {
            // Not needed, but definition needs to be present
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                submission.setClosestTownSuburb(closestLocationBox.getText().toString().trim());
            }

            // Not needed, but definition needs to be present
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        return closestLocationBox;
    }

    /**
     * Sets up either a plant types or plant growths spinner so that it internally sets the relevant
     * choice.
     * @param type - ID corresponding to the spinner itself
     * @param choicesList - ID corresponding to the choices it has
     * @param view - where the spinner is located
     * @require type and choicesList both refer to the spinners for plant types or plant growths &&
     *          view != null
     * @return a fully functional plant types or plant growths spinner.
     */
    public Spinner setupSpinner(int type, int choicesList, View view) {
        final Spinner choices = (Spinner)(view.findViewById(type));
        setSpinnerItems(choices, choicesList, view);
        if (type == R.id.plantTypeSpinner) {
            choices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    setSpinnerChoice(choices);
                }

                // Not needed, but definition needs to be present
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
        } else {
            choices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    setSpinnerChoice(choices);
                }

                // Not needed, but definition needs to be present
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });

        }
        return choices;
    }

    /**
     * Helper method for setting the spinner choice internally.
     * @param choices the spinner being focused on
     * @require choices != null && choices contains plant types or plant growths
     */
    private void setSpinnerChoice(Spinner choices) {
        int type = choices.getId();
        String plantAttribute = choices.getSelectedItem().toString().
                toUpperCase().replace(' ', '_');
        if (type == R.id.plantTypeSpinner) {
            submission.setPlantType(PlantType.valueOf(plantAttribute));
        } else {
            submission.setPlantGrowth(PlantGrowth.valueOf(plantAttribute));
        }
    }

    /**
     * Fills up a spinner with the given choices.
     * @param choices - the spinner to fill.
     * @param choicesList - the choices, from R.id
     * @param view - the view that this spinner will be displayed on.
     * @require choices != null && choicesList != null && view != null
     */
    private void setSpinnerItems(Spinner choices, int choicesList, View view) {
        ArrayAdapter<CharSequence> choicesAdaptor = ArrayAdapter.createFromResource(
                view.getContext(), choicesList, android.R.layout.simple_spinner_item);
        choicesAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        choices.setAdapter(choicesAdaptor);
    }
}