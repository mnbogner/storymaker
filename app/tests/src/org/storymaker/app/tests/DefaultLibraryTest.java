package org.storymaker.app.tests;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.google.android.apps.common.testing.ui.espresso.NoMatchingViewException;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.storymaker.app.AccountsActivity;
import org.storymaker.app.HomeActivity;
import org.storymaker.app.R;
import org.storymaker.app.StoryInfoEditActivity;

import java.io.File;
import java.util.ArrayList;

import scal.io.liger.MainActivity;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withParent;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withTagValue;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

/**
 * Created by mnbogner on 1/15/15.
 */
public class DefaultLibraryTest extends ActivityInstrumentationTestCase2<HomeActivity> {

    private HomeActivity mHomeActivity;

    private Instrumentation.ActivityMonitor mVideoActivityMonitor;
    private Instrumentation.ActivityMonitor mAudioActivityMonitor;
    private Instrumentation.ActivityMonitor mPhotoActivityMonitor;
    private Instrumentation.ActivityMonitor mPassThroughMonitor1;
    private Instrumentation.ActivityMonitor mPassThroughMonitor2;
    private Instrumentation.ActivityMonitor mPassThroughMonitor3;

    private String testDirectory;

    public DefaultLibraryTest() {
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
        mPassThroughMonitor1 = new Instrumentation.ActivityMonitor(MainActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor1);

        mPassThroughMonitor2 = new Instrumentation.ActivityMonitor(AccountsActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor2);

        mPassThroughMonitor3 = new Instrumentation.ActivityMonitor(StoryInfoEditActivity.class.getCanonicalName(), null, false);
        getInstrumentation().addMonitor(mPassThroughMonitor3);

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

        // select "new" option
        stall(500, "SELECT NEW");
        onView(withText("New")).perform(click());

        // first selection
        stall(500, "FIRST SELECTION (" + "An Event" + ")");
        onView(withText("An Event")).perform(click());

        // second selection
        stall(500, "SECOND SELECTION (" + "Show the best moments." + ")");
        onView(withText("Show the best moments.")).perform(click());

        // third selection
        stall(500, "THIRD SELECTION (" + "Video" + ")");
        onView(withText("Video")).perform(click());

        // media capture
        stall(500, "MEDIA CAPTURE 1");
        swipe(1);
        stall(500, "WAIT FOR UPDATE");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_card_0")))))).perform(click());

        // scroll to bottom, check for publish button
        stall(500, "SWIPING");
        swipe(15);

        // begin publish/upload steps
        try {
            stall(500, "PUBLISH BUTTON FOUND");
            onView(allOf(withText("Publish"), withParent(withTagValue(is((Object) "publish_card_1"))))).check(matches(isDisplayed()));
            Log.d("AUTOMATION", "FOUND PUBLISH BUTTON");
            stall(500, "PUBLISH BUTTON CLICKED");
            onView(allOf(withText("Publish"), withParent(withTagValue(is((Object) "publish_card_1"))))).perform(click());
            Log.d("AUTOMATION", "CLICKED PUBLISH BUTTON");
        } catch (NoMatchingViewException nmve) {
            // implies no button was found (failure)
            Log.d("AUTOMATION", "NO PUBLISH BUTTON FOUND");
            assertTrue(false);
        }

        // enter metadata
        stall(500, "METADATA");
        onView(withId(R.id.fl_info_container)).perform(click());
        Log.d("AUTOMATION", "CLICKED DESCRIPTION FIELD");
        stall(500, "TITLE");
        onView(withId(R.id.et_story_info_title)).perform(clearText()).perform(typeText("TEST TITLE"));
        Log.d("AUTOMATION", "ENTERED TITLE TEXT");
        stall(500, "DESCRIPTION");
        onView(withId(R.id.et_story_info_description)).perform(clearText()).perform(typeText("TEST DESCRIPTION"));
        Log.d("AUTOMATION", "ENTERED DESCRIPTION TEXT");
        stall(500, "FIRST TAG");
        onView(withId(R.id.act_story_info_tag)).perform(clearText()).perform(typeText("TAG1"));
        stall(500, "FIRST ADD");
        onView(withId(R.id.btn_add_tag)).perform(click());
        Log.d("AUTOMATION", "ENTERED FIRST TAG");
        stall(500, "SECOND TAG");
        onView(withId(R.id.act_story_info_tag)).perform(clearText()).perform(typeText("TAG2"));
        stall(500, "SECOND ADD");
        onView(withId(R.id.btn_add_tag)).perform(click());
        Log.d("AUTOMATION", "ENTERED SECOND TAG");
        stall(500, "SECTION LIST");
        onView(withId(R.id.sp_story_section)).perform(click());
        stall(500, "SECTION ITEM");
        onView(withText("Travel")).perform(click());
        Log.d("AUTOMATION", "SELECTED SECTION");
        stall(500, "LOCATION LIST");
        onView(withId(R.id.sp_story_location)).perform(click());
        stall(500, "LOCATION ITEM");
        onView(withText("Czech Republic")).perform(click());
        Log.d("AUTOMATION", "SELECTED LOCATION");
        stall(500, "SAVE");
        onView(withText("Save")).perform(click());
        Log.d("AUTOMATION", "SAVED INFO");



        // select account and upload
        stall(500, "UPLOAD BUTTON");
        onView(withId(R.id.btnUpload)).perform(click());
        Log.d("AUTOMATION", "CLICKED UPLOAD BUTTON");
        stall(500, "ACCOUNT BUTTON");
        onView(withText("Internet Archive")).perform(click());
        Log.d("AUTOMATION", "CLICKED ACCOUNT BUTTON");
        stall(500, "CONTINUE BUTTON");
        onView(withText("Continue")).perform(click());
        Log.d("AUTOMATION", "CLICKED CONTINUE BUTTON");



        // test complete
        Log.d("AUTOMATION", "testVideo() COMPLETE");
        assertTrue(true);
    }

