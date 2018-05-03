package br.ufrn.dimap.dim0863.sigs;

import android.util.Log;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import br.ufrn.dimap.dim0863.sigs.dto.Usuario;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UsuarioGet {

    private String servicoUsuario = "/usuario/v0.1/usuarios/info";

    public Usuario getInfo(String urlBase, String token, String apiKey) throws IOException {
        String url = urlBase + servicoUsuario;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "bearer " + token)
                .addHeader("x-api-key", apiKey)
                .build();

        Response response = client.newCall(request).execute();

        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Usuario> jsonAdapter = moshi.adapter(Usuario.class);

        String json = response.body().string();
        Log.d(UsuarioGet.class.getSimpleName(), "Requisição: " + json);
        Usuario usuario = jsonAdapter.fromJson(json);

        return usuario;
    }
}
