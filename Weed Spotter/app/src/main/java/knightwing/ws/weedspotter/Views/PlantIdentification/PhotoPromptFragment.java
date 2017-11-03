package knightwing.ws.weedspotter.Views.PlantIdentification;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import knightwing.ws.weedspotter.Models.PlantIdentification.CurrentSubmission;
import knightwing.ws.weedspotter.Models.PlantIdentification.Utilities;
import knightwing.ws.weedspotter.R;
import knightwing.ws.weedspotter.Views.Widgets.PageIndicatorView;

import static android.app.Activity.RESULT_OK;

/**
 * A fragment for prompting the user to take photos.
 */
public class PhotoPromptFragment extends Fragment {
    /**
     * The fragment argument representing the message.
     */
    private static final String ARG_PHOTO_TYPE = "photo_type";
    private static final String ARG_PAGE_NUMBER = "page_number";
    private static final String ARG_MAX_PAGES = "max_pages";

    // Names for each photo
    private static Map<Integer, String> photoNames = new HashMap<>();

    // Prompts to be displayed for particular photo types
    private static Map<Integer, String> photoPrompts = new HashMap<>();

    // Placeholders for each image type
    private static Map<Integer, Integer> placeholders = new HashMap<>();

    private File imageFile;

    public PhotoPromptFragment() {
        // populate map of photo types used for file name purposes
        photoNames.put(0, "whole-plant");
        photoNames.put(1, "leaves");
        photoNames.put(2, "flowers");
        photoNames.put(3, "fruit");
        photoNames.put(4, "extra1");
        photoNames.put(5, "extra2");

        // populate the map with the prompts which will appear above the camera button
        photoPrompts.put(0, "Please take a photo of the whole plant.");
        photoPrompts.put(1, "Please take a photo of the \nleaves.");
        photoPrompts.put(2, "Please take a photo of the flowers, if present.");
        photoPrompts.put(3, "Please take a photo of the fruit,\n if present.");
        photoPrompts.put(4, "Please take a photo of other important features (e.g. bark).");
        photoPrompts.put(5, "Please take another photo of other important features (e.g. sap).");

        // Placeholder images to display
        placeholders.put(0, R.drawable.wholeplanticon);
        placeholders.put(1, R.drawable.leavesicon);
        placeholders.put(2, R.drawable.flowericon);
        placeholders.put(3, R.drawable.fruiticon);
        placeholders.put(4, R.drawable.otherfeaturesicon);
        placeholders.put(5, R.drawable.otherfeaturesicon);
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PhotoPromptFragment newInstance(int photoMode, int page, int maxPages) {
        PhotoPromptFragment fragment = new PhotoPromptFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PHOTO_TYPE, photoMode);
        args.putInt(ARG_PAGE_NUMBER, page);
        args.putInt(ARG_MAX_PAGES, maxPages);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Set the image taken to be displayed on a particular fragment
     * @param path - where the image is on the device
     * @param img - where the image is displayed
     * @require path != null && path is a valid image && img != null
     * @ensure no image is lost and can be retrieved after capturing it.
     */
    private void setImage(String path, ImageView img) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, bmOptions);

        float width = bmOptions.outWidth;
        float height = bmOptions.outHeight;
        float viewWidth = img.getWidth();
        float viewHeight = img.getHeight();

        // far less dodgy hack -- assume the same size as the screen
        // since the photo will take up most of the screen anyway
        // it's better than before but maybe fixme
        if (viewWidth == 0 || viewHeight == 0) {
            Toast.makeText(getContext(), "guessing imageview size", Toast.LENGTH_LONG);
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            viewWidth = size.x;
            viewHeight = size.y;
        }

        int scale = (int) Math.ceil(Math.max(width/viewWidth, height/viewHeight));

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scale;