    /*
    public void testPhoto() {
        // select "new" option
        stall(500, "SELECT NEW");
        onView(withText("New")).perform(click());

        // first selection
        stall(500, "FIRST SELECTION (" + "An Event" + ")");
        onView(withText("An Event")).perform(click());

        // second selection
        stall(500, "SECOND SELECTION (" + "Show the best moments." + ")");
        onView(withText("Show the best moments.")).perform(click());

        // third selection
        stall(500, "THIRD SELECTION (" + "Photo" + ")");
        onView(withText("Photo")).perform(click());

        // scroll to bottom, check for publish button
        stall(500, "SWIPING");
        swipe(15);

        try {
            stall(500, "PUBLISH BUTTON");
            onView(allOf(withText("Publish"), withParent(withTagValue(is((Object) "publish_card_1"))))).check(matches(isDisplayed()));
            Log.d("AUTOMATION", "FOUND PUBLISH BUTTON");
        } catch (NoMatchingViewException nmve) {
            // implies no button was found (failure)
            Log.d("AUTOMATION", "NO PUBLISH BUTTON FOUND");
            assertTrue(false);
        }

        Log.d("AUTOMATION", "testVideo() COMPLETE");
        assertTrue(true);
    }

    public void testAudio() {
        // select "new" option
        stall(500, "SELECT NEW");
        onView(withText("New")).perform(click());

        // first selection
        stall(500, "FIRST SELECTION (" + "An Event" + ")");
        onView(withText("An Event")).perform(click());

        // second selection
        stall(500, "SECOND SELECTION (" + "Show the best moments." + ")");
        onView(withText("Show the best moments.")).perform(click());

        // third selection
        stall(500, "THIRD SELECTION (" + "Audio" + ")");
        onView(withText("Audio")).perform(click());

        // scroll to bottom, check for publish button
        stall(500, "SWIPING");
        swipe(15);

        try {
            stall(500, "PUBLISH BUTTON");
            onView(allOf(withText("Publish"), withParent(withTagValue(is((Object) "publish_card_1"))))).check(matches(isDisplayed()));
            Log.d("AUTOMATION", "FOUND PUBLISH BUTTON");
        } catch (NoMatchingViewException nmve) {
            // implies no button was found (failure)
            Log.d("AUTOMATION", "NO PUBLISH BUTTON FOUND");
            assertTrue(false);
        }

        Log.d("AUTOMATION", "testVideo() COMPLETE");
        assertTrue(true);
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