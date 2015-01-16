package org.storymaker.app.tests;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.google.android.apps.common.testing.ui.espresso.NoMatchingViewException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.storymaker.app.HomeActivity;
import org.storymaker.app.R;

import java.io.File;
import java.util.ArrayList;

import scal.io.liger.MainActivity;

import static android.test.ViewAsserts.assertOnScreen;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withParent;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withTagValue;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;

/**
 * Created by mnbogner on 11/18/14.
 */
public class LearningGuideTest extends ActivityInstrumentationTestCase2<HomeActivity> {

    private HomeActivity mHomeActivity;

    private Instrumentation.ActivityMonitor mVideoActivityMonitor;
    private Instrumentation.ActivityMonitor mAudioActivityMonitor;
    private Instrumentation.ActivityMonitor mPhotoActivityMonitor;
    private Instrumentation.ActivityMonitor mPassThroughMonitor;

    private String testDirectory;

    public LearningGuideTest() {
        super(HomeActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        mHomeActivity = getActivity();

        // create references to sample files for dummy responses
        // sample files assumed to be present (copied by test setup script)
        // NOTE: can these be refactored into uri's like "content://media/external/video/media/1258"
        String packageName = mHomeActivity.getApplicationContext().getPackageName();
        File root = Environment.getExternalStorageDirectory();
        testDirectory = root.toString() + "/Android/data/" + packageName + "/files/";
        String sampleVideo = testDirectory + "SAMPLE.mp4";
        String sampleAudio = testDirectory + "SAMPLE.mp3";
        String samplePhoto = testDirectory + "SAMPLE.jpg";

        // seems necessary to create activity monitor to prevent activity from being caught by other monitors
        mPassThroughMonitor = new Instrumentation.ActivityMonitor(MainActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor);

        // create activity monitors to intercept media capture requests
        IntentFilter videoFilter = new IntentFilter(MediaStore.ACTION_VIDEO_CAPTURE);
        Intent videoIntent = new Intent();
        Uri videoUri = Uri.parse(sampleVideo);
        videoIntent.setData(videoUri);
        Instrumentation.ActivityResult videoResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, videoIntent);
        mVideoActivityMonitor = new Instrumentation.ActivityMonitor(videoFilter, videoResult, true);
        getInstrumentation().addMonitor(mVideoActivityMonitor);

        IntentFilter photoFilter = new IntentFilter(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent photoIntent = new Intent();
        Uri photoUri = Uri.parse(samplePhoto);
        photoIntent.setData(photoUri);
        Instrumentation.ActivityResult photoResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, photoIntent);
        mPhotoActivityMonitor = new Instrumentation.ActivityMonitor(photoFilter, photoResult, true);
        getInstrumentation().addMonitor(mPhotoActivityMonitor);

        IntentFilter audioFilter = new IntentFilter(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        Intent audioIntent = new Intent();
        Uri audioUri = Uri.parse(sampleAudio);
        audioIntent.setData(audioUri);
        Instrumentation.ActivityResult audioResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, audioIntent);
        mAudioActivityMonitor = new Instrumentation.ActivityMonitor(audioFilter, audioResult, true);
        getInstrumentation().addMonitor(mAudioActivityMonitor);

        // clear out files from previous tests
        cleanup(testDirectory);
    }

