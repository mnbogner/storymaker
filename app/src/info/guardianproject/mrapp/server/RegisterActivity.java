package info.guardianproject.mrapp.server;

import java.security.GeneralSecurityException;

import info.guardianproject.cacheword.CacheWordActivityHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;
import info.guardianproject.mrapp.CacheWordActivity;
import info.guardianproject.mrapp.R;
import info.guardianproject.mrapp.StoryMakerApp;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
 
public class RegisterActivity extends Activity implements ICacheWordSubscriber {
    
    // NEW/CACHEWORD
    private CacheWordActivityHandler mCacheWordHandler;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // NEW/CACHEWORD
        mCacheWordHandler = new CacheWordActivityHandler(this, ((StoryMakerApp)getApplication()).getCacheWordSettings());
        
        // Set View to register.xml
        setContentView(R.layout.activity_registration);
 
        TextView loginScreen = (TextView) findViewById(R.id.link_to_login);
 
        // Listening to Login Screen link
        loginScreen.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View arg0) {
                                // Closing registration screen
                // Switching to Login Screen/closing register screen
                finish();
            }
        });
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