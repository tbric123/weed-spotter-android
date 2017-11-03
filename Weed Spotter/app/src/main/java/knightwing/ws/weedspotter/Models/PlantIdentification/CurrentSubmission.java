package knightwing.ws.weedspotter.Models.PlantIdentification;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbric123 on 12/8/17.
 * Holds all information pertaining to a not-already-sent submission
 * so it can be retrieved at any stage during the submission process (i.e.
 * going back between screens). Can only be created once.
 */

public class CurrentSubmission {
    // User email details
    private String senderName = "";
    private String sendDate = "";

    // Plant information
    private PlantType plantType;
    private PlantGrowth plantGrowth;

    // Location: -200 is a "dummy location" for now (outside limits of longitude
    // and latitude)
    // NOTE: it is critical that these values are outside the sensible bounds of
    // longitude and latitude by default.
    private double latitude = -200;
    private double longitude = -200;

    // Closest town/suburb
    private String closestTownSuburb = "";

    // Photos
    private List<String> photos = new ArrayList<>();

    // Optional notes
    private String notes = "";

    // Attachment path
    private Uri attachmentPath = null;

    private static CurrentSubmission singleInstance;

    protected CurrentSubmission() {
        for (int i = 0; i < 6; i++) {
            photos.add(null);
        }
    }

    public static CurrentSubmission getInstance() {
        if (singleInstance == null) {
            singleInstance = new CurrentSubmission();
        }
        return singleInstance;
    }

    // Getters - useful for pre-filling screens
    public String getSenderName() {
        return this.senderName;
    }

    public String getSubmissionDate() {
        return this.sendDate;
    }

    public PlantType getPlantType() {
        return this.plantType;
    }

