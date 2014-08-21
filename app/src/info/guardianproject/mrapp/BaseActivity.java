package info.guardianproject.mrapp;

import info.guardianproject.mrapp.server.LoginActivity;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Date;

import org.holoeverywhere.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.view.WindowManager;
import android.widget.ImageView;

//import com.google.analytics.tracking.android.EasyTracker;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.OnClosedListener;

// NEW/CACHEWORD
import info.guardianproject.cacheword.CacheWordActivityHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;

public class BaseActivity extends Activity implements ICacheWordSubscriber {

	public SlidingMenu mSlidingMenu;
		
	// NEW/CACHEWORD
    private CacheWordActivityHandler mCacheWordHandler;

	@Override
	public void onStart() {
		super.onStart();
//		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
//		EasyTracker.getInstance(this).activityStop(this);
	}
	
	// NEW/CACHEWORD
    @Override
    protected void onPause() {
        super.onPause();
        mCacheWordHandler.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mCacheWordHandler.onResume();
    }
    @Override
    public void onCacheWordUninitialized() {
        // showLockScreen();   
        // SET PIN BY DEFAULT, CLEAR AND PROMPT ON FIRST LOCK
        try {
            mCacheWordHandler.setPassphrase("1111".toCharArray());
        } catch (GeneralSecurityException e) {
            // TODO implement try again and wipe if fail
            Log.e("CACHEWORD", "FAILED TO SET DEFAULT PIN: " + e.getMessage());
        }
    }
    @Override
    public void onCacheWordLocked() {
        showLockScreen();
        // lock db? -> provider classes need to implement ICacheWordSubscriber and handle db
    }
    @Override
    public void onCacheWordOpened() {
        // unlock db? -> provider classes need to implement ICacheWordSubscriber and handle db
    }
    
