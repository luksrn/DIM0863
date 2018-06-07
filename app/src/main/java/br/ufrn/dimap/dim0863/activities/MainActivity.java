package br.ufrn.dimap.dim0863.activities;

import android.Manifest;
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
import android.widget.TextView;
import android.widget.Toast;

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
import br.ufrn.dimap.dim0863.util.RequestManager;
import br.ufrn.dimap.dim0863.util.Session;
import br.ufrn.dimap.dim0863.services.LocationService;

public class MainActivity extends AppCompatActivity {

    private boolean locationPermission;
    private static final int REQUEST_PERMISSIONS = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnReadQRCode = findViewById(R.id.btn_read_qrcode);

        btnReadQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator scanIntegrator = new IntentIntegrator(MainActivity.this);
                scanIntegrator.setPrompt("Scan");
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


        requestLocationPermissions();

        if (locationPermission) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            startService(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Por favor, habilite o GPS", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestLocationPermissions() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if (!(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION))
                    && !(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION))) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSIONS);
            }
        } else {
            locationPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    locationPermission = true;
                } else {
                    Toast.makeText(getApplicationContext(), "Por favor, aceite as permissões solicitadas",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
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

            Session session = new Session(MainActivity.this);
            username = session.getusename();

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