    public PlantGrowth getPlantGrowth() {
        return this.plantGrowth;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public String getClosestTownSuburb() {
        return this.closestTownSuburb;
    }

    /**
     * @return whether or not the location has been set.
     */
    public boolean locationHasBeenSet() {
        return !(this.latitude == -200 || this.longitude == -200);
    }

    public Uri getAttachmentPath() {
        return this.attachmentPath;
    }

    /**
     * Adds a photo to the submission, according to a given position:
     * 0 - whole plant
     * 1 - leaves
     * 2 - flowers
     * 3 - fruit
     * 4 - extra photo 1
     * 5 - extra photo 2
     * @param position - the type of photo you want to get
     * @require 0 <= position <= 5
     * @ensure you get the correct photo as specified in the comments above
     * @return the photo according to the comments above
     */
    public String getPhoto(int position) {
        return this.photos.get(position);
    }

    /**
     * Get all photos that are part of the submission.
     * @return all photos in submission
     */
    public List<String> getPhotos() {
        return new ArrayList<>(this.photos);
    }

    public String getNotes() {
        return this.notes;
    }

    // Setters - useful for submission process

    /**
     * Store the name of the sender.
     * @param senderName - the name of the WeedSpotter volunteer
     * @require senderName != null
     */
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    /**
     * Store the date that the submission is being made by specifying the year, month and day.
     * @param year - year submitted
     * @param month - month submitted
     * @param day - day submitted
     * @require year > 0 && 1 <= month <= 12 && day >= 1 && (day <= 30 when month = 4, 6, 9 or 11
     *          || day <= 31 when month != 2, 4, 6, 9 or 11 || day == 28 when month = 2 for non-leap
     *          years || day == 29 when month = 2 for leap years
     * @ensure date format internally is D/M/Y
     */
    public void setSendDate(int year, int month, int day) {
        this.sendDate = "" + day + "/" + month + "/" + year;
    }

    /**
     * Store the date that the submission is being made by specifying a date string.
     * @param date - the date submitted: D/M/Y
     * @require date.split("/").length() == 3 && date != null
     * @ensure date format internally is D/M/Y
     */
    public void setSendDate(String date) {
        this.sendDate = date;
    }

    /**
     * Sets the type of plant being submitted.
     * @param plantType - plant type
     * @require plantType != null
     */
    public void setPlantType(PlantType plantType) {
        this.plantType = plantType;
    }

    /**
     * Sets the growth behaviour of the plant being submitted.
     * @param plantGrowth - plant growth behaviour
     * @require plantGrowth != null
     */
    public void setPlantGrowth(PlantGrowth plantGrowth) {
        this.plantGrowth = plantGrowth;
    }

    /**
     * Sets the latitude the plant is located at.
     * @param latitude - global latitude location (degrees)
     * @require -90 <= latitude <= 90
     */
    public void setLatitude(double latitude) {

        this.latitude = latitude;
    }

    /**
     * Sets the longitude the plant is located at.
     * @param longitude - global longitude location (degrees)
     * @require -180 <= longitude <= 180
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Sets the user's closest town/suburb.
     * @param closestTownSuburb the closest town/suburb to the user
     * @require closestTownSuburb != null && closestTownSuburb != ""
     */
    public void setClosestTownSuburb(String closestTownSuburb) {
        this.closestTownSuburb = closestTownSuburb;
    }

    /**
     * Adds a photo to the submission, according to a given position:
     * 0 - whole plant
     * 1 - leaves
     * 2 - fruit
     * 3 - flowers
     * 4 - extra photo 1
     * 5 - extra photo 2
     * @param photoPosition - the type of photo you want to add
     * @require 0 <= photoPosition <= 5 && photo != null && photo points to an existing photo.
     */
    public void addPhoto(String photo, int photoPosition) {
        this.photos.set(photoPosition, photo);
    }

    /**
     * Sets extra notes about the submission.
     * @param notes - extra information about the submission
     * @require notes != null
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Sets the location of the zip archive of images that are part of this submission.
     * @param attachmentPath - where the archive is stored
     * @require attachmentPath != null && is accessible
     * @ensure the zip archive can be retrieved using this path
     */
    public void setAttachmentPath(Uri attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    /**
     * Specifies whether or not a photo of the whole plant has been taken.
     * @return true if there's a photo of the whole plant, else false.
     */
    public boolean haveWholePlantPhoto() {
        return this.photos.get(0) != null;
    }

    /**
     * Specifies whether or not a photo of the plant's leaves has been taken.
     * @return true if there's a photo of the plant's leaves, else false.
     */
    public boolean havePlantLeafPhoto() {
        return this.photos.get(1) != null;
    }

    /**
     * Specifies whether or not the user has specified their name.
     * @return true if the user's name has been given, else false.
     */
    public boolean senderNameGiven() {
        return !this.senderName.equals("");
    }

    /**
     * Specifies whether or not the user has specified the submission date.
     * @return true if the submission date has been given, else false.
     */
    public boolean dateGiven() {
        return !this.sendDate.equals("");
    }

    /**
     * Specifies whether or not the user has specified their closest suburb or town.
     * @return true if they have provided their closest suburb/town, else false.
     */
    public boolean closestTownSuburbGiven() {
        return !this.closestTownSuburb.equals("");
    }

    /**
     * Specifies whether or not a plant type has been chosen.
     * @return true if a plant type has been chosen, else false.
     */
    public boolean plantTypeChosen() {
        return this.plantType != null;
    }

    /**
     * Specifies whether or not a plant growth behaviour has been chosen.
     * @return true if a plant growth behaviour has been chosen, else false.
     */
    public boolean plantGrowthChosen() {
        return this.plantGrowth != null;
    }

    /**
     * Clear all details of a submission.
     */
    public void clearSubmission() {
        this.senderName = "";
        this.sendDate = "";
        this.plantType = null;
        this.plantGrowth = null;
        this.latitude = -200;
        this.longitude = -200;
        this.photos = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            this.photos.add(null);
        }
        this.attachmentPath = null;
        this.notes = "";
        this.closestTownSuburb = "";
    }
}
