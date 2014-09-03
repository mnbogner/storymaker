package info.guardianproject.mrapp.server;

import java.security.GeneralSecurityException;

import info.guardianproject.cacheword.CacheWordActivityHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;
import info.guardianproject.mrapp.CacheWordActivity;
import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.StoryMakerApp;
import info.guardianproject.mrapp.media.MediaHelper;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class WebViewActivity extends SherlockActivity implements ICacheWordSubscriber {

	WebView mWebView;
	MediaHelper mMediaHelper;

    // NEW/CACHEWORD
    private CacheWordActivityHandler mCacheWordHandler;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // NEW/CACHEWORD
        mCacheWordHandler = new CacheWordActivityHandler(this, ((StoryMakerApp)getApplication()).getCacheWordSettings());

        setContentView(R.layout.activity_web_view);

        getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
        
        Intent intent = getIntent();
        if (intent != null)
        {
        	String title = intent.getStringExtra("title");
        	if (title != null)
        		setTitle(title);
        
        	String url = intent.getStringExtra("url");
        	mWebView = (WebView) findViewById(R.id.web_engine);  
        	
        	mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        	mWebView.getSettings().setJavaScriptEnabled(true);
        	mWebView.getSettings().setPluginState(PluginState.ON);
        	mWebView.getSettings().setAllowFileAccess(true);
        	mWebView.getSettings().setSupportZoom(false);
        
        	mWebView.setWebViewClient(new WebViewClient ()
        	{

        		
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {

					boolean isMedia = false;
					
					String mimeType = mMediaHelper.getMimeType(url);
					
					if (mimeType != null && (!mimeType.startsWith("text")))
							isMedia = true;
					
					if (isMedia)
					{
						//launch video player
						mMediaHelper.playMedia(Uri.parse(url), mimeType);
					}
					
					return isMedia;// super.shouldOverrideUrlLoading(view, url);
				}
        		
        	});
        	
        	mWebView.loadUrl(url); 
        
        }
        
        mMediaHelper = new MediaHelper(this, null);
        
    }
    
    
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       // getSupportMenuInflater().inflate(R.menu.activity_lesson_list, menu);
        return true;
    }
    

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        
        case android.R.id.home:
       
            finish();
            return true;

       
        }
        
        return super.onOptionsItemSelected(item);
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (mWebView.canGoBack())
			{
				mWebView.goBack();
				return true;
			}
			
				
		}
		
		return super.onKeyDown(keyCode, event);
	}



	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyUp(keyCode, event);
	}
    
    //NEW/CACHEWORD
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
        // cacheword initialized with default pin by base activity at startup
    }
    @Override
    public void onCacheWordLocked() {
        // if there has been no first lock and pin prompt, use default pin to unlock
        SharedPreferences sp = getSharedPreferences("appPrefs", Context.MODE_PRIVATE);
        String cachewordStatus = sp.getString("cacheword_status", "default");
        if (cachewordStatus.equals(getText(R.string.cacheword_state_unset).toString())) {
            try {
                CharSequence defaultPinSequence = getText(R.string.cacheword_default_pin);
                char[] defaultPin = defaultPinSequence.toString().toCharArray();
                mCacheWordHandler.setPassphrase(defaultPin);
                Log.d(this.getClass().getName(), "used default cacheword pin");
            } catch (GeneralSecurityException gse) {
                Log.e(this.getClass().getName(), "failed to use default cacheword pin: " + gse.getMessage());
            }
        } else {
            Log.d(this.getClass().getName(), "prompt for cacheword pin");
            showLockScreen();
        }
    }
    @Override
    public void onCacheWordOpened() {
        // ???
    }
    void showLockScreen() {
        // set aside current activity and prompt for cacheword pin
        Intent intent = new Intent(this, CacheWordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("originalIntent", getIntent());
        startActivity(intent);
        finish();
    }
}
