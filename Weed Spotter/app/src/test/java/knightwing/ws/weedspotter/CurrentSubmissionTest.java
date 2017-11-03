package knightwing.ws.weedspotter;

/**
 * Created by tbric123 on 12/8/17.
 */

import org.junit.Assert;
import org.junit.Test;

import knightwing.ws.weedspotter.Models.PlantIdentification.CurrentSubmission;
import knightwing.ws.weedspotter.Models.PlantIdentification.PlantGrowth;
import knightwing.ws.weedspotter.Models.PlantIdentification.PlantType;

public class CurrentSubmissionTest {
    CurrentSubmission testSubmissionData = CurrentSubmission.getInstance();
    String[] testPhotos = {"wholeplant.jpeg", "leaf.jpg", "flower.jpg", "fruit.jpg", "extra1.jpg",
                            "extra2.jpg"};

    // Test presence of initial data in the object and clearing it.
    @Test
    public void testInitialDataFillingAndClearing() {
        // Fill in data - check if form data (email, name, plant growth, plant type) is given
        testSubmissionData.clearSubmission();
        testSubmissionData.setSenderName("George Costanza");
        Assert.assertTrue(testSubmissionData.senderNameGiven());

        testSubmissionData.setSendDate(2017, 5, 17);
        Assert.assertTrue(testSubmissionData.dateGiven());

        testSubmissionData.setSendDate("17/6/2017");
        Assert.assertTrue(testSubmissionData.dateGiven());

        testSubmissionData.setPlantType(PlantType.HERB);
        Assert.assertTrue(testSubmissionData.plantTypeChosen());

        testSubmissionData.setPlantGrowth(PlantGrowth.GROWING_WILD);
        Assert.assertTrue(testSubmissionData.plantGrowthChosen());

        // Set a longitude and latitude
        Assert.assertFalse(testSubmissionData.locationHasBeenSet());
        testSubmissionData.setLatitude(56);
        testSubmissionData.setLongitude(145);
        Assert.assertTrue(testSubmissionData.locationHasBeenSet());

        // Set user's closest town/suburb
        testSubmissionData.setClosestTownSuburb("Kuraby");

        // Add photos and check whether or not the whole plant and leaf ones are there
        for (int i = 0; i < testPhotos.length; i++) {
            testSubmissionData.addPhoto(testPhotos[i], i);
            if (i == 0) {
                Assert.assertTrue(testSubmissionData.haveWholePlantPhoto());
            } else if (i == 1) {
                Assert.assertTrue(testSubmissionData.havePlantLeafPhoto());
            }
        }

        // Set extra notes
        testSubmissionData.setNotes("Has corrosive sap - watch out!");

        // Test that data is held in the object
        Assert.assertEquals("George Costanza", testSubmissionData.getSenderName());
        Assert.assertEquals("17/6/2017", testSubmissionData.getSubmissionDate());
        Assert.assertEquals(PlantType.HERB, testSubmissionData.getPlantType());
        Assert.assertEquals(PlantGrowth.GROWING_WILD, testSubmissionData.getPlantGrowth());
        Assert.assertEquals("Kuraby", testSubmissionData.getClosestTownSuburb());
        Assert.assertTrue(Math.abs(56 - testSubmissionData.getLatitude()) < 0.001);
        Assert.assertTrue(Math.abs(145 - testSubmissionData.getLongitude()) < 0.001);

        for (int i = 0; i < testPhotos.length; i++) {
            Assert.assertEquals(testPhotos[i], testSubmissionData.getPhoto(i));
        }

        // Clear it all away
        testSubmissionData.clearSubmission();
        Assert.assertEquals("", testSubmissionData.getSenderName());
        Assert.assertEquals("", testSubmissionData.getSubmissionDate());
        Assert.assertEquals(null, testSubmissionData.getPlantType());
        Assert.assertEquals(null, testSubmissionData.getPlantGrowth());
        Assert.assertEquals(null, testSubmissionData.getAttachmentPath());
        for (int i = 0; i < testSubmissionData.getPhotos().size(); i++) {
            Assert.assertEquals(null, testSubmissionData.getPhotos().get(i));
        }

        Assert.assertEquals("", testSubmissionData.getNotes());
        Assert.assertEquals("", testSubmissionData.getClosestTownSuburb());
    }

    // Test that only one instance of the object is created - that any other references
    // point to the same object.
    @Test
    public void testSingleCreation() {
        // Fill in data - check if form data (name, plant growth, plant type) is given
        testSubmissionData.setSenderName("George Costanza");
        Assert.assertTrue(testSubmissionData.senderNameGiven());

        testSubmissionData.setSendDate(2017, 5, 17);
        Assert.assertTrue(testSubmissionData.dateGiven());

        testSubmissionData.setSendDate("17/6/2017");
        Assert.assertTrue(testSubmissionData.dateGiven());

        testSubmissionData.setPlantType(PlantType.HERB);
        Assert.assertTrue(testSubmissionData.plantTypeChosen());

        testSubmissionData.setPlantGrowth(PlantGrowth.GROWING_WILD);
        Assert.assertTrue(testSubmissionData.plantGrowthChosen());

        // Set a longitude and latitude
        testSubmissionData.setLatitude(56);
        testSubmissionData.setLongitude(145);

        // Set user's closest town/suburb
        testSubmissionData.setClosestTownSuburb("Kuraby");

        // Add photos and check whether or not the whole plant and leaf ones are there
        for (int i = 0; i < testPhotos.length; i++) {
            testSubmissionData.addPhoto(testPhotos[i], i);
            if (i == 0) {
                Assert.assertTrue(testSubmissionData.haveWholePlantPhoto());
            } else if (i == 1) {
                Assert.assertTrue(testSubmissionData.havePlantLeafPhoto());
            }
        }

        testSubmissionData.setNotes("Has corrosive sap - watch out!");

        // Should just be testSubmissionData - only one instance created!
        CurrentSubmission testSubmissionDataCopy = CurrentSubmission.getInstance();

        Assert.assertEquals("George Costanza", testSubmissionDataCopy.getSenderName());
        Assert.assertEquals("17/6/2017", testSubmissionDataCopy.getSubmissionDate());
        Assert.assertEquals(PlantType.HERB, testSubmissionDataCopy.getPlantType());
        Assert.assertEquals(PlantGrowth.GROWING_WILD, testSubmissionDataCopy.getPlantGrowth());
        Assert.assertEquals("Kuraby", testSubmissionData.getClosestTownSuburb());
        for (int i = 0; i < testPhotos.length; i++) {
            Assert.assertEquals(testPhotos[i], testSubmissionData.getPhoto(i));
        }

        Assert.assertEquals("Has corrosive sap - watch out!", testSubmissionDataCopy.getNotes());
    }
}
