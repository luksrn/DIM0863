package br.ufrn.dimap.dim0863.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
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

import br.ufrn.dimap.dim0863.R;
import br.ufrn.dimap.dim0863.receivers.CollectDataBroadcastReceiver;
import br.ufrn.dimap.dim0863.util.RequestManager;
import br.ufrn.dimap.dim0863.util.Session;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton btnReadQRCode = findViewById(R.id.btn_read_qrcode);

        btnReadQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator scanIntegrator = new IntentIntegrator(MainActivity.this);
                scanIntegrator.setPrompt("Escanear chaveiro");
                scanIntegrator.setBeepEnabled(false);
                scanIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);  //If you want QR code
                scanIntegrator.setCaptureActivity(CaptureActivity.class);
                scanIntegrator.setOrientationLocked(true);
                scanIntegrator.setBarcodeImageEnabled(true);
                scanIntegrator.initiateScan();
            }
        });

        Button btnConfigBluetooth = findViewById(R.id.btn_config_bluetooth);
        btnConfigBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(intent);
            }
        });

        Button btnCarInfo = findViewById(R.id.btn_car_info);
        btnCarInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CarInfoActivity.class);
                startActivity(intent);
            }
        });

        Button btnUserLocation = findViewById(R.id.btn_user_location);
        btnUserLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UserLocationActivity.class);
                startActivity(intent);
            }
        });

        Button btnStartServices = findViewById(R.id.btn_start_services);
        btnStartServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CollectDataBroadcastReceiver.START_COLLECT_REQUESTED);
                sendBroadcast(intent);
            }
        });

        Button btnStopServices = findViewById(R.id.btn_stop_services);
        btnStopServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CollectDataBroadcastReceiver.STOP_COLLECT_REQUESTED);
                sendBroadcast(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final TextView consoleLog = findViewById(R.id.log_operacoes);

        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanningResult != null && scanningResult.getContents() != null) {
            String idChaveiro = scanningResult.getContents();
            consoleLog.append("QRCode com Chaveiro = " + idChaveiro + "\n");

            Session session = new Session(MainActivity.this);
            String username = session.getusename();

            JSONObject requestJSON = new JSONObject();
            try {
                requestJSON.put("login", username);
                requestJSON.put("chaveiro", idChaveiro);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, RequestManager.CHAVEIRO_ENDPOINT, requestJSON, new Response.Listener<JSONObject>() {

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
