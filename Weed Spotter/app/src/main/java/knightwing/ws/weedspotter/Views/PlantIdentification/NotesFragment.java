package knightwing.ws.weedspotter.Views.PlantIdentification;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import java.util.HashMap;
import java.util.Map;

import knightwing.ws.weedspotter.Models.PlantIdentification.CurrentSubmission;
import knightwing.ws.weedspotter.Models.PlantIdentification.Utilities;
import knightwing.ws.weedspotter.R;
import knightwing.ws.weedspotter.Views.Activities.SubmissionStages.SubmissionCompleteActivity;
import knightwing.ws.weedspotter.Views.Activities.SubmissionStages.SubmissionStoredActivity;
import knightwing.ws.weedspotter.Views.Widgets.PageIndicatorView;

/**
 * A fragment for allowing users to enter extra notes about a potential weed
 * they have spotted.
 */
public class NotesFragment extends Fragment {

    private final int CHOOSER_RESULT = 0;
    private final int EMAILING_RESULT = 1;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PAGE_NUMBER = "page_number";
    private static final String ARG_MAX_PAGES = "max_pages";
    private static Map<Integer, String> errorHeaders = new HashMap<>();
    private static Map<Integer, String> errorText = new HashMap<>();

    private EditText notes;

    private ImageButton submitButton;
    private ImageButton backbtn;
    SharedPreferences nameStorage;
    SharedPreferences locationSkipStateStorage;
    ProgressBar emailConstructionBar;
    Intent emailIntent;
    View rootView;

    CurrentSubmission submission = CurrentSubmission.getInstance();

    public NotesFragment() {
        fillErrorInformation();
    }

    private static void fillErrorInformation() {
        errorHeaders.put(0, "No Photo of Whole Plant");
        errorHeaders.put(1, "No Photo of Leaves");
        errorText.put(0, "You haven't taken a photo of the entire plant.");
        errorText.put(1, "You haven't taken a photo of the plant's leaves.");
    }