    // NEW/CACHEWORD
    void showLockScreen() {
        Intent intent = new Intent(this, CacheWordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("originalIntent", getIntent());
        startActivity(intent);
        finish();
    }

    public void initSlidingMenu ()
    {

        mSlidingMenu = new SlidingMenu(this);
        mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        mSlidingMenu.setShadowDrawable(R.drawable.shadow);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        
        mSlidingMenu.setBehindWidthRes(R.dimen.slidingmenu_offset);
        mSlidingMenu.setFadeDegree(0.35f);
        mSlidingMenu.setMenu(R.layout.fragment_drawer);

		mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		
        mSlidingMenu.setOnClosedListener(new OnClosedListener() {

            @Override
            public void onClosed() {
                mSlidingMenu.requestLayout();

            }
        });
        
        final Activity activity = this;
        
        ImageButton btnDrawerQuickCaptureVideo = (ImageButton) findViewById(R.id.btnDrawerQuickCaptureVideo);
        ImageButton btnDrawerQuickCapturePhoto = (ImageButton) findViewById(R.id.btnDrawerQuickCapturePhoto);
        ImageButton btnDrawerQuickCaptureAudio = (ImageButton) findViewById(R.id.btnDrawerQuickCaptureAudio);
        
        Button btnDrawerHome = (Button) findViewById(R.id.btnDrawerHome);
        Button btnDrawerProjects = (Button) findViewById(R.id.btnDrawerProjects);
        Button btnDrawerLessons = (Button) findViewById(R.id.btnDrawerLessons);
        Button btnDrawerAccount = (Button) findViewById(R.id.btnDrawerAccount);
        Button btnDrawerSettings = (Button) findViewById(R.id.btnDrawerSettings);

        // NEW/CACHEWORD
        Button btnDrawerLock = (Button) findViewById(R.id.btnDrawerLock);

        btnDrawerQuickCaptureVideo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	String dateNowStr = new Date().toLocaleString();
                
            	Intent intent = new Intent(BaseActivity.this, StoryNewActivity.class);
            	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	intent.putExtra("story_name", "Quick Story " + dateNowStr);
            	intent.putExtra("story_type", 0);
            	intent.putExtra("auto_capture", true);
                
                 activity.startActivity(intent);           
                 }
        });
        
        btnDrawerQuickCapturePhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	String dateNowStr = new Date().toLocaleString();
                
            	Intent intent = new Intent(BaseActivity.this, StoryNewActivity.class);
            	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	intent.putExtra("story_name", "Quick Story " + dateNowStr);
            	intent.putExtra("story_type", 2);
            	intent.putExtra("auto_capture", true);
                
                 activity.startActivity(intent);           
                 }
        });
        
        btnDrawerQuickCaptureAudio.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	String dateNowStr = new Date().toLocaleString();
                
            	Intent intent = new Intent(BaseActivity.this, StoryNewActivity.class);
            	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	intent.putExtra("story_name", "Quick Story " + dateNowStr);
            	intent.putExtra("story_type", 1);
            	intent.putExtra("auto_capture", true);
                
                 activity.startActivity(intent);           
                 }
        });
        
        btnDrawerHome.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	mSlidingMenu.showContent(true);
                
            	 Intent i = new Intent(activity, HomeActivity.class);
                 activity.startActivity(i);
            }
        });
        btnDrawerProjects.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            	mSlidingMenu.showContent(true);
            	  Intent i = new Intent(activity, ProjectsActivity.class);
                  activity.startActivity(i);
            }
        });
        btnDrawerLessons.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            	mSlidingMenu.showContent(true);
            	
                Intent i = new Intent(activity, LessonsActivity.class);
                activity.startActivity(i);
            }
        });
        
        btnDrawerAccount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            	mSlidingMenu.showContent(true);
                Intent i = new Intent(activity, LoginActivity.class);
                activity.startActivity(i);
            }
        });
        
        btnDrawerSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            	mSlidingMenu.showContent(true);
                Intent i = new Intent(activity, SimplePreferences.class);
                activity.startActivity(i);
            }
        });
        
        // NEW/CACHEWORD
        btnDrawerLock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCacheWordHandler.manuallyLock();
            }
        }); 
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    
    	super.onCreate(savedInstanceState);
        
        (new Eula(this)).show();
        
        // NEW/CACHEWORD
        mCacheWordHandler = new CacheWordActivityHandler(this, ((StoryMakerApp)getApplication()).getCacheWordSettings());
    }
    
    @Override
	public void onPostCreate(Bundle savedInstanceState) {
		
		super.onPostCreate(savedInstanceState);
	

        initSlidingMenu();
	}



	private void detectCoachOverlay ()
    {
        try {
        	
        	if (this.getClass().getName().contains("SceneEditorActivity"))
        	{
        		showCoachOverlay("images/coach/coach_add.png");
        	}
        	else if (this.getClass().getName().contains("OverlayCameraActivity"))
        	{
        		showCoachOverlay("images/coach/coach_camera_prep.png");
        	}
        		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * 
	public void switchContent(final Fragment fragment) {
		mContent = fragment;
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, fragment)
		.commit();
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			public void run() {
				getSlidingMenu().showContent();
			}
		}, 50);
	}	
**/
    
    private void showCoachOverlay (String path) throws IOException
    {
    	ImageView overlayView = new ImageView(this);
    	
    	overlayView.setOnClickListener(new OnClickListener () 
    	{

			@Override
			public void onClick(View v) {
				getWindowManager().removeView(v);
				
			}
    		
    	});
    	
    	AssetManager mngr = getAssets();
        // Create an input stream to read from the asset folder
           InputStream ins = mngr.open(path);

           // Convert the input stream into a bitmap
           Bitmap bmpCoach = BitmapFactory.decodeStream(ins);
           overlayView.setImageBitmap(bmpCoach);
           
    	WindowManager.LayoutParams params = new WindowManager.LayoutParams(
    	        WindowManager.LayoutParams.MATCH_PARENT,
    	        WindowManager.LayoutParams.MATCH_PARENT,
    	        WindowManager.LayoutParams.TYPE_APPLICATION,
    	        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
    	        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
    	        PixelFormat.TRANSLUCENT);

    	getWindowManager().addView(overlayView, params);
    }
}
