package elapse.easyread;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import static com.android.volley.Request.Method.GET;
import static com.android.volley.Request.Method.POST;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{
    private static final String TAG = "LOGIN ACTIVITY";


    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences_file_key),Context.MODE_PRIVATE);
        String user = sharedPref.getString("logged_user",null);

        if(user !=null){
            EasyReadSingleton.getInstance(getApplicationContext()).setUserId(user);
            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(i);
        }else{
            setContentView(R.layout.activity_login);
            // Set up the login form.
            mUsernameView = (EditText) findViewById(R.id.username);

            mPasswordView = (EditText) findViewById(R.id.password);
            mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    if (id == R.id.login || id == EditorInfo.IME_NULL) {
                        attemptLogin();
                        return true;
                    }
                    return false;
                }
            });

            Button mSignInButton = (Button) findViewById(R.id.email_sign_in_button);
            mSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });
            Button mCreateAccountButton = (Button) findViewById(R.id.create_account_button);
            mCreateAccountButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    createAccount();
                }
            });

            mLoginFormView = findViewById(R.id.login_form);
            mProgressView = findViewById(R.id.login_progress);

        }
    }

    private void attemptLogin() {

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("Field is required");
            focusView = mPasswordView;
            cancel = true;
        }

        else if (TextUtils.isEmpty(username)) {
            mUsernameView.setError("Field is required");
            focusView = mUsernameView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            JSONObject body = new JSONObject();
            try{
                body.put("_id",username);
                body.put("password",password);
            }catch (JSONException e){
                Log.d(TAG,e.toString());
            }
            JsonObjectRequest req = new JsonObjectRequest(POST, Config.API+"users/login",body, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    EasyReadSingleton.getInstance(getApplicationContext()).setUserId(username);
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences_file_key),Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("logged_user",username);
                    editor.commit();

                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    showProgress(false);
                    startActivity(i);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    if(error.networkResponse == null){
                        Toast.makeText(getApplicationContext(),R.string.api_error,Toast.LENGTH_LONG).show();
                    }else{
                        if(error.networkResponse.statusCode == 403){
                            mPasswordView.setError("Incorrect Password");
                            mPasswordView.requestFocus();
                        }else if(error.networkResponse.statusCode == 404){
                            mUsernameView.setError("Username does not exist.");
                            mUsernameView.requestFocus();
                        }else{
                            Log.d(TAG,error.toString());
                            Toast.makeText(getApplicationContext(),R.string.api_error,Toast.LENGTH_LONG).show();
                        }
                    }

                    showProgress(false);
                }
            });
            EasyReadSingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
        }
    }


    private void createAccount(){
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        final String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("Field is required");
            focusView = mPasswordView;
            cancel = true;
        }

        else if (TextUtils.isEmpty(username) || ! isUsernameValid(username)) {
            mUsernameView.setError("Username must be 5 characters");
            focusView = mUsernameView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            JSONObject body = new JSONObject();
            try{
                body.put("_id",username);
                body.put("password",password);
            }catch (JSONException e){
                Log.d(TAG,e.toString());
            }
            JsonObjectRequest req = new JsonObjectRequest(POST, Config.API+"users",body, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(getApplicationContext(),"Account created. Logging in.",Toast.LENGTH_LONG).show();
                    EasyReadSingleton.getInstance(getApplicationContext()).setUserId(username);
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    if(error.networkResponse.statusCode == 403){
                        mUsernameView.setError("Username already in use");
                        mUsernameView.requestFocus();
                    }else{
                        Toast.makeText(getApplicationContext(),R.string.api_error,Toast.LENGTH_LONG).show();
                    }
                    showProgress(false);
                }
            });
            EasyReadSingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
        }
    }

    private boolean isUsernameValid(String name) {
        return name.length()>=5;
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}

