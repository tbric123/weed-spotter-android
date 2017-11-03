package knightwing.ws.weedspotter.Models.PendingSubmissions;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import knightwing.ws.weedspotter.Models.PlantIdentification.PlantGrowth;
import knightwing.ws.weedspotter.Models.PlantIdentification.PlantType;
import knightwing.ws.weedspotter.Models.PlantIdentification.Utilities;
import knightwing.ws.weedspotter.Views.Activities.SubmissionStages.StartupScreenActivity;

/**
 * Contains the date and location of a single submission as seen on a list of submissions.
 */
public class PendingSubmissionCell {

    // Database of pending submissions
    public static final SQLiteDatabase pendingSubmissionsDatabase = StartupScreenActivity.getPssHelper().getReadableDatabase();

    // Query result of getting every pending submission
    public static Cursor allItems = pendingSubmissionsDatabase.query(PendingSubmissionStorage.PendingSubmissionEntry.TABLE_TITLE,
            PendingSubmissionStorage.psdColumns, null, null, null, null, null, null);

    // An array of pending submissions.
    public static final List<PendingSubmission> ITEMS = new ArrayList<>();

    // A map of pending submissions, by ID.
    public static final Map<String, PendingSubmission> ITEM_MAP = new HashMap<>();

    /**
     * Recreates a list of all submissions stored in the database.
     */
    public static void refreshItems() {
        ITEMS.clear();
        ITEM_MAP.clear();
        allItems = pendingSubmissionsDatabase.query(PendingSubmissionStorage.PendingSubmissionEntry.TABLE_TITLE,
                PendingSubmissionStorage.psdColumns, null, null, null, null, null);
        while (allItems.moveToNext()) {

            // Get values
            long id = allItems.getLong(allItems.getColumnIndexOrThrow(PendingSubmissionStorage.PendingSubmissionEntry._ID));
            String senderName = allItems.getString(allItems.getColumnIndexOrThrow(PendingSubmissionStorage.PendingSubmissionEntry.NAME_TITLE));
            String submissionDate = allItems.getString(allItems.getColumnIndexOrThrow(PendingSubmissionStorage.PendingSubmissionEntry.SUBMISSION_DATE_TITLE));
            double latitude = allItems.getDouble(allItems.getColumnIndexOrThrow(PendingSubmissionStorage.PendingSubmissionEntry.LATITUDE_TITLE));
            double longitude = allItems.getDouble(allItems.getColumnIndexOrThrow(PendingSubmissionStorage.PendingSubmissionEntry.LONGITUDE_TITLE));
            String plantType = allItems.getString(allItems.getColumnIndexOrThrow(PendingSubmissionStorage.PendingSubmissionEntry.PLANT_TYPE_TITLE));
            String plantGrowth = allItems.getString(allItems.getColumnIndexOrThrow(PendingSubmissionStorage.PendingSubmissionEntry.PLANT_GROWTH_TITLE));
            String photoZipPath = allItems.getString(allItems.getColumnIndexOrThrow(PendingSubmissionStorage.PendingSubmissionEntry.PHOTOS_ZIP_TITLE));
            String closestLocation = allItems.getString(allItems.getColumnIndexOrThrow(PendingSubmissionStorage.PendingSubmissionEntry.CLOSEST_LOCATION_TITLE));
            String notes = allItems.getString(allItems.getColumnIndexOrThrow(PendingSubmissionStorage.PendingSubmissionEntry.NOTES_TITLE));

            // Add a new pending submission
            addItem(createPendingSubmission(id, senderName, submissionDate, latitude, longitude,
                    plantType, plantGrowth, photoZipPath, closestLocation, notes));
        }
    }

    /**
     * Adds a pending submission to the list and map to be displayed in the list.
     * @param item - the pending submission to be added
     * @require item != null && has no information left out of it.
     * @ensure item can be seen in the list as the last item.
     */
    private static void addItem(PendingSubmission item) {
        ITEMS.add(item);
        ITEM_MAP.put(String.valueOf(item.id), item);
    }