    public void testVideo() {

        // obb file assumed to be present (copied by test setup script)

        // select learning guide
        stall(500, "SELECT GUIDE");
        onView(withText("Learning Guide 1")).perform(click());

        // select medium
        stall(500, "SELECT MEDIUM");
        swipe(6);
        onView(withText("Video")).perform(click());

        // continue
        stall(500, "CONTINUE");
        swipe(1);
        onView(withText("Start creating")).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 1");
        swipe(2);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_video_0")))))).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 2");
        swipe(2);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_video_1")))))).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 3");
        swipe(1);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_video_2")))))).perform(click());

        // continue
        stall(500, "CONTINUE");
        swipe(1);
        onView(withText("Order your clips")).perform(click());

        // finish
        stall(500, "FINISH");
        swipe(1);
        onView(withText("Finish")).perform(click());

        // check box
        stall(500, "CHECK BOX");
        swipe(1);
        onView(withText("Thumbs Up")).perform(click());

        // continue
        stall(500, "CONTINUE");
        swipe(1);
        onView(withText("Continue")).perform(click());

        try {
            stall(500, "NEXT");
            swipe(1);
            onView(withText("Next: Add More Detail to Your Story")).perform(click());
            Log.d("AUTOMATION", "FOUND NEXT BUTTON");
        } catch (NoMatchingViewException nmve) {
            // implies no button was found (failure)
            Log.d("AUTOMATION", "NO NEXT BUTTON FOUND");
            assertTrue(false);
        }

        Log.d("AUTOMATION", "testVideo() COMPLETE");
        assertTrue(true);
    }

    public void testPhoto() {

        // obb file assumed to be present (copied by test setup script)

        // select learning guide
        stall(500, "SELECT GUIDE");
        onView(withText("Learning Guide 1")).perform(click());

        // select medium
        stall(500, "SELECT MEDIUM");
        swipe(6);
        onView(withText("Photo")).perform(click());

        // continue
        stall(500, "CONTINUE");
        swipe(1);
        onView(withText("Start creating")).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 1");
        swipe(2);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_photo_0")))))).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 2");
        swipe(2);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_photo_1")))))).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 3");
        swipe(1);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_photo_2")))))).perform(click());

        // continue
        stall(500, "CONTINUE");
        swipe(1);
        onView(withText("Order your clips")).perform(click());

        // finish
        stall(500, "FINISH");
        swipe(1);
        onView(withText("Finish")).perform(click());

        // check box
        stall(500, "CHECK BOX");
        swipe(1);
        onView(withText("Thumbs Up")).perform(click());

        // continue
        stall(500, "CONTINUE");
        swipe(1);
        onView(withText("Continue")).perform(click());

        try {
            stall(500, "NEXT");
            swipe(1);
            onView(withText("Next: Add More Detail to Your Story")).perform(click());
            Log.d("AUTOMATION", "FOUND NEXT BUTTON");
        } catch (NoMatchingViewException nmve) {
            // implies no button was found (failure)
            Log.d("AUTOMATION", "NO NEXT BUTTON FOUND");
            assertTrue(false);
        }

        Log.d("AUTOMATION", "testPhoto() COMPLETE");
        assertTrue(true);
    }

    public void testAudio() {

        // obb file assumed to be present (copied by test setup script)

        // select learning guide
        stall(500, "SELECT GUIDE");
        onView(withText("Learning Guide 1")).perform(click());

        // select medium
        stall(500, "SELECT MEDIUM");
        swipe(6);
        onView(withText("Audio")).perform(click());

        // continue
        stall(500, "CONTINUE");
        swipe(1);
        onView(withText("Start creating")).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 1");
        swipe(2);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_audio_0")))))).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 2");
        swipe(2);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_audio_1")))))).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 3");
        swipe(1);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_audio_2")))))).perform(click());

        // continue
        stall(500, "CONTINUE");
        swipe(1);
        onView(withText("Order your clips")).perform(click());

        // finish
        stall(500, "FINISH");
        swipe(1);
        onView(withText("Finish")).perform(click());

        // check box
        stall(500, "CHECK BOX");
        swipe(1);
        onView(withText("Thumbs Up")).perform(click());

        // continue
        stall(500, "CONTINUE");
        swipe(1);
        onView(withText("Continue")).perform(click());

        try {
            stall(500, "NEXT");
            swipe(1);
            onView(withText("Next: Add More Detail to Your Story")).perform(click());
            Log.d("AUTOMATION", "FOUND NEXT BUTTON");
        } catch (NoMatchingViewException nmve) {
            // implies no button was found (failure)
            Log.d("AUTOMATION", "NO NEXT BUTTON FOUND");
            assertTrue(false);
        }

        Log.d("AUTOMATION", "testAudio() COMPLETE");
        assertTrue(true);
    }

    /*
    public void testMenu() {
        boolean errorStatus = true;

        stall(1000, "WAITING...");

        openMenu();
        stall(1000, "MENU WAS OPENED?");
        onView(withText("Home")).perform(click());
        stall(3000, "CLICKED HOME");

        openMenu();
        stall(1000, "MENU WAS OPENED?");
        onView(withText("Accounts")).perform(click());
        stall(3000, "CLICKED ACCOUNTS");

        openMenu();
        stall(1000, "MENU WAS OPENED?");
        onView(withText("Lessons")).perform(click());
        stall(3000, "CLICKED LESSONS");

        openMenu();
        stall(1000, "MENU WAS OPENED?");
        onView(withText("Exports")).perform(click());
        stall(3000, "CLICKED EXPORTS");

        openMenu();
        stall(1000, "MENU WAS OPENED?");
        onView(withText("Settings")).perform(click());
        stall(3000, "CLICKED SETTINGS");

        Log.d("AUTOMATION", "testMenu() COMPLETE");
    }

    public void openMenu() {
        mHomeActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mHomeActivity.toggleDrawer();
            }
        });
    }
    */

    private void stall(long milliseconds, String message) {
        try {
            Log.d("AUTOMATION", "SLEEP " + (milliseconds / 1000) + " (" + message + ")");
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void swipe(int swipes) {
        for (int i = 0; i < swipes; i++) {
            onView(withId(R.id.recyclerView)).perform(Util.swipeUpLess());
        }
    }

    private void cleanup(String directory) {
        WildcardFileFilter oldFileFilter = new WildcardFileFilter("*instance*");
        for (File oldFile : FileUtils.listFiles(new File(directory), oldFileFilter, null)) {
            Log.d("AUTOMATION", "CLEANUP: FOUND " + oldFile.getPath() + ", DELETING");
            FileUtils.deleteQuietly(oldFile);
        }
    }
}