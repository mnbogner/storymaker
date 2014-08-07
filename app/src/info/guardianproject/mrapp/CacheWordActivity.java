package info.guardianproject.mrapp;

import java.security.GeneralSecurityException;

import info.guardianproject.cacheword.CacheWordActivityHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class CacheWordActivity extends Activity implements ICacheWordSubscriber {

    private EditText mTextEnterPin;
    private EditText mTextCreatePin;
    private EditText mTextConfirmPin;
    private View mViewEnterPin;
    private View mViewCreatePin;
    private TwoViewSlider mSlider;
    private Button mButton;
    
    private CacheWordActivityHandler mCacheWordHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);
        
        mCacheWordHandler = new CacheWordActivityHandler(this, ((StoryMakerApp)getApplication()).getCacheWordSettings());    

        mViewEnterPin = findViewById(R.id.llEnterPin);
        mViewCreatePin = findViewById(R.id.llCreatePin);
        
        mTextEnterPin = (EditText) findViewById(R.id.editEnterPin);
        mTextCreatePin = (EditText) findViewById(R.id.editCreatePin);
        mTextConfirmPin = (EditText) findViewById(R.id.editConfirmPin);
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper);
        LinearLayout flipView1 = (LinearLayout) findViewById(R.id.flipView1);
        LinearLayout flipView2 = (LinearLayout) findViewById(R.id.flipView2);
        
        mSlider = new TwoViewSlider(vf, flipView1, flipView2, mTextCreatePin, mTextConfirmPin);
    }
    
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
        createPassphrase();  
    }

    @Override
    public void onCacheWordLocked() {
        requestPassphrase();
    }

    @Override
    public void onCacheWordOpened() {
        Intent intent = (Intent) getIntent().getParcelableExtra("originalIntent");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }
    
    private void createPassphrase() {
        
        mViewCreatePin.setVisibility(View.VISIBLE);
        mViewEnterPin.setVisibility(View.GONE);
        
        mTextCreatePin.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!isPasswordValid())
                        showValidationError();
                    else
                        mSlider.showConfirmationField();
                }
                return false;
            }
        });
        
        mTextConfirmPin.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!newEqualsConfirmation()) {
                        showInequalityError();
                        mSlider.showNewPasswordField();
                    }
                }
                return false;
            }
        });

        Button btnCreate = (Button) findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // validate pin
                if (!isPasswordValid()) {
                    showValidationError();
                    mSlider.showNewPasswordField();
                } else if (isConfirmationFieldEmpty()) {
                    mSlider.showConfirmationField();
                } else if (!newEqualsConfirmation()) {
                    showInequalityError();
                    mSlider.showNewPasswordField();
                } else {
                    try {
                        mCacheWordHandler.setPassphrase(mTextCreatePin.getText().toString().toCharArray());
                    } catch (GeneralSecurityException e) {
                        // TODO initialization failed
                        Log.e("TEST", "PASSWORD CREATION FAILED: " + e.getMessage());
                    }
                }
            }
        }); 
    }
    
    private boolean isPasswordValid() {
        if (mTextCreatePin.getText().toString().toCharArray().length < 4) 
            return false;
        else 
            return true;
    }
    
    private boolean newEqualsConfirmation() {
        return mTextCreatePin.getText().toString().equals(mTextConfirmPin.getText().toString());
    }
    
    private void showValidationError() {
        Toast.makeText(CacheWordActivity.this, "PIN must be 4 or more characters", Toast.LENGTH_LONG).show();
        mTextCreatePin.requestFocus();
    }
    
    private void showInequalityError() {
        Toast.makeText(CacheWordActivity.this, "PIN confirmation must match", Toast.LENGTH_LONG).show();
        clearNewFields();
    }
    
    private void clearNewFields() {
        mTextCreatePin.getEditableText().clear();
        mTextConfirmPin.getEditableText().clear();
    }
    
    private boolean isConfirmationFieldEmpty() {
        return mTextConfirmPin.getText().toString().length() == 0;
    }
    
    private void requestPassphrase() {
        mViewCreatePin.setVisibility(View.GONE);
        mViewEnterPin.setVisibility(View.VISIBLE);

        mButton = (Button) findViewById(R.id.btnOpen);
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTextEnterPin.getText().toString().length() == 0)
                    return;
                // Check passphrase
                try {
                    mCacheWordHandler.setPassphrase(mTextEnterPin.getText().toString().toCharArray());
                } catch (GeneralSecurityException e) {
                    mTextEnterPin.setText("");
                    // TODO implement try again and wipe if fail
                    Log.e("TEST", "PASSWORD CHECK FAILED: " + e.getMessage());
                    return;
                }
            }
        });

        mTextEnterPin.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_GO)
                {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    Handler threadHandler = new Handler();
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0, new ResultReceiver(
                            threadHandler)
                    {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            super.onReceiveResult(resultCode, resultData);
                            mButton.performClick();
                        }
                    });
                    return true;
                }
                return false;
            }
        });
        
        
    }
    
    
    
    
    public class TwoViewSlider {

        private boolean firstIsShown = true;
        private ViewFlipper flipper;
        private LinearLayout container1;
        private LinearLayout container2;
        private View firstView;
        private View secondView;
        private Animation pushRightIn;
        private Animation pushRightOut;
        private Animation pushLeftIn;
        private Animation pushLeftOut;

        public TwoViewSlider(ViewFlipper flipper, LinearLayout container1, LinearLayout container2,
                View view1, View view2) {
            this.flipper = flipper;
            this.container1 = container1;
            this.container2 = container2;
            this.firstView = view1;
            this.secondView = view2;

            pushRightIn = AnimationUtils.loadAnimation(CacheWordActivity.this, R.anim.push_right_in);
            pushRightOut = AnimationUtils.loadAnimation(CacheWordActivity.this, R.anim.push_right_out);
            pushLeftIn = AnimationUtils.loadAnimation(CacheWordActivity.this, R.anim.push_left_in);
            pushLeftOut = AnimationUtils.loadAnimation(CacheWordActivity.this, R.anim.push_left_out);

        }

        public void showNewPasswordField() {
            if (firstIsShown)
                return;

            flipper.setInAnimation(pushRightIn);
            flipper.setOutAnimation(pushRightOut);
            flip();
        }

        public void showConfirmationField() {
            if (!firstIsShown)
                return;

            flipper.setInAnimation(pushLeftIn);
            flipper.setOutAnimation(pushLeftOut);
            flip();
        }

        private void flip() {
            if (firstIsShown) {
                firstIsShown = false;
                container2.removeAllViews();
                container2.addView(secondView);
            } else {
                firstIsShown = true;
                container1.removeAllViews();
                container1.addView(firstView);
            }
            flipper.showNext();
        }
    }
    
}