    /**
     * Creates a Pending submission based on individual pieces of information as per the database
     * table.
     * @param id - the submission's position in the list
     * @param senderName - name of the person who stored the submission
     * @param submissionDate - the date the submission was stored
     * @param latitude - latitude of the submission location
     * @param longitude - longitude of the submission location
     * @param plantType - type of plant that is thought to have been spotted
     * @param plantGrowth - how the plant is thought to be growing
     * @param photoZipPath - where the archive of images taken is stored
     * @param closestLocation - the town/suburb closest to the submission
     * @param notes - extra notes written about the submission
     * @return a new pending submission that can be browsed through.
     * @require id > 0 && senderName != null && submissionDate != null && submissionDate is a valid
     *          date && latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180
     *          && plantType != null && plantType in PlantType && plantGrowth != null && plantGrowth
     *          in PlantGrowth && photoZipPath != null && photoZipPath points to a valid zip file
     *          && notes != null
     * @ensure all information is present in a single pending submission that can be part of an
     *          easy to browse list
     */
    private static PendingSubmission createPendingSubmission(long id, String senderName,
                                                             String submissionDate, double latitude,
                                                             double longitude, String plantType,
                                                             String plantGrowth, String photoZipPath,
                                                             String closestLocation, String notes) {
        return new PendingSubmission(id, senderName, submissionDate, latitude, longitude, plantType,
                plantGrowth, photoZipPath, closestLocation, notes);
    }

    /**
     * A submission ready to be sent.
     */
    public static class PendingSubmission {
        public long id;
        public String senderName;
        public String sendDate;
        public double latitude;
        public double longitude;
        public String closestLocation;
        public String plantType;
        public String plantGrowth;
        public String photoZipPath;
        public String notes;
        public String details;

        public PendingSubmission(long id, String senderName, String sendDate, double latitude,
                                 double longitude, String plantType, String plantGrowth,
                                 String photoZipPath, String closestLocation, String notes) {
            this.id = id;
            this.senderName = senderName;
            this.sendDate = sendDate;
            this.latitude = latitude;
            this.longitude = longitude;
            this.plantType = plantType;
            this.plantGrowth = plantGrowth;
            this.photoZipPath = photoZipPath;
            this.closestLocation = closestLocation;
            this.notes = notes;

            // Construct all the information that will be shown in a single view of a pending
            // submission.
            StringBuilder detailParts = new StringBuilder();
            detailParts.append("Made on: " + this.sendDate + "\n\n");
            if (!(Math.abs(this.latitude - -200) < 0.001 || Math.abs(this.longitude - -200) < 0.001)) {
                detailParts.append("Location: (" + this.latitude + ", " + this.longitude + ")\n\n");
            }

            detailParts.append("Closest to: ");
            detailParts.append(this.closestLocation);
            detailParts.append("\n\n");

            detailParts.append("Sender Name: " + this.senderName + "\n\n");
            detailParts.append("Plant Type: " +
                    Utilities.formatType(PlantType.valueOf(this.plantType)) + "\n\n");
            detailParts.append("Plant Growth: " +
                    Utilities.formatGrowth(PlantGrowth.valueOf(this.plantGrowth)) + "\n\n");
            if (!notes.isEmpty()) {
                detailParts.append("Notes:\n" + this.notes + "\n\n");
            }

            // Reassurance that images are still part of the submission
            detailParts.append("All images taken are attached to this submission.\n");

            this.details = detailParts.toString();
        }

        /**
         * Get a text summary of important information that will represent a single pending
         * submission when listed.
         * @return what will be displayed when the submission is seen in the list of submissions.
         */
        public String getListEntry() {
            String introduction = this.senderName + ": On " + this.sendDate;
            String location;
            if (!(Math.abs(this.latitude - -200) < 0.001 || Math.abs(this.longitude - -200) < 0.001)) {
                location = "\nAt (" + this.latitude + ", " + this.longitude + ")";
            } else {
                location = "\nAt: " + this.closestLocation;
            }

            introduction += location;
            String plantInformation = "\nType: " + Utilities.formatType(PlantType.valueOf(this.plantType))
                    + "\n" + "Growth: " + Utilities.formatGrowth(PlantGrowth.valueOf(this.plantGrowth));
            return introduction + plantInformation;
        }
    }
}
