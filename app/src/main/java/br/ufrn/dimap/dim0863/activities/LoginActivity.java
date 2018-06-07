package br.ufrn.dimap.dim0863.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import br.ufrn.dimap.dim0863.R;
import br.ufrn.dimap.dim0863.util.Session;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
}
