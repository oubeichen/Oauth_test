package com.oubeichen.oauth_test_client;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.VKRequest.VKRequestListener;
import com.vk.sdk.dialogs.VKCaptchaDialog;
import com.vk.sdk.util.VKUtil;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {
    
    private static final String[] sMyScope = new String[] {
            VKScope.FRIENDS,
            VKScope.WALL,
            VKScope.NOHTTPS
    };
    
    private final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VKUIHelper.onCreate(this);
        VKSdk.initialize(sdkListener, "4654541");
        if (VKSdk.wakeUpSession()) {
            showUserInfo();
            return;
        }

        String[] fingerprint = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        Log.d("Fingerprint", fingerprint[0]);
    }

    private void showLogout() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new LogoutFragment(), "logout_view")
                .commit();
    }
    private void showLogin() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new LoginFragment())
                .commit();
    }

    private final VKSdkListener sdkListener = new VKSdkListener() {
        @Override
        public void onCaptchaError(VKError captchaError) {
            new VKCaptchaDialog(captchaError).show();
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            VKSdk.authorize(sMyScope);
        }

        @Override
        public void onAccessDenied(final VKError authorizationError) {
            new AlertDialog.Builder(VKUIHelper.getTopActivity())
                    .setMessage(authorizationError.toString())
                    .show();
        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            showUserInfo();
        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {
            showUserInfo();
        }
    };
    
    /* user already logged in */
    private void showUserInfo(){
        Log.d(TAG, "already logged in");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
        if (VKSdk.isLoggedIn()) {
            showLogout();
        } else {
            showLogin();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKUIHelper.onActivityResult(this, requestCode, resultCode, data);
    }
    

    public void setAuthId(String string) {
        LogoutFragment fragment = (LogoutFragment) getSupportFragmentManager()
                .findFragmentByTag("logout_view");
        TextView authId = (TextView)fragment.getView().findViewById(R.id.auth_id);
        authId.setText("Auth details: " + string);
    }

    public static class LoginFragment extends android.support.v4.app.Fragment {
        public LoginFragment() {
            super();
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_login, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getView().findViewById(R.id.button_vk).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VKSdk.authorize(sMyScope, true, true);
                }
            });
        }
    }

    public class LogoutFragment extends android.support.v4.app.Fragment {
        public LogoutFragment() {
            super();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_logout, container, false);
            TextView authType = (TextView)view.findViewById(R.id.auth_type);
            VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS,
                    "id,first_name,last_name"));
            if(VKSdk.isLoggedIn()){
                authType.setText("Auth type: VK");
                new GetVKDetailsTask().execute(request);
            }

            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            getView().findViewById(R.id.button_logout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VKSdk.logout();
                    if (!VKSdk.isLoggedIn()) {
                        ((MainActivity)getActivity()).showLogin();
                    }
                }
            });
        }
    }
    
    private class GetVKDetailsTask extends AsyncTask<VKRequest, Void, Void> {  
        @Override
        protected Void doInBackground(VKRequest... requests) {
            requests[0].executeWithListener(new VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    setAuthId(response.json.toString());
                }

                @Override
                public void onError(VKError error) {
                    super.onError(error);
                }
            });
            return null;
        }
    }
}
