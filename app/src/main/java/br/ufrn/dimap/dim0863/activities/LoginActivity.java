package br.ufrn.dimap.dim0863.activities;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import br.ufrn.dimap.dim0863.R;
import br.ufrn.dimap.dim0863.services.MyFirebaseInstanceIDService;
import br.ufrn.dimap.dim0863.util.Session;

public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 100;

    private final static String TAG = LoginActivity.class.getSimpleName();

    // The authority for the sync adapter's content provider
    public static final String USER_LOCATION_AUTHORITY = "br.ufrn.dimap.dim0863.user.provider";
    public static final String CAR_INFO_AUTHORITY = "br.ufrn.dimap.dim0863.car.provider";

    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "br.ufrn.dimap.dim0863";

    public static final String ACCOUNT = "default_account";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Create the dummy account
        Account carInfoAccount = createSyncAccount(this, ACCOUNT, ACCOUNT_TYPE, CAR_INFO_AUTHORITY);
        Account userLocationAccount = createSyncAccount(this, ACCOUNT, ACCOUNT_TYPE, USER_LOCATION_AUTHORITY);

        ContentResolver.setSyncAutomatically(carInfoAccount, CAR_INFO_AUTHORITY, true);
        ContentResolver.setSyncAutomatically(userLocationAccount, USER_LOCATION_AUTHORITY, true);

        Button botaoLogin = findViewById(R.id.botao_logar);
        final EditText etUsername = findViewById(R.id.login_username);

        botaoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();

                Session session = new Session(LoginActivity.this);
                session.setusename(username);

                String token = FirebaseInstanceId.getInstance().getToken();
                MyFirebaseInstanceIDService.sendRegistrationToServer(getApplicationContext(), token);

                Intent principal = new Intent(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(principal);
            }
        });

        requestLocationPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();

        final EditText etUsername = findViewById(R.id.login_username);
        etUsername.setText("luksrn");
    }

    private void requestLocationPermissions() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if (!(ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION))
                    && !(ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION))) {
                ActivityCompat.requestPermissions(LoginActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length <= 0 || grantResults[0] == PackageManager.PERMISSION_DENIED
                        || grantResults[1] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getApplicationContext(), "Por favor, aceite as permissões solicitadas para poder utilizar o app.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account createSyncAccount(Context context, String account, String accountType, String authority) {
        // Create the account type and default account
        Account newAccount = new Account(account, accountType);

        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager != null && accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, USER_LOCATION_AUTHORITY, 1)
             * here.
             */
            Bundle extras = new Bundle();
            ContentResolver.setIsSyncable(newAccount, authority, 1);
            ContentResolver.setSyncAutomatically(newAccount, authority, true);
            ContentResolver.requestSync(newAccount, authority, extras);
            Log.d(TAG, "Account created");
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            Log.e(TAG, "Account not created. Account already exists.");
        }

        //TODO Change
        return newAccount;
    }
}
