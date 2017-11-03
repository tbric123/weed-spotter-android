package knightwing.ws.weedspotter.Models.PlantIdentification;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import knightwing.ws.weedspotter.Models.PendingSubmissions.PendingSubmissionCell;
import knightwing.ws.weedspotter.R;

/**
 * Created by malkav on 16/08/17.
 * Contains tools for obtaining the user's location, constructing an email, constructing an
 * attachment of images, and remembering the user's name for future submissions.
 */
public class Utilities {

    // Requests to use the camera and to change location settings
    public final static int CAMERA_REQUEST = 0x01;
    public final static int LOCATION_SETTINGS_REQUEST = 0x02;

    public static CurrentSubmission currentSubmission = CurrentSubmission.getInstance();

    /**
     * Checks to see whether or not the app has permission to access the device's location.
     * @param context - app context
     * @require context != null
     * @return whether or not the app currently can access the device's location
     */
    public static boolean hasLocationPermission(Context context) {
        int permission;
        permission = ContextCompat.checkSelfPermission(context,
                                                       Manifest.permission.ACCESS_FINE_LOCATION);

        return permission == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Prompts user to allow for device location to be accessed.
     * @param activity - the screen where location is requested
     * @require activity != null
     * @ensure permission is asked for before allowing location to be obtained
     */
    public static void requestLocationPermission(final Activity activity) {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle("Location is Required");
        dialogBuilder.setMessage("The Weed Spotters app requires permission to access your location.\n" +
                                 "This is so the Queensland Herbarium can easily find the weeds you report.");
        dialogBuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(activity,
                                                  new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                                                  1);

            }
        });
        dialogBuilder.setIcon(R.drawable.alerticon);
        dialogBuilder.show();
    }

    /**
     * Checks to see whether or not the device is tracking its location.
     * @param context - app context
     * @require context != null
     * @return whether or not the device is tracking its location
     */
    public static boolean locationIsOn(Context context) {
        try {
            int gpsOn;
            gpsOn = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            return gpsOn != 0;
        } catch(Settings.SettingNotFoundException e) {

        }
        return false;
    }

    /**
     * Creates an email attachment containing all of the images taken for the submission, as a Zip
     * archive.
     * @param context - the environment the app is running under
     * @param submission - the current submission data
     * @require context != null && filePaths != null and every path points to an image on the device
     * @return a path to the Zip archive of all images taken
     */
    public static Uri generateArchive(Context context, CurrentSubmission submission) {
        File file;
        ZipOutputStream out = null;
        System.out.println("Attempting to generate a zip file.");
        try {
            file = File.createTempFile("plant-images", ".zip",
                            context.getExternalFilesDir(context.getString(R.string.file_provider_authority)));
            out = new ZipOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            System.out.println("Error opening zip file for output.");
            return null;
        } catch (IOException e) {
            System.out.println("Error opening zip file for output.");
            return null;
        }

        // Make the CSV file with the submission date, sender name, plant type, plant growth,
        // latitude and longitude, user-defined location and notes.
        try {
            out.putNextEntry(new ZipEntry("data.csv"));
            StringBuilder csv = new StringBuilder();
            csv.append(submission.getSubmissionDate());
            csv.append(',');
            csv.append(submission.getSenderName());
            csv.append(',');
            if (submission.locationHasBeenSet()) {
                csv.append(submission.getLatitude());
            }
            csv.append(',');
            if (submission.locationHasBeenSet()) {
                csv.append(submission.getLongitude());
            }
            csv.append(',');
            csv.append(submission.getPlantType());
            csv.append(',');
            csv.append(submission.getPlantGrowth());
            csv.append(',');
            csv.append(submission.getClosestTownSuburb());
            csv.append(',');
            csv.append(submission.getNotes());
            csv.append("\r\n");
            out.write(csv.toString().getBytes());
            out.closeEntry();
        } catch (IOException e) {
            System.out.println("CSV file unable to be created");
        }

        // Add photos to the archive
        for (String path : submission.getPhotos()) {
            if (path == null) {
                continue;
            }
            System.out.println("Adding " + path);
            FileInputStream in;
            File imageFileToCompress = new File(path);
            try {

                // Compress the image
                Bitmap imageToCompress = BitmapFactory.decodeFile(imageFileToCompress.getPath());
                FileOutputStream compressedImageStream = new FileOutputStream(path);
                imageToCompress.compress(Bitmap.CompressFormat.JPEG, 50, compressedImageStream);
                compressedImageStream.flush();
                compressedImageStream.close();

                // Add the compressed image to the archive
                in = new FileInputStream(path);
                try {
                    String fileName = path.split("/")[path.split("/").length - 1];
                    out.putNextEntry(new ZipEntry(fileName));
                } catch(IOException e) {
                    System.out.println("Error adding zipfile entry ");
                    return null;
                }
                // Read in 4096 bytes at a time and write it out to the archive
                int bytes = 0;
                byte[] buffer = new byte[4096];
                try {
                    while ((bytes = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytes);
                    }
                } catch (IOException e) {
                    System.out.println("Error reading or writing wrt zip file.");
                    return null;
                }
                // done writing out the archive entry
                try {
                    out.closeEntry();
                } catch (IOException e) {
                    System.out.println("Error closing zipfile entry.");
                    return null;
                }
            } catch (FileNotFoundException e) {
                System.out.println("Error opening " + path + " for input.");
                return null;
            } catch (IOException e) {
                System.out.println("Error compressing image at " + path);
                return null;
            }
            imageFileToCompress.delete();
        }
        try {
            out.close();
        } catch (IOException e) {
            System.out.println("Error closing zipfile.");
            return null;
        }

        System.out.println("Finished writing zip file for a total of " + file.length() + " bytes");
        return FileProvider.getUriForFile(context,  context.getString(R.string.file_provider_authority), file);
    }

    /**
     * Builds an email that will be sent to the Queensland Herbarium as a weed submission.
     * @param submission - has all information needed for the email
     * @require submission != null and nothing in submission being null
     * @return the contents of the email that will be sent
     */
    public static String generateEmail(CurrentSubmission submission) {
        StringBuilder sb = new StringBuilder();

        // Introduction
        sb.append("Dear Queensland Herbarium,\n\n");
        sb.append("I would like to report a possible weed sighting:\n\n");

        // Plant type, growth, location and when found
        sb.append("Plant Type: ");
        sb.append(formatType(submission.getPlantType()));
        sb.append("\nPlant Growth: ");
        sb.append(formatGrowth(submission.getPlantGrowth()));

        // handle both location cases
        if (submission.locationHasBeenSet()) {
            sb.append("\nFound at: (");
            sb.append(submission.getLatitude());
            sb.append(", ");
            sb.append(submission.getLongitude());
            sb.append(")");
        }

        sb.append("\nOn: ");
        sb.append(submission.getSubmissionDate());
        sb.append("\nClosest Town/Suburb: ");
        sb.append(submission.getClosestTownSuburb().trim());

        // Include notes if present
        if (!submission.getNotes().isEmpty()) {
            sb.append("\n\nExtra Notes:\n");
            sb.append(submission.getNotes().trim());
        }

        // Conclusion
        sb.append("\n\nPlease find attached a zip archive of images.\n\n");
        sb.append("Kind regards,\n");
        sb.append(submission.getSenderName());
        sb.append("\n\nThis email was generated automatically.");

        return sb.toString();
    }

    /**
     * Send an email and generate an attachment for it.
     * @param toEmail - where the email will be sent to
     * @param context - where this call is being made
     * @param submission - the submission being sent
     * @require toEmail != null && a valid email address && context != null && submission != null
     *          && submission has no required information missing
     * @ensure user can see the screen of their chosen email program
     * @return a pointer to the application that will send the email
     */
    public static Intent sendEmail(String toEmail, Context context, CurrentSubmission submission) {
        // Create and attach the archive of photos
        Uri attachment = Utilities.generateArchive(context, submission);
        if (attachment == null) {
            return null;
        }
        return sendEmail(toEmail, submission, attachment);
    }

    /**
     * Send an email without generating an archive for it - provide this yourself.
     * @param toEmail - where the email will be sent to
     * @param submission - the submission being sent
     * @param attachment - the path to the file that will be attached and sent with the email
     * @require toEmail != null && a valid email address && submission != null && submission has no
     *          required information missing && attachment != null && attachment points to a valid
     *          file
     * @ensure user can see the screen of their chosen email program
     * @return a pointer to the application that will send the email
     */
    public static Intent sendEmail(String toEmail, CurrentSubmission submission, Uri attachment) {
        Uri sendToURI = Uri.parse("mailto:" + toEmail);
        Intent emailIntent = new Intent(Intent.ACTION_SEND, sendToURI);
        String subjectLine = "Weed Spotter App_" + submission.getSenderName().replace(' ', '_') + "_";
        if (submission.locationHasBeenSet()) {
            subjectLine += submission.getLatitude() + "_" + submission.getLongitude();
        } else {
            subjectLine += submission.getClosestTownSuburb().replace(' ', '_');
        }

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subjectLine);
        String body = Utilities.generateEmail(submission);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        emailIntent.putExtra(Intent.EXTRA_STREAM, attachment);
        return emailIntent;
    }

    /**
     * Launches a chooser for the user to pick an email program to use for
     * emailing their submission.
     * @param emailAddress where the email will be sent to
     * @require emailAddress != null && a valid email address
     * @ensure user can pick an email program effectively
     * @return an array of 2 intents - the chooser followed by the email program
     *          respectively
     */
    public static Intent[] createEmailChooser(String emailAddress) {
        Intent[] emailIntents = new Intent[2];

        Uri sendToURI = Uri.parse("mailto:" + emailAddress);

        // Client, chooser
        Intent emailClient = new Intent(Intent.ACTION_SENDTO, sendToURI);
        Intent chooser = new Intent(Intent.ACTION_PICK_ACTIVITY);
        chooser.putExtra(Intent.EXTRA_INTENT, emailClient);
        chooser.putExtra(Intent.EXTRA_TITLE, "Send an email.");

        // Add to array
        emailIntents[0] = chooser;
        emailIntents[1] = emailClient;

        return emailIntents;
    }

    /**
     * Outputs the string value of a plant type in a readable manner.
     * i.e. NOT_SURE -> Not sure
     * @param type - the plant type to be formatted in a nicer manner
     * @require type != null
     * @ensure Only first letter is capitalised and spaces instead of underscores
     * @return A more readable version of a plant type
     */
    public static String formatType(PlantType type) {
        String formattedType = "";
        String lowerCased = type.toString().toLowerCase().replace('_', ' ');

        formattedType = lowerCased.substring(0, 1).toUpperCase() + lowerCased.substring(1);
        return formattedType;
    }

    /**
     * Outputs the string value of a plant growth in a readable manner.
     * i.e. NOT_SURE -> Not sure
     * @param growth - the plant growth to be formatted in a nicer manner
     * @require growth != null
     * @ensure Only first letter is capitalised and spaces instead of underscores
     * @return A more readable version of a plant growth
     */
    public static String formatGrowth(PlantGrowth growth) {
        String formattedGrowth = "";
        String lowerCased = growth.toString().toLowerCase().replace('_', ' ');

        formattedGrowth = lowerCased.substring(0, 1).toUpperCase() + lowerCased.substring(1);
        return formattedGrowth;
    }

    /**
     * Provides access to the shared preferences file where the user's name will be stored.
     * @param activityContext - the activity that requires the shared preferences file
     * @require activityContext != null
     * @ensure the shared preferences file that contains the user's name is obtained
     * Based on code from https://developer.android.com/training/basics/data-storage/shared-preferences.html
     */
    public static SharedPreferences getNameStorage(Context activityContext) {
        return activityContext.getSharedPreferences(activityContext.getResources().getString(R.string.name_storage_key),
                Context.MODE_PRIVATE);
    }

    /**
     * Get the current location where the state of whether the user has chosen to skip the location
     * finding process or not is stored.
     * @param activityContext - the activity that requires the shared preferences file
     * @return where the location skip setting is stored
     * @require activityContext != null
     * @ensure the shared preferences file that contains the user's name is obtained
     * Based on code from https://developer.android.com/training/basics/data-storage/shared-preferences.html
     */
    public static SharedPreferences getLocationSkipStateStorage(Context activityContext) {
        return activityContext.getSharedPreferences(activityContext.getResources().getString(R.string.location_skip_state_key),
                Context.MODE_PRIVATE);
    }

    /**
     * If one is specified, refill the name text box with the stored Weed Spotter volunteer's name
     * @param nameStorage - storage location of the name
     * @param textbox - the name textbox
     * @param activityContext - the activity where refilling is to be done.
     * @require nameStorage != null && textbox != null && activityContext != null
     * @ensure user can see the name they entered previously, even after they close the app.
     * Based on code from https://developer.android.com/training/basics/data-storage/shared-preferences.html
     */
    public static void refillName(SharedPreferences nameStorage, EditText textbox,
                                    Context activityContext) {
        String defaultName = "";
        String filledName = nameStorage.getString(activityContext.getResources().getString(R.string.weed_spotter_name),
                defaultName);
        textbox.setText(filledName);
    }

    /**
     * Saves the collector's name for use in later submissions.
     * @param nameStorage - storage location of the name
     * @param submission - where name is located locally
     * @param activityContext - the activity where refilling is to be done.
     * @require nameStorage != null && submission != null && activityContext != null
     * @ensure user can retrieve the name they entered previously, even after they close the app.
     * Based on code from https://developer.android.com/training/basics/data-storage/shared-preferences.html
     */
    public static void saveName(SharedPreferences nameStorage, CurrentSubmission submission,
                                Context activityContext) {
        SharedPreferences.Editor nameStorageEditor = nameStorage.edit();
        nameStorageEditor.putString(activityContext.getString(R.string.weed_spotter_name), submission.getSenderName());
        nameStorageEditor.commit();
    }

    /**
     * Sets whether or not the user has chosen to skip the location finding process.
     * @param locationSkipState -
     * @param state - the new value of whether we are skipping or not.
     * @param activityContext - the activity where this setting is to be done.
     * @require locationSkipState != null && activityContext != null
     * @ensure choice to skip location finding process is reflected in the form screen as having
     *          manual latitude and location entry text boxes turned on (true) or off (false)
     */
    public static void setLocationSkipState(SharedPreferences locationSkipState, boolean state,
                                            Context activityContext) {
        SharedPreferences.Editor locationSkipStateEditor = locationSkipState.edit();
        locationSkipStateEditor.putBoolean(activityContext.getString(R.string.location_skip_state_key),
                state);
        locationSkipStateEditor.commit();
    }

    /**
     * Sets a button in the app as disabled or enabled by controlling its colour.
     * @param button - the button being enabled or disabled.
     * @param enabled - whether or not the button is to be enabled.
     * @require button != null
     * @ensure button is gray if disabled, otherwise tan.
     */
    public static void setButtonEnabled(Button button, boolean enabled) {
        button.setEnabled(enabled);
        if (enabled) {
            button.setBackgroundColor(Color.parseColor("#DDA030"));
        } else {
            button.setBackgroundColor(Color.GRAY);
            button.setTextColor(Color.parseColor("#222222"));
        }
    }

    /**
     * Exports a submission from the pending submissions database into a CurrentSubmission object
     * for processing into an email
     * @param submissionInDatabase - the submission from the pending submissions database.
     * @require submissionInDatabase != null && has all information needed for a submission
     * @ensure all data for a submission in the database is retained in the submission about to be
     *          sent
     */
    public static void exportSubmission(PendingSubmissionCell.PendingSubmission submissionInDatabase) {
        // Extract information from database's submission
        String senderName = submissionInDatabase.senderName;
        String sendDate = submissionInDatabase.sendDate;
        double latitude = submissionInDatabase.latitude;
        double longitude = submissionInDatabase.longitude;
        PlantType plantType = PlantType.valueOf(submissionInDatabase.plantType);
        PlantGrowth plantGrowth = PlantGrowth.valueOf(submissionInDatabase.plantGrowth);
        String closestLocation = submissionInDatabase.closestLocation;
        Uri photoZipPath = Uri.parse(submissionInDatabase.photoZipPath);
        String notes = submissionInDatabase.notes;

        // Place this information into the current submission
        currentSubmission.clearSubmission();
        currentSubmission.setSenderName(senderName);
        currentSubmission.setSendDate(sendDate);
        currentSubmission.setClosestTownSuburb(closestLocation);

        if (!(Math.abs(latitude - -200) < 0.001 || Math.abs(longitude - -200) < 0.001)) {
            currentSubmission.setLatitude(latitude);
            currentSubmission.setLongitude(longitude);
        }
        currentSubmission.setPlantType(plantType);
        currentSubmission.setPlantGrowth(plantGrowth);
        currentSubmission.setAttachmentPath(photoZipPath);
        currentSubmission.setNotes(notes);
    }

    /**
     * Checks to see whether or not the device is connected to the Internet.
     * @param activityContext - the context of the activity where an Internet connection is being
     *                        checked for.
     * @require activityContext != null
     * @ensure true if the device is connected to the Internet, otherwise false.
     * @return whether or not the device is connected to the Internet.
     */
    public static boolean connectedToInternet(Context activityContext) {
        ConnectivityManager internetChecker = (ConnectivityManager)activityContext.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = internetChecker.getActiveNetworkInfo();
        return !(network == null || !network.isConnected());
    }

    /**
     * Displays dialog box informing the user of a certain error.
     * @param activity - where the alert is being displayed
     * @param title - title of error dialog box
     * @param message - what has gone wrong
     * @require activity != null && title != null && title != "" && message != null && message != ""
     */
    public static void displayErrorAlert(final Activity activity, String title, String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alert.setIcon(R.drawable.alerticon); // https://icons8.com/icon/set/alert/androidL
        alert.show();
    }

    /**
     * Test to see if two floats are equal
     * @param numberOne first number
     * @param numberTwo second number
     * @return if equal within a reasonable number of decimal places, then true, else false
     */
    public static boolean floatEquals(float numberOne, float numberTwo) {
        boolean result = Math.abs(numberOne - numberTwo) < 0.0001;
        System.out.println(result);
        return result;
    }
}
