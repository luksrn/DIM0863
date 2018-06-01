package br.ufrn.dimap.dim0863.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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

public class PrincipalActivity extends AppCompatActivity {

    private boolean locationPermission;
    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;
    private static final int REQUEST_PERMISSIONS = 100;

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

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferencesEditor = preferences.edit();

        requestPermissions();

        if (locationPermission) {
//            if (preferences.getString("service", "").matches("")) {
//                preferencesEditor.putString("service", "service").apply();

                Intent intent = new Intent(getApplicationContext(), LocationService.class);
                startService(intent);
//            } else {
//                Toast.makeText(getApplicationContext(), "Location service is already running", Toast.LENGTH_SHORT).show();
//            }
        } else {
            Toast.makeText(getApplicationContext(), "Please enable the gps", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestPermissions() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if (!(ActivityCompat.shouldShowRequestPermissionRationale(PrincipalActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION))
                    && !(ActivityCompat.shouldShowRequestPermissionRationale(PrincipalActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION))) {
                ActivityCompat.requestPermissions(PrincipalActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSIONS);
            }
        } else {
            locationPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    locationPermission = true;
                } else {
                    Toast.makeText(getApplicationContext(), "Please allow the permission",
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
