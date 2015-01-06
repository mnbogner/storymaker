
package org.storymaker.app;

import org.storymaker.app.db.StoryMakerDB;
import org.storymaker.app.media.MediaProjectManager;
import org.storymaker.app.media.OverlayCameraActivity;
import org.storymaker.app.model.template.Clip;
import org.storymaker.app.model.template.Template;
import org.storymaker.app.model.Auth;
import org.storymaker.app.model.AuthTable;
import org.storymaker.app.model.JobTable;
import org.storymaker.app.model.Project;
import org.storymaker.app.model.ProjectTable;
import org.storymaker.app.model.PublishJobTable;
import org.storymaker.app.model.Scene;
import org.storymaker.app.server.ServerManager;
import io.scal.secureshareui.controller.ArchiveSiteController;
import io.scal.secureshareui.controller.SiteController;
import io.scal.secureshareui.lib.ChooseAccountFragment;
import io.scal.secureshareui.lib.ArchiveMetadataActivity;
import scal.io.liger.Constants;
import scal.io.liger.model.FullMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.micode.soundrecorder.SoundRecorder;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class SceneEditorActivity extends EditorBaseActivity implements ActionBar.TabListener {
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";


    protected Menu mMenu = null;

    //private String mTemplateJsonPath = null;
    private int mSceneIndex = 0;

    private final static String CAPTURE_MIMETYPE_AUDIO = "audio/3gpp";
    public Fragment mFragmentTab0, mFragmentTab1, mLastTabFrag;
    public PublishFragment mPublishFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

//        mTemplateJsonPath = getIntent().getStringExtra("template_path"); 
        //       mStoryMode = getIntent().getIntExtra("story_mode", Project.STORY_TYPE_VIDEO);

        int pid = intent.getIntExtra("pid", -1); //project id
        ArrayList<Parcelable> parcelables = intent.getParcelableArrayListExtra(Constants.EXTRA_EXPORT_CLIPS);

        mSceneIndex = getIntent().getIntExtra("scene", 0);

        if (parcelables != null) {
            // FIXME this should be split into a method, probably in the model.Project class?
            mProject = new Project(this, 1);
            mProject.setTitle("export from liger");
            mProject.setTemplatePath("");
            mProject.setStoryType(Project.STORY_TYPE_VIDEO);
            mProject.save();
            Scene scene = new Scene(this, parcelables.size());
            scene.setTitle("ligerscene1");
            scene.setProjectId(mProject.getId());
            scene.setProjectIndex(0);
            scene.save();

            // FIXME convert export into project
            int i = 0;
            for (Parcelable p: parcelables) {
                // index, cliptype, path, mimetype
                FullMetadata m = ((FullMetadata) p);
                scene.setMedia(i, m.getFilePath(), m.getFilePath(), "video/mp4");
                i++;
            }
            scene.save();
            // FIXME load project
            mMPM = new MediaProjectManager(this, getApplicationContext(), mHandlerPub, mProject, scene);
        } else if (pid != -1) {
            mProject = (Project)(new ProjectTable()).get(getApplicationContext(), pid); // FIXME ugly
            Scene scene = null;
            if ((mSceneIndex != -1) && (mSceneIndex < mProject.getScenesAsArray().length)) {
                scene = mProject.getScenesAsArray()[mSceneIndex];
            }
            mMPM = new MediaProjectManager(this, getApplicationContext(), mHandlerPub, mProject, scene);
            mMPM.initProject();
            mMPM.addAllProjectMediaToEditor();
        } else {
            int clipCount = 5; // FIXME get rid of hardcoded clipCount = 5

            String title = intent.getStringExtra("title");

            mProject = new Project(getApplicationContext(), clipCount);
            mProject.setTitle(title);
            mProject.save();
            mMPM = new MediaProjectManager(this, getApplicationContext(), mHandlerPub, mProject);
            mMPM.initProject();
        }

        try
        {
            if (mProject.getScenesAsList().size() > 1)
            {
                mTemplate = Template.parseAsset(this, mProject.getTemplatePath(),Project.getSimpleTemplateForMode(getApplicationContext(), mProject.getStoryType()));

            }
            else
            {
                mTemplate = Template.parseAsset(this, Project.getSimpleTemplateForMode(getApplicationContext(), mProject.getStoryType()));

            }
        }
        catch (Exception e)
        {
            Log.e(AppConstants.TAG,"could not parse templates",e);
        }
        setContentView(R.layout.activity_scene_editor_no_swipe);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);
//        if (mMPM.mScene != null) {
//            actionBar.setTitle(mMPM.mScene.getTitle());
//        }

        // For each of the sections in the app, add a tab to the action bar.
