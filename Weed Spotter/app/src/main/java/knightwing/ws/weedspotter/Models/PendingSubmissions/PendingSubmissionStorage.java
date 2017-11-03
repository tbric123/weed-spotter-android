package knightwing.ws.weedspotter.Models.PendingSubmissions;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import knightwing.ws.weedspotter.Views.Activities.SubmissionStages.StartupScreenActivity;

/**
 * Storage location for pending submissions.
 */
public class PendingSubmissionStorage {
    private static SQLiteDatabase pendingSubmissionsDatabase = StartupScreenActivity.getPssHelper().getReadableDatabase();
    private PendingSubmissionStorage() {}

    // All columns that will form the table of pending submissions
    public static String [] psdColumns = {PendingSubmissionStorage.PendingSubmissionEntry._ID,
            PendingSubmissionStorage.PendingSubmissionEntry.NAME_TITLE,
            PendingSubmissionStorage.PendingSubmissionEntry.SUBMISSION_DATE_TITLE,
            PendingSubmissionStorage.PendingSubmissionEntry.LATITUDE_TITLE,
            PendingSubmissionStorage.PendingSubmissionEntry.LONGITUDE_TITLE,
            PendingSubmissionStorage.PendingSubmissionEntry.PLANT_TYPE_TITLE,
            PendingSubmissionStorage.PendingSubmissionEntry.PLANT_GROWTH_TITLE,
            PendingSubmissionStorage.PendingSubmissionEntry.PHOTOS_ZIP_TITLE,
            PendingSubmissionStorage.PendingSubmissionEntry.CLOSEST_LOCATION_TITLE,
            PendingSubmissionStorage.PendingSubmissionEntry.NOTES_TITLE};

    // A single pending submission in the database
    public static class PendingSubmissionEntry implements BaseColumns {
        public static final String TABLE_TITLE = "PendingSubmissions";
        public static final String NAME_TITLE = "SenderName";
        public static final String SUBMISSION_DATE_TITLE = "SubmissionDate";
        public static final String LATITUDE_TITLE = "Latitude";
        public static final String LONGITUDE_TITLE = "Longitude";
        public static final String PLANT_TYPE_TITLE = "PlantType";
        public static final String PLANT_GROWTH_TITLE = "PlantGrowth";
        public static final String PHOTOS_ZIP_TITLE = "Photos";
        public static final String NOTES_TITLE = "Notes";
        public static final String CLOSEST_LOCATION_TITLE = "ClosestLocation";

        /**
         * Get the SQL query for creating a table for pending submissions
         * to go into.
         * @return a valid SQL query for forming the pending submissions table.
         */
        public static String getTableCreationQuery() {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("CREATE TABLE ");
            queryBuilder.append(TABLE_TITLE + " (");
            queryBuilder.append(_ID);
            queryBuilder.append(" INTEGER PRIMARY KEY,");
            queryBuilder.append(NAME_TITLE + " TEXT,");
            queryBuilder.append(SUBMISSION_DATE_TITLE + " TEXT,");
            queryBuilder.append(LATITUDE_TITLE + " DECIMAL,");
            queryBuilder.append(LONGITUDE_TITLE + " DECIMAL,");
            queryBuilder.append(PLANT_TYPE_TITLE + " TEXT,");
            queryBuilder.append(PLANT_GROWTH_TITLE + " TEXT,");
            queryBuilder.append(PHOTOS_ZIP_TITLE + " TEXT,");
            queryBuilder.append(CLOSEST_LOCATION_TITLE + " TEXT,");
            queryBuilder.append(NOTES_TITLE + " TEXT)");

            return queryBuilder.toString();
        }

        /**
         * Obtain the query for deleting the pending submissions table.
         * @return an SQL statement that will drop the pending submissions table
         */
        public static String getTableDeletionQuery() {
            return "DROP TABLE IF EXISTS " + TABLE_TITLE;
        }

        /**
         * Get the number of submissions currently being stored.
         * @return how many submissions still need to be sent.
         */
        public static int getNumberOfSubmissions() {
            String[] columns = new String[1];
            columns[0] = _ID;
            Cursor countContainer = pendingSubmissionsDatabase.query(TABLE_TITLE, columns,
                    null, null, null, null, null, null);
            int count = countContainer.getCount();
            countContainer.close();
            return count;
        }

        /**
         * Delete a single submission from the database based on its ID.
         * @param id the ID of the submission being deleted
         * @require id > 0 && id <= number of submissions
         * @ensure the item with the specified is completely deleted
         */
        public static void deleteItem(long id) {
            pendingSubmissionsDatabase.execSQL("DELETE FROM " +
                    PendingSubmissionStorage.PendingSubmissionEntry.TABLE_TITLE + " WHERE " +
                    PendingSubmissionStorage.PendingSubmissionEntry._ID + " = " + id + ";");
            resetSubmissionIDs();
            PendingSubmissionCell.refreshItems();
        }

        /**
         * Get all submissions from the database.
         * @return a query result of obtaining all submissions from the database
         * @ensure all items can be obtained from this result
         */
        public static Cursor getAllItems() {
            return pendingSubmissionsDatabase.query(TABLE_TITLE, psdColumns, null, null,
                    null, null, null);
        }

        /**
         * Adjusts all of the submission IDs to ensure they are between 1 and the number of
         * submissions and in order from smallest to largest.
         * @ensure submission IDs are between 1 and the number of submissions present
         */
        private static void resetSubmissionIDs() {
            Cursor items = getAllItems();
            List<Long> idsFound = new ArrayList<>();

            // Compile a list of all ids found
            while (items.moveToNext()) {
                long id = items.getLong(items.getColumnIndexOrThrow(PendingSubmissionStorage.PendingSubmissionEntry._ID));
                idsFound.add(id);
            }

            // Replace these with numbers from 1 to the number of IDs currently found
            for (int i = 0; i < idsFound.size(); i++) {
                int newID = i + 1;
                pendingSubmissionsDatabase.execSQL("UPDATE " + TABLE_TITLE + " SET " + _ID + "=" +
                        newID + " WHERE " + _ID + "=" + idsFound.get(i));
            }
        }
    }
}
