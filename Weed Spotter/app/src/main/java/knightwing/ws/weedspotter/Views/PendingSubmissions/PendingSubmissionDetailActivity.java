package knightwing.ws.weedspotter.Views.PendingSubmissions;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.Toast;

import knightwing.ws.weedspotter.Models.PlantIdentification.CurrentSubmission;
import knightwing.ws.weedspotter.Models.PendingSubmissions.PendingSubmissionCell;
import knightwing.ws.weedspotter.Models.PendingSubmissions.PendingSubmissionStorage;
import knightwing.ws.weedspotter.Models.PlantIdentification.Utilities;
import knightwing.ws.weedspotter.R;
import knightwing.ws.weedspotter.Views.Activities.SubmissionStages.StartupScreenActivity;
import knightwing.ws.weedspotter.Views.Activities.SubmissionStages.SubmissionCompleteActivity;

/**
 * An activity to contain the fragment that will display a selected pending
 * submission's details.
 */
public class PendingSubmissionDetailActivity extends AppCompatActivity {

    private final int CHOOSER_RESULT = 0;
    private final int EMAILING_RESULT = 1;

    PendingSubmissionDetailActivity activity;
    public static final SQLiteDatabase pendingSubmissionsDatabase = StartupScreenActivity.getPssHelper().getWritableDatabase();
    public static CurrentSubmission currentSubmission = CurrentSubmission.getInstance();
    Intent emailIntent;
    ImageButton sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = this;
        setContentView(R.layout.activity_pending_submission_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        sendButton = (ImageButton) findViewById(R.id.sendButton);

        // Don't allow sending of a pending submission without an Internet connection
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Utilities.connectedToInternet(activity)) {
                    Utilities.displayErrorAlert(activity, "No Internet Connection",
                                "You must be connected to the Internet to send submissions.");
                } else {
                    displayConfirmationAlert(activity, "Confirm Sending",
                            "Do you want to send this submission?");
                }

            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(PendingSubmissionDetailFragment.ARG_ITEM_ID,
                    getIntent().getLongExtra(PendingSubmissionDetailFragment.ARG_ITEM_ID, 1));
            PendingSubmissionDetailFragment fragment = new PendingSubmissionDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }
    }

    /**
     * Displays dialog box asking if the user wants to send a submission.
     * @param activity - where the alert is being displayed
     * @param title - title of dialog box
     * @param message - question to check whether they want to send a submission
     * @require activity != null && title != null && title != "" && message != null && message != ""
     */
    public void displayConfirmationAlert(final Activity activity, String title, String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sendButton.setEnabled(false);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, PendingSubmissionListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSER_RESULT) {
            if (data == null) {
                // If cancelled, do nothing!
                sendButton.setEnabled(true);
                return;
            }
            // Otherwise start adding photos - display submission complete screen then
            // the email client.
            new SendPendingSubmissionProcess(data).execute();
        } else if (requestCode == EMAILING_RESULT) {
            // Display completion screen
            startActivity(new Intent(this.getBaseContext(), SubmissionCompleteActivity.class));
        }
    }

    /**
     * Process of sending a submission that has already been stored for sending later.
     */
    public class SendPendingSubmissionProcess extends AsyncTask<Void, Void, String> {
        private Intent data;

        public SendPendingSubmissionProcess(Intent data) {
            this.data = data;
        }

        /**
         * To begin, show message that tells the user the email is being generated, and
         * turn on the spinner (bar).
         */
        @Override
        protected void onPreExecute() {
            sendButton.setEnabled(false);
            Toast toast = Toast.makeText(activity.getBaseContext(), "Creating submission...", Toast.LENGTH_SHORT);
            toast.show();
        }

        /**
         * When finished, remove the message and turn off the spinner.
         * @param result - not used directly.
         */
        @Override
        protected void onPostExecute(String result) {
            sendButton.setEnabled(true);
            Toast toast = Toast.makeText(activity.getBaseContext(), "Opening email program...", Toast.LENGTH_SHORT);
            toast.show();
        }

        @Override
        public String doInBackground(Void... inputs) {
            String id = PendingSubmissionDetailFragment.itemID;
            PendingSubmissionCell.PendingSubmission item = PendingSubmissionCell.ITEM_MAP.get(id);

            // Email the submission
            Uri attachment = Uri.parse(item.photoZipPath);
            Utilities.exportSubmission(item);
            emailIntent = Utilities.sendEmail(getString(R.string.queensland_herbarium_email),
                    currentSubmission, attachment);

            if (emailIntent == null) {
                return null;
            }

            ComponentName component = data.getComponent();
            emailIntent.setComponent(component);
            startActivityForResult(emailIntent, EMAILING_RESULT);

            // Delete submission from database
            PendingSubmissionStorage.PendingSubmissionEntry.deleteItem(Long.parseLong(id));
            return "";
        }
    }

}