    /**
     * Create a new fragment that will allow the user to specify extra notes about the submission.
     * @param page - where the notes fragment will be displayed
     * @param maxPages - the number of pages in the entire submission process
     * @require page > 0 && page <= maxPages
     * @return the new notes screen to be displayed in the app
     */
    public static NotesFragment newInstance(int page, int maxPages) {
        NotesFragment notesFragment = new NotesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE_NUMBER, page);
        args.putInt(ARG_MAX_PAGES, maxPages);
        notesFragment.setArguments(args);
        return notesFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_notes, container, false);

        backbtn = (ImageButton) rootView.findViewById(R.id.backbtn);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ReportActivity)getActivity()).previousFragment();
            }
        });

        // Get references to the notes box, manual latitude and longitude boxes and the button
        notes = setupNotesBox(rootView);
        submitButton = (ImageButton)(rootView.findViewById(R.id.submitbtn));

        // Progress spinner and message for email construction
        emailConstructionBar = (ProgressBar)(rootView.findViewById(R.id.emailConstructionBar));
        emailConstructionBar.setVisibility(View.INVISIBLE);

        // Get reference to where the name is stored
        nameStorage = Utilities.getNameStorage(rootView.getContext());

        // Get reference to whether or not the location obtaining step was skipped
        locationSkipStateStorage = Utilities.getLocationSkipStateStorage(rootView.getContext());

        // Set up the button's functionality - constructing an email
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the name is not given, display an alert
                if (!submission.senderNameGiven()) {
                    Utilities.displayErrorAlert(getActivity(), "Collector Name Not Given",
                            "Please provide your name before submitting.");
                    return;
                }

                // The user's closest town/suburb must be provided
                if (!submission.closestTownSuburbGiven()) {
                    Utilities.displayErrorAlert(getActivity(), "Closest Town or Suburb Not Given",
                            "Please provide the closest town or suburb to you before submitting.");
                    return;
                }

                // The user must provide at least a photo of the whole plant and the leaves
                if (!submission.haveWholePlantPhoto()) {
                    displayPhotoErrorAlert(getActivity(), 0);
                    return;
                }

                if (!submission.havePlantLeafPhoto()) {
                    displayPhotoErrorAlert(getActivity(), 1);
                    return;
                }

                // Save the name so it will be filled in next time
                Utilities.saveName(nameStorage, submission, rootView.getContext());

                // Ask user for confirmation - for immediate submission if they're connected to the
                // Internet, or for storing the submission for later sending
                if (!Utilities.connectedToInternet(getContext())) {
                    displaySubmissionStoringConfirmationAlert(getActivity());
                } else {
                    displayImmediateSubmissionConfirmationAlert(getActivity());
                }
            }
        });

        // set up page indicator
        PageIndicatorView indicator = (PageIndicatorView) rootView.findViewById(R.id.page_indicator);
        indicator.setTotalSteps(getArguments().getInt(ARG_MAX_PAGES));
        indicator.setCurrentStep(getArguments().getInt(ARG_PAGE_NUMBER));

        return rootView;
    }

    /**
     * Creates a notes text box that will have all of its leading and trailing whitespace taken out.
     * @param view - where the notes text box is in the app
     * @require view != null
     * @ensure notes box has no leading or trailing whitespace remaining
     * @return a notes box that will ensure leading and trailing whitespace is removed
     */
    private EditText setupNotesBox(View view) {
        final EditText notes = (EditText)(view.findViewById(R.id.notes));
        notes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                submission.setNotes(notes.getText().toString().trim());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        return notes;
    }

    /**
     * Displays dialog box asking if the user wants to send a submission immediately.
     * @param activity - where the alert is being displayed
     * @require activity != null
     */
    public void displayImmediateSubmissionConfirmationAlert(final Activity activity) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle("Confirm Immediate Submission");
        alert.setMessage("Your plant identification request will be submitted immediately. Proceed?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Save the name so it will be filled in next time
                Utilities.saveName(nameStorage, submission, rootView.getContext());

                // Allow user to select an email program to use
                toggleElementFunctionality(false);
                Intent[] emailIntents = Utilities.createEmailChooser(getString(R.string.queensland_herbarium_email));
                Intent chooser = emailIntents[0];
                emailIntent = emailIntents[1];
                startActivityForResult(chooser, CHOOSER_RESULT);

            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {}});
        alert.setIcon(R.drawable.askicon); // https://icons8.com/icon/set/ask/androidL
        alert.show();
    }

    /**
     * Displays dialog box asking if the user wants to store their submission.
     * @param activity - where the alert is being displayed
     * @require activity != null
     */
    public void displaySubmissionStoringConfirmationAlert(final Activity activity) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle("Confirm Submission Storage");
        alert.setMessage(R.string.store_notification_message);
        alert.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Utilities.saveName(nameStorage, submission, rootView.getContext());
                new StoringArchiveProcess(submission).execute();
                return;
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {}});
        alert.setIcon(R.drawable.askicon); // https://icons8.com/icon/set/ask/androidL
        alert.show();
    }
    /**
     * Displays dialog box informing the user that they haven't provided a photo of the whole plant
     * or its leaves.
     * @param activity - where the alert is being displayed
     * @require activity != null && 0 <= photoType <= 1
     */
    private void displayPhotoErrorAlert(final Activity activity, int photoType) {
        AlertDialog.Builder photoErrorAlert = new AlertDialog.Builder(activity);
        photoErrorAlert.setTitle(errorHeaders.get(photoType));
        photoErrorAlert.setMessage(errorText.get(photoType));
        photoErrorAlert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        photoErrorAlert.setIcon(R.drawable.alerticon);
        photoErrorAlert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSER_RESULT) {
            if (data == null) {
                // If cancelled, do nothing!
                toggleElementFunctionality(true);
                return;
            }
            // Otherwise start adding photos - display submission complete screen then
            // the email client.
            new ArchivingProcess(submission, data).execute();
        } else if (requestCode == EMAILING_RESULT) {
            // Display completion screen
            startActivity(new Intent(getContext(), SubmissionCompleteActivity.class));
        }
    }

    /**
     * Task for storing the archive along with a pending submission.
     */
    public class StoringArchiveProcess extends AsyncTask<Void, Void, String> {
        private CurrentSubmission submission;

        public StoringArchiveProcess(CurrentSubmission submission) {
            this.submission = submission;
        }
        /**
         * To begin, show message that tells the user the email is being generated, and
         * turn on the spinner (bar).
         */
        @Override
        protected void onPreExecute() {
            prepareForArchiving();
        }

        /**
         * When finished, remove the message and turn off the spinner.
         * @param result - not used directly.
         */
        @Override
        protected void onPostExecute(String result) {
            finishOffArchivingProcess();
            startActivity(new Intent(getContext(), SubmissionStoredActivity.class));
        }

        @Override
        public String doInBackground(Void... inputs) {
            // Create the photo archive without sending an email
            Uri attachment = Utilities.generateArchive(getContext(), submission);
            if (attachment == null) {
                return null;
            }

            // Store it!
            submission.setAttachmentPath(attachment);
            return "";
        }
    }

    /**
     * Task for creating the archive of photos.
     */
    public class ArchivingProcess extends AsyncTask<Void, Void, String> {
        private CurrentSubmission submission;
        private Intent data;

        public ArchivingProcess(CurrentSubmission submission, Intent data) {
            this.submission = submission;
            this.data = data;
        }

        /**
         * To begin, show message that tells the user the email is being generated, and
         * turn on the spinner (bar).
         */
        @Override
        protected void onPreExecute() {
            prepareForArchiving();
        }

        /**
         * When finished, remove the message and turn off the spinner.
         * @param result - not used directly.
         */
        @Override
        protected void onPostExecute(String result) {
            finishOffArchivingProcess();
        }

        @Override
        public String doInBackground(Void... inputs) {
            // Put email together and start up user's choice of program.
            emailIntent = Utilities.sendEmail(getString(R.string.queensland_herbarium_email),
                    getContext(), submission);
            if (emailIntent == null) {
                return null;
            }

            ComponentName component = data.getComponent();
            emailIntent.setComponent(component);
            startActivityForResult(emailIntent, EMAILING_RESULT);
            return "";
        }
    }

    /**
     * Enables or disables all elements on the notes screen.
     * @param on - flag for enabling (true) or disabling (false) the elements
     */
    private void toggleElementFunctionality(boolean on) {
        notes.setEnabled(on);
        submitButton.setEnabled(on);
        backbtn.setEnabled(on);
    }

    /**
     * Turn on the spinner and disable controls while archiving takes place.
     */
    private void prepareForArchiving() {
        emailConstructionBar.setVisibility(View.VISIBLE);
        emailConstructionBar.setIndeterminate(true);
        toggleElementFunctionality(false);
    }

    /**
     * Turn off spinner and enable controls once archiving is finished.
     */
    private void finishOffArchivingProcess() {
        emailConstructionBar.setVisibility(View.INVISIBLE);
        toggleElementFunctionality(true);
    }
}
