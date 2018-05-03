package br.ufrn.dimap.dim0863;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import br.ufrn.mobile.japi.JApiWebView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);

        JApiWebView japiWebView = (JApiWebView) findViewById(R.id.japiwebview);
        japiWebView.loadJapiWebView("https://autenticacao.info.ufrn.br", "dim086320181-id", "segredo", this, PrincipalActivity.class);

       /* botaoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();

                Session session = new Session(LoginActivity.this);
                session.setusename(username);

                Intent principal = new Intent(LoginActivity.this, PrincipalActivity.class);
                LoginActivity.this.startActivity(principal);
            }
        });*/
    }
}
