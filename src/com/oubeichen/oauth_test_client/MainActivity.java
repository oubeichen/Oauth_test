package com.oubeichen.oauth_test_client;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkTokenRequestListener;
import ru.ok.android.sdk.util.OkScope;

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
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
    
    protected final Context mContext = this;
    
    private static final String VK_APP_ID = "4654541";
    
    private static Odnoklassniki mOdnoklassniki;
    private static final String OK_APP_ID = "1110583040";
    private static final String OK_APP_SECRET = "7A1D1FF34AA6AAE4240CF063";
    private static final String OK_APP_KEY = "CBAICDDDEBABABABA";
    
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
        VKSdk.initialize(vkSdkListener, VK_APP_ID);
        
        mOdnoklassniki = Odnoklassniki.createInstance(this, OK_APP_ID, OK_APP_SECRET, OK_APP_KEY);
        mOdnoklassniki.setTokenRequestListener(okRequestListener);
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

    private final VKSdkListener vkSdkListener = new VKSdkListener() {
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
    
    OkTokenRequestListener okRequestListener = new OkTokenRequestListener() {
        @Override
        public void onSuccess(final String accessToken) {
            Toast.makeText(mContext, "Recieved token : " + accessToken, Toast.LENGTH_SHORT).show();
            showUserInfo();
        }

        @Override
        public void onCancel() {
            Toast.makeText(mContext, "Authorization was canceled", Toast.LENGTH_SHORT).show();
        }
        
        @Override
        public void onError() {
            Toast.makeText(mContext, "Error getting token", Toast.LENGTH_SHORT).show();
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
        if (VKSdk.isLoggedIn() || mOdnoklassniki.hasAccessToken()) {
            showLogout();
        } else {
            showLogin();
        }

    }

    @Override
    protected void onDestroy() {
        VKUIHelper.onDestroy(this);
        mOdnoklassniki.removeTokenRequestListener();
        super.onDestroy();
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

    public static class LoginFragment extends Fragment {
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
            getView().findViewById(R.id.button_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOdnoklassniki.requestAuthorization(getActivity(), false, OkScope.VALUABLE_ACCESS);
                }
            });
        }
    }

    public class LogoutFragment extends Fragment {
        public LogoutFragment() {
            super();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_logout, container, false);
            TextView authType = (TextView)view.findViewById(R.id.auth_type);
            TextView authToken = (TextView)view.findViewById(R.id.auth_token);

            if(VKSdk.isLoggedIn()){
                VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS,
                        "id,first_name,last_name"));
                authType.setText("Auth type: VK");
                authToken.setText("Auth token: " + VKSdk.getAccessToken().accessToken);
                new GetVKDetailsTask().execute(request);
            } else if(mOdnoklassniki.hasAccessToken()){
                authType.setText("Auth type: Odnoklassniki");
                authToken.setText("Auth token: " + mOdnoklassniki.getCurrentAccessToken());
                new GetOKDetailsTask().execute();
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
                    mOdnoklassniki.clearTokens(getActivity());
                    if (!VKSdk.isLoggedIn() && !mOdnoklassniki.hasAccessToken()) {
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
    
    private class GetOKDetailsTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(final Void... params) {
            try {
                return mOdnoklassniki.request("users.getCurrentUser", null, "get");
            } catch (Exception exc) {
                Log.e("Odnoklassniki", "Failed to get current user info", exc);
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(final String result) {
            if (result != null) {
                setAuthId(result);
            }
        }
    }
}
