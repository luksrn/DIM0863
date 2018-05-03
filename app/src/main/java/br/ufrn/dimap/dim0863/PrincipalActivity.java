package br.ufrn.dimap.dim0863;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import br.ufrn.dimap.dim0863.sigs.UsuarioGet;
import br.ufrn.dimap.dim0863.sigs.dto.Usuario;
import br.ufrn.mobile.japi.JApi;

public class PrincipalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.principal_main);

        Button botaoLerQRCode = findViewById(R.id.ler_qrcode_button);

        botaoLerQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator scanIntegrator = new IntentIntegrator(PrincipalActivity.this);
                scanIntegrator.setPrompt("Scan");
                scanIntegrator.setBeepEnabled(false);
                //The following line if you want QR code
                scanIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                scanIntegrator.setCaptureActivity(CaptureActivity.class);
                scanIntegrator.setOrientationLocked(true);
                scanIntegrator.setBarcodeImageEnabled(true);
                scanIntegrator.initiateScan();
            }
        });


        Button botaoSair = findViewById(R.id.deslogar);

        botaoSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JApi.deslogar(PrincipalActivity.this);

                Intent loginActivity = new Intent(PrincipalActivity.this, LoginActivity.class);
                PrincipalActivity.this.startActivity(loginActivity);
            }
        });


        new AsyncTask<String, Void, Usuario>() {


            protected Usuario doInBackground (String...urls){
                try {
                    return new UsuarioGet().getInfo("https://api.info.ufrn.br",
                            JApi.getAccessToken(PrincipalActivity.this),
                            "jeKxlbdi9PxgCdZ1HlphYH4JJYAKY4J1aOv2VAwk");
                } catch (IOException e){
                    Log.e(PrincipalActivity.class.getSimpleName(), "Erro: " + e.getMessage(), e);
                    return null;
                }

            }

            protected void onPostExecute (Usuario u){
                TextView tv = findViewById(R.id.text_view_nome_usuario);
                if( u != null ) {
                    tv.setText("Usuário: " + u.getNomePessoa());
                    new Session(PrincipalActivity.this).setusename( u.getLogin() );
                }else
                    tv.setText( "Usuário: Falha ao recuperar"  );


            }
        }.execute();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final TextView consoleLog = findViewById(R.id.log_operacoes);
        String idChaveiro = "";
        String username = "";

        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanningResult != null && scanningResult.getContents() != null) {
            idChaveiro = scanningResult.getContents();
            consoleLog.append("QRCode com Chaveiro = " + idChaveiro + "\n");

            Session session = new Session(PrincipalActivity.this);
            username = session.getusename();

            JSONObject requestJSON = new JSONObject();
            try {
                requestJSON.put("login", username);
                requestJSON.put("chaveiro", idChaveiro);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, RequestManager.CHAVEIRO_ENDPOINT, requestJSON, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                consoleLog.append( "Chave está na porta: " + response.getInt("numeroChave") );
                            } catch (JSONException e) {
                                consoleLog.append( "Erro ao ler resposta " + response.toString() );

                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if( error instanceof  AuthFailureError){
                                consoleLog.append( "Usuário não autorizado." );
                            } else {
                                consoleLog.append( error.toString() );
                            }

                        }
                    });

            RequestManager.getInstance(this).addToRequestQueue(jsonObjectRequest);
        } else {
            consoleLog.append("NOOP\n");
        }
    }

}