//        actionBar.addTab(actionBar.newTab().setText(R.string.tab_add_clips).setTabListener(this));
//        actionBar.addTab(actionBar.newTab().setText(R.string.tab_order).setTabListener(this));
//        if (mMPM.mProject.isTemplateStory()) {
//            actionBar.addTab(actionBar.newTab().setText(R.string.tab_finish).setTabListener(this));
//        } else {
//            actionBar.addTab(actionBar.newTab().setText(R.string.tab_publish).setTabListener(this));
//        }
        addPublishFragement();
        actionBar.setTitle(getString(R.string.tab_publish));


        if (intent.hasExtra("auto_capture")
                && intent.getBooleanExtra("auto_capture", false))
        {
            openCaptureMode(0, 0);

        }

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.activity_scene_editor, menu);
        mMenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addNewShot:
                addShotToScene();

                return true;
            case R.id.exportProjectFiles:
                exportProjectFiles();

                return true;
            case R.id.itemInfo:
                Intent intent = new Intent(this, StoryInfoActivity.class);
                intent.putExtra("pid", mProject.getId());
                startActivity(intent);

                return true;
            case R.id.itemTrim:
                if (mFragmentTab1 != null) {
                    if(((OrderClipsFragment) mFragmentTab1).loadTrim()) {
                        ((OrderClipsFragment) mFragmentTab1).enableTrimUIMode();
                        startActionMode(mActionModeCallback);
                    }

                    return true;
                }
                return true;
            case R.id.purgePublishTables:
                net.sqlcipher.database.SQLiteDatabase db = new StoryMakerDB(getBaseContext()).getWritableDatabase("foo");
                JobTable foo;
                (new PublishJobTable(db)).debugPurgeTable();
                (new JobTable(db)).debugPurgeTable();
                db.close();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private boolean actionModelCancel = false;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu_trim, menu);
            actionModelCancel = false;
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_cancel:
                    actionModelCancel = true;
                    mode.finish();
                    return true;
                case R.id.menu_trim_clip:
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        // this has slightly odd save logic so that I can always save exit actionmode as 
        // the checkmark button acts as a cancel but the users will treat it as an accept
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (actionModelCancel) {
                ((OrderClipsFragment) mFragmentTab1).undoSaveTrim();
            } else {
                ((OrderClipsFragment) mFragmentTab1).saveTrim();
            }

            ((OrderClipsFragment) mFragmentTab1).enableTrimUIMode();
        }
    };

    // FIXME move this into AddClipsFragment?
    public void addShotToScene ()
    {
        try
        {
            Clip tClip = new Clip();
            tClip.setDefaults();
            AddClipsFragment acf = ((AddClipsFragment)mFragmentTab0);
            acf.addTemplateClip(tClip);
        }
        catch (Exception e)
        {
            Log.e(AppConstants.TAG,"error adding new clip",e);
        }
    }

    public void deleteCurrentShot ()
    {
        mMPM.deleteCurrentClip();

    }

    private void exportProjectFiles()
    {
        try
        {
            File fileProjectSrc = MediaProjectManager.getExternalProjectFolder(mMPM.mProject, mMPM.getContext());
            ArrayList<File> fileList= new ArrayList<File>();
            String mZipFileName = buildZipFilePath(fileProjectSrc.getAbsolutePath());

            //if not enough space
            if(!mMPM.checkStorageSpace())
            {
                return;
            }

            String[] mMediaPaths = mMPM.mProject.getMediaAsPathArray();

            //add videos
            for (String path : mMediaPaths)
            {
                fileList.add(new File(path));
            }

            //add thumbnails
            fileList.addAll(Arrays.asList(fileProjectSrc.listFiles()));

            //add database file
            fileList.add(getDatabasePath("sm.db"));

            FileOutputStream fos = new FileOutputStream(mZipFileName);
            ZipOutputStream zos = new ZipOutputStream(fos);

            exportProjectFiles(zos, fileList.toArray( new File[fileList.size()]));

            zos.close();

            onExportProjectSuccess(mZipFileName);
        }
        catch (IOException ioe)
        {
            Log.e(AppConstants.TAG, "Error creating zip file:", ioe);
        }
    }


    private void exportProjectFiles(ZipOutputStream zos, File[] fileList)
    {
        final int BUFFER = 2048;

        for (int i = 0; i < fileList.length; i++)
        {
            try
            {
                byte[] data = new byte[BUFFER];

                FileInputStream fis = new FileInputStream(fileList[i]);
                zos.putNextEntry(new ZipEntry(fileList[i].getName()));

                int count;
                while ((count = fis.read(data, 0, BUFFER)) != -1)
                {
                    zos.write(data, 0, count);
                }

                //close steams
                zos.closeEntry();
                fis.close();

            }
            catch (IOException ioe)
            {
                Log.e(AppConstants.TAG, "Error creating zip file:", ioe);
            }
        }
    }

    private void onExportProjectSuccess(final String zipFileName)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.export_dialog_title);
        dialogBuilder.setPositiveButton(R.string.export_dialog_share, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(zipFileName)));
                shareIntent.setType("*/*");
                startActivity(shareIntent);
            }
        });
        dialogBuilder.setNegativeButton(R.string.export_dialog_close,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        dialogBuilder.show();
    }

    private String buildZipFilePath(String filePath)
    {
        //create datestamp
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

        int index = filePath.lastIndexOf('/');
        filePath = filePath.substring(0, index + 1);

        return String.format("%sstorymaker_project_%s_%s.zip", filePath, mMPM.mProject.getId(), dateFormat.format(date));
    }

    void addMediaFromGallery()
    {
        mMPM.mMediaHelper.openGalleryChooser("*/*");
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().hide(mLastTabFrag).commit();
    }

    // protected void setupAddClipsFragment() {
    // FragmentManager fm = getSupportFragmentManager();
    //
    // try {
    // mFragmentTab0 = new SceneChooserFragment(R.layout.fragment_add_clips, fm,
    // mTemplateJsonPath);
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (JSONException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, show the tab contents in the
        // container
        int layout = R.layout.fragment_add_clips;
        FragmentManager fm = getSupportFragmentManager();

        if (mMenu != null) {
            mMenu.findItem(R.id.itemTrim).setVisible(false);
        }

        if(mLastTabFrag instanceof OrderClipsFragment)
        {
            ((OrderClipsFragment) mLastTabFrag).stopPlaybackOnTabChange();
        }

        addPublishFragement();

        //Make Tab
//        if (tab.getPosition() == 0) {
//            if (mMenu != null) {
//                //show relevant menu items
//                setMenuItemsVisibility(true);
//            }
//            layout = R.layout.fragment_add_clips;
//
//            if (mFragmentTab0 == null)
//            {
//
//                //    mFragmentTab0 = new AddClipsFragment(layout, fm, mTemplate, mSceneIndex, this);
//                mFragmentTab0 = new AddClipsFragment();
//
//                Bundle args = new Bundle();
//                args.putInt(AddClipsFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
//                args.putInt("layout", layout);
//                args.putInt("scene",mSceneIndex);
//                mFragmentTab0.setArguments(args);
//
//                fm.beginTransaction()
//                        .add(R.id.container, mFragmentTab0, layout + "")
//                        .commit();
//
//            } else {
//                fm.beginTransaction()
//                        .show(mFragmentTab0)
//                        .commit();
//            }
//            mLastTabFrag = mFragmentTab0;
//            //Edit Tab
//        } else if (tab.getPosition() == 1) {
//            layout = R.layout.fragment_order_clips;
//
//            if (mMenu != null) {
//                //hide irrelevant menu items
//                setMenuItemsVisibility(false);
//
//                //if only photos, no need to display trim option
//                if(!(mMPM.mProject.getStoryType() == Project.STORY_TYPE_ESSAY || mMPM.mProject.getStoryType() == Project.STORY_TYPE_PHOTO))
//                {
//                    mMenu.findItem(R.id.itemTrim).setVisible(true);
//                }
//            }
//
//            if (mFragmentTab1 == null)
//            {
//
//                mFragmentTab1 = new OrderClipsFragment();
//
//                Bundle args = new Bundle();
//                args.putInt(OrderClipsFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
//                args.putInt("layout", layout);
//                mFragmentTab1.setArguments(args);
//
//
//                fm.beginTransaction()
//                        .add(R.id.container, mFragmentTab1, layout + "")
//                        .commit();
//
//            } else {
//
//                ((OrderClipsFragment)mFragmentTab1).loadMedia();
//
//                fm.beginTransaction()
//                        .show(mFragmentTab1)
//                        .commit();
//            }
//
//            mLastTabFrag = mFragmentTab1;
//            //Publish Tab
//        } else if (tab.getPosition() == 2) {

//        }
    }

    private void addPublishFragement() {
        FragmentManager fm = getSupportFragmentManager();

        if (mMenu != null) {
            //hide irrelevant menu items
            setMenuItemsVisibility(false);
        }

        if (mMPM.mProject.isTemplateStory()) {
            Intent intent = new Intent(getBaseContext(), StoryTemplateActivity.class);
            intent.putExtra("template_path", mProject.getTemplatePath());
            intent.putExtra("story_mode", mMPM.mProject.getStoryType());
            intent.putExtra("pid", mMPM.mProject.getId());
            intent.putExtra("title", mMPM.mProject.getTitle());
            startActivity(intent);
            finish();
        } else {
            int layout = R.layout.fragment_complete_story;

            if (mPublishFragment == null) {
                mPublishFragment = new PublishFragment();
                Bundle args = new Bundle();
                args.putInt("layout",layout);
                mPublishFragment.setArguments(args);


                fm.beginTransaction()
                        .add(R.id.container, mPublishFragment, layout + "")
                        .commit();

            } else {
                fm.beginTransaction().show(mPublishFragment).commit();
            }

            mLastTabFrag = mPublishFragment;
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    private void setMenuItemsVisibility(boolean show) {
        //hide irrelevant menu items
        mMenu.findItem(R.id.addNewShot).setVisible(show);
    }

    public void refreshClipPager() {
        if (mFragmentTab0 != null) {
            try {
                ((AddClipsFragment) mFragmentTab0).reloadClips();
            }
            catch (Exception e) {
                Log.e(AppConstants.TAG, "error reloading clips", e);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void openCaptureMode (int shotType, int clipIndex)
    {

        if (mProject.getStoryType() == Project.STORY_TYPE_AUDIO)
        {
            Intent i = new Intent(getApplicationContext(), SoundRecorder.class);
            i.putExtra("dir", mMPM.getExternalProjectFolder(mProject, getBaseContext()));
            i.setType(CAPTURE_MIMETYPE_AUDIO);
            i.putExtra("mode", mProject.getStoryType());
            mMPM.mClipIndex = clipIndex;
            startActivityForResult(i,mProject.getStoryType());

        }
        else
        {
            Intent i = new Intent(getApplicationContext(), OverlayCameraActivity.class);
            i.putExtra("group", shotType);
            i.putExtra("mode", mProject.getStoryType());
            mMPM.mClipIndex = clipIndex;
            startActivityForResult(i, REQ_OVERLAY_CAM);
        }
    }

    private File mCapturePath;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQ_OVERLAY_CAM) {
                File fileMediaFolder = mMPM.getExternalProjectFolder(mProject, getBaseContext());

                if (mProject.getStoryType() == Project.STORY_TYPE_VIDEO) {
                    mCapturePath = mMPM.mMediaHelper.captureVideo(fileMediaFolder);
                } else if (mProject.getStoryType() == Project.STORY_TYPE_PHOTO) {
                    mCapturePath = mMPM.mMediaHelper.capturePhoto(fileMediaFolder);
                } else if (mProject.getStoryType() == Project.STORY_TYPE_ESSAY) {
                    mCapturePath = mMPM.mMediaHelper.capturePhoto(fileMediaFolder);
                }
//            } else if (requestCode == REQ_YOUTUBE_AUTH) {
//                if (resultCode == RESULT_OK) {
//                    String oauthToken = intent.getStringExtra("token");
//                    Log.d("OAuth", "got token: " + oauthToken);
//                    mPublishFragment.setYouTubeAuth(oauthToken);
//                }
            } else if (requestCode == ChooseAccountFragment.ACCOUNT_REQUEST_CODE) { // FIXME hard wireing archive.org in for now, baad
//                mPublishFragment.onChooseAccountDialogResult(resultCode, intent);
                // TODO if site is archive, do this
                ArrayList<String> siteKeys = intent.getExtras().getStringArrayList(ChooseAccountFragment.EXTRAS_ACCOUNT_KEYS);
                if (siteKeys.contains(ArchiveSiteController.SITE_KEY)) {
                    Intent i = new Intent(getBaseContext(), ArchiveMetadataActivity.class);
                    Bundle extras = intent.getExtras();
                    ServerManager serverManager = ((StoryMakerApp) this.getApplication()).getServerManager();
                    if (serverManager.hasCreds()) {
                        extras.putString(SiteController.VALUE_KEY_AUTHOR, serverManager.getUserName());
                    }
                    extras.putString(SiteController.VALUE_KEY_BODY, mMPM.mProject.getDescription());
                    extras.putString(SiteController.VALUE_KEY_LOCATION_NAME, mMPM.mProject.getLocation());
                    extras.putString(SiteController.VALUE_KEY_TAGS, mMPM.mProject.getTagsAsString());
                    extras.putString(SiteController.VALUE_KEY_TITLE, mMPM.mProject.getTitle());
                    i.putExtras(extras);
                    startActivityForResult(i, ArchiveSiteController.METADATA_REQUEST_CODE);
                } else {
                    mPublishFragment.onChooseAccountDialogResult(resultCode, intent);
                }
            } else if (requestCode == ArchiveSiteController.METADATA_REQUEST_CODE) {
                mPublishFragment.onChooseAccountDialogResult(resultCode, intent);
            } else {
                try {
                    mMPM.handleResponse(intent, mCapturePath);
                    refreshClipPager();
                } catch (IOException e) {
                    Log.e(AppConstants.TAG, "error handling capture response: " + mCapturePath, e);
                }
            }
        } else {
            if (requestCode == ChooseAccountFragment.ACCOUNT_REQUEST_CODE) {
                mPublishFragment.onChooseAccountDialogResult(resultCode, intent);
            }
        }
    }
}
