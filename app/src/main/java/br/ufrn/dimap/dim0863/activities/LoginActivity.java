package br.ufrn.dimap.dim0863.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import br.ufrn.dimap.dim0863.R;
import br.ufrn.dimap.dim0863.util.Session;

public class LoginActivity extends AppCompatActivity {

    private final static String TAG = LoginActivity.class.getSimpleName();

    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "br.ufrn.dimap.dim0863.provider";

    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "br.ufrn.dimap.dim0863";

    public static final String ACCOUNT = "default_account";

    public Account account;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Create the dummy account
        account = createSyncAccount(this);

        ContentResolver.setSyncAutomatically(account, AUTHORITY, true);

        Button botaoLogin = findViewById(R.id.botao_logar);
        final EditText etUsername = findViewById(R.id.login_username);

        botaoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();

                Session session = new Session(LoginActivity.this);
                session.setusename(username);

                Intent principal = new Intent(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(principal);
            }
        });
    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account createSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);

        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager != null && accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            Bundle extras = new Bundle();
            ContentResolver.setIsSyncable(newAccount, AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(newAccount, AUTHORITY, true);
            ContentResolver.requestSync(newAccount, AUTHORITY, extras);
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