        Bitmap bmp = BitmapFactory.decodeFile(path, bmOptions);
        img.setImageBitmap(bmp);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If this result comes from the camera app and the user chooses to keep the image,
        // store it.
        if (requestCode == Utilities.CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                ImageView preview = (ImageView)getView().findViewById(R.id.image_view);
                if ((!Utilities.floatEquals(preview.getPivotX(), 216) ||
                        !Utilities.floatEquals(preview.getPivotY(), 186)) &&
                        Utilities.floatEquals(preview.getRotation(), 0)) {
                    preview.setRotation(90); // For Samsung Devices
                }
                setImage(imageFile.getAbsolutePath(), preview);
            } else {
                System.out.println("Chose to cancel!");
                CurrentSubmission.getInstance().addPhoto(null, getArguments().getInt(ARG_PHOTO_TYPE));
            }
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_capture_photo, container, false);

        // Set up the label to specify instructions to the user, and display a photo if one has
        // been already taken.
        TextView textView = (TextView) rootView.findViewById(R.id.txt_message);
        final int photoType = getArguments().getInt(ARG_PHOTO_TYPE);
        preparePhotoDisplay(rootView, photoType, textView);

        // Set up the camera button
        ImageButton btn = (ImageButton) rootView.findViewById(R.id.btn_camera);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePhotoIntent.resolveActivity(v.getContext().getPackageManager()) != null) {

                    takePhotoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                    try {
                        preparePhotoSaving(photoType);
                    } catch (IOException e) {
                        Toast.makeText(getContext(),
                                "Error saving photo.",
                                Toast.LENGTH_LONG).show();
                    }
                    Context c = getContext();
                    Uri imageUri = FileProvider.getUriForFile(c, c.getString(R.string.file_provider_authority), imageFile);
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(takePhotoIntent, Utilities.CAMERA_REQUEST);
                }
            }
        });

        // Setup the back button
        ImageButton backbtn = (ImageButton) rootView.findViewById(R.id.backbtn);
        if (photoType == 0) {
            backbtn.setVisibility(View.INVISIBLE);
            backbtn.setEnabled(false);
        } else {
            backbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ReportActivity) getActivity()).previousFragment();
                }
            });
        }

        // Setup the forward button
        ImageButton forwardbtn = (ImageButton) rootView.findViewById(R.id.forwardbtn);
        forwardbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ReportActivity)getActivity()).nextFragment();
            }
        });

        // Setup page indicator
        PageIndicatorView indicator = (PageIndicatorView) rootView.findViewById(R.id.page_indicator);
        indicator.setTotalSteps(getArguments().getInt(ARG_MAX_PAGES));
        indicator.setCurrentStep(getArguments().getInt(ARG_PAGE_NUMBER));

        return rootView;
    }

    /**
     * Prepare to display the correct photo according to the screen being displayed.
     * @param rootView - the main view of all photo fragments
     * @param photoType - the type of photo being saved to/shown
     * @param textView - where the prompt text will be displayed
     * @require rootView != null && 0 <= photoType <= 5
     * @ensure correct photo is being viewed or saved to
     */
    private void preparePhotoDisplay(View rootView, int photoType, TextView textView) {
        textView.setText(photoPrompts.get(photoType));
        CurrentSubmission submission = CurrentSubmission.getInstance();
        ImageView imageView = (ImageView) rootView.findViewById(R.id.image_view);
        if (submission.getPhoto(photoType) != null) {
            if (!submission.getPhotos().isEmpty()) {
                setImage(submission.getPhoto(photoType), imageView);
            } else {
                submission.addPhoto(null, photoType);
            }
        } else {
            imageView.setImageResource(placeholders.get(photoType));
        }
    }

    /**
     * Prepare to save the correct photo according to the screen we're on.
     * @param photoType - the type of photo being saved to
     * @require 0 <= photoType <= 5
     * @ensure correct photo is being saved to
     */
    private void preparePhotoSaving(int photoType) throws IOException {
        this.imageFile = File.createTempFile(photoNames.get(photoType), ".jpg",
                getContext().getExternalFilesDir(getString(R.string.file_provider_authority)));
        this.imageFile.deleteOnExit();
        CurrentSubmission.getInstance().addPhoto(imageFile.getAbsolutePath(), photoType);
    }
}