package knightwing.ws.weedspotter;

import org.junit.Assert;
import org.junit.Test;

import knightwing.ws.weedspotter.Models.PlantIdentification.CurrentSubmission;
import knightwing.ws.weedspotter.Models.PlantIdentification.PlantGrowth;
import knightwing.ws.weedspotter.Models.PlantIdentification.PlantType;
import knightwing.ws.weedspotter.Models.PlantIdentification.Utilities;

/**
 * Test that email contents are generated correctly, and that the plant type and growth are properly
 * formatted within the email contents.
 */

public class UtilitiesTest {

    CurrentSubmission submission = CurrentSubmission.getInstance();

    // Fixed sections of the email
    private final String startGreeting = "Dear Queensland Herbarium,\n\n";
    private final String initialLine = "I would like to report a possible weed sighting:\n\n";
    private final String plantTypeHeader = "Plant Type: ";
    private final String plantGrowthHeader = "\nPlant Growth: ";
    private final String gpsLocationHeader = "\nFound at: (";
    private final String dateHeader = "\nOn: ";
    private final String closestSuburbTownHeader = "\nClosest Town/Suburb: ";
    private final String extraNotesHeader = "\n\nExtra Notes:\n";
    private final String attachmentReminder = "\n\nPlease find attached a zip archive of images.\n\n";
    private final String closingGreeting = "Kind regards,\n";
    private final String lastLine = "\n\nThis email was generated automatically.";

    // Test data
    private final String senderName = "Tom B";
    private final String sendDate = "24/10/17";
    private final PlantType plantType = PlantType.NOT_SURE;
    private final PlantGrowth plantGrowth = PlantGrowth.GROWING_WILD;
    private final double latitude = -77.789;
    private final double longitude = 170.222;
    private final String closestSuburbTown = "Kuraby";
    private final String extraNotes = "Poisonous leaves";

    // The plant growth and type must be formatted correctly
    // so that all underscores are replaced with spaces and
    // only the starting letter is a capital.
    @Test
    public void testFormatPlantTypeSpaces() {
        PlantType type = PlantType.NOT_SURE;
        String formattedType = Utilities.formatType(type);
        Assert.assertEquals("Not sure", formattedType);
    }

    @Test
    public void testFormatPlantTypeCapitalStart() {
        PlantType type = PlantType.VINE;
        String formattedType = Utilities.formatType(type);
        Assert.assertEquals("Vine", formattedType);
    }

    @Test
    public void testFormatPlantGrowthSpaces() {
        PlantGrowth type = PlantGrowth.GROWING_WILD;
        String formattedType = Utilities.formatGrowth(type);
        Assert.assertEquals("Growing wild", formattedType);
    }

    @Test
    public void testFormatPlantGrowthCapitalStart() {
        PlantGrowth type = PlantGrowth.CULTIVATED;
        String formattedType = Utilities.formatGrowth(type);
        Assert.assertEquals("Cultivated", formattedType);
    }

    // The contents of the email must correctly reflect the information
    // provided in the plant information and extra notes screens by the user
    @Test
    public void testEmailContentCreation() {
        // GPS and notes
        String expectedContents = setupCurrentSubmission(true, true);
        String actualContents = Utilities.generateEmail(submission);
        Assert.assertEquals(expectedContents, actualContents);

        // GPS and no notes
        expectedContents = setupCurrentSubmission(true, false);
        actualContents = Utilities.generateEmail(submission);
        Assert.assertEquals(expectedContents, actualContents);

        // No GPS, notes
        expectedContents = setupCurrentSubmission(false, true);
        actualContents = Utilities.generateEmail(submission);
        Assert.assertEquals(expectedContents, actualContents);

        // No GPS, no notes
        expectedContents = setupCurrentSubmission(false, false);
        actualContents = Utilities.generateEmail(submission);
        Assert.assertEquals(expectedContents, actualContents);
    }

    /**
     * Helper function to provide all initial required details for a
     * submission.
     */
    private void setAllInitialDetails() {
        submission.setSenderName(senderName);
        submission.setSendDate(sendDate);
        submission.setPlantType(plantType);
        submission.setPlantGrowth(plantGrowth);
        submission.setClosestTownSuburb(closestSuburbTown);
    }

    private StringBuilder setupEmailStartContents(StringBuilder emailContents) {
        emailContents.append(startGreeting);
        emailContents.append(initialLine);

        // Plant type header
        emailContents.append(plantTypeHeader);
        return emailContents;
    }

    private StringBuilder setupEmailEndContents(StringBuilder emailContents) {
        // Conclusion
        emailContents.append(attachmentReminder);
        emailContents.append(closingGreeting);
        emailContents.append(senderName);
        emailContents.append(lastLine);
        return emailContents;
    }

    /**
     * Prepare for testing the case where email contents
     *
     */
    private String setupCurrentSubmission(boolean withGPS, boolean withNotes) {
        StringBuilder emailContents = new StringBuilder();

        // Set up the submission itself
        submission.clearSubmission();
        setAllInitialDetails();

        // Put together the expected email contents
        emailContents = setupEmailStartContents(emailContents);
        emailContents.append(Utilities.formatType(plantType));
        emailContents.append(plantGrowthHeader);
        emailContents.append(Utilities.formatGrowth(plantGrowth));

        // GPS coordinates if desired
        if (withGPS) {
            submission.setLatitude(latitude);
            submission.setLongitude(longitude);
            emailContents.append(gpsLocationHeader);
            emailContents.append(latitude);
            emailContents.append(", ");
            emailContents.append(longitude);
            emailContents.append(")");
        }

        // Sending date
        emailContents.append(dateHeader);
        emailContents.append(sendDate);

        // Location - closest town/suburb
        emailContents.append(closestSuburbTownHeader);
        emailContents.append(closestSuburbTown);

        // Extra notes if desired
        if (withNotes) {
            emailContents.append(extraNotesHeader);
            emailContents.append(extraNotes);
            submission.setNotes(extraNotes);
        }

        emailContents = setupEmailEndContents(emailContents);
        return emailContents.toString();
    }


}
