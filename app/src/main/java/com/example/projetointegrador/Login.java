package com.example.projetointegrador;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Login extends AppCompatActivity implements View.OnClickListener {


    EditText txtEmail, txtSenha;
    Button btEntrar;
    TextView txtCadastroLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);


        txtEmail        = findViewById(R.id.txtLogEmail);
        txtSenha        = findViewById(R.id.txtLogSenha);
        btEntrar        = findViewById(R.id.btEntrar);
        txtCadastroLink = findViewById(R.id.txtCadastroLink);


        btEntrar.setOnClickListener(this);
        txtCadastroLink.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {


        if (v.getId() == R.id.btEntrar) {


            String email = txtEmail.getText().toString().trim();
            String senha = txtSenha.getText().toString().trim();

            // Valida se os campos foram preenchidos
            if (email.isEmpty()) {
                Toast.makeText(this, "O campo de Email deve ser preenchido!", Toast.LENGTH_LONG).show();
                return;
            }
            if (senha.isEmpty()) {
                Toast.makeText(this, "O campo de Senha deve ser preenchido!", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                String senhaHash = md5(senha); // converte a senha para MD5 antes de enviar
                verificarLoginSupabase(email, senhaHash);
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao processar senha.", Toast.LENGTH_SHORT).show();
            }
        }


        if (v.getId() == R.id.txtCadastroLink) {
            startActivity(new Intent(this, Cadastre_se.class)); // abre a tela de cadastro
        }
    }

    private void verificarLoginSupabase(String email, String senha) {
        OkHttpClient client = new OkHttpClient();


        Request request = new Request.Builder()
                .url(SupabaseConfig.URL + "/rest/v1/usuarios"
                        + "?email=eq."      + email  // filtra pelo email
                        + "&senha_hash=eq." + senha  // filtra pela senha em MD5
                        + "&select=id_usuario,nome,email")
                .get()
                .addHeader("apikey",        SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();


        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                Log.e("LOGIN_ERRO", e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(Login.this,
                                "Erro de conexão: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String json = response.body() != null ? response.body().string() : "[]";
                Log.d("LOGIN_RESP", json);


                runOnUiThread(() -> {
                    try {

                        JsonArray array = JsonParser.parseString(json).getAsJsonArray();

                        if (array.size() > 0) {

                            Intent tela = new Intent(Login.this, MainActivity.class);
                            tela.putExtra("emailLogado", email);
                            startActivity(tela);
                            finish();
                        } else {

                            Toast.makeText(Login.this,
                                    "E-mail ou senha incorretos!",
                                    Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {

                        Log.e("LOGIN_PARSE", e.getMessage());
                        Toast.makeText(Login.this,
                                "Erro ao processar login.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Converte uma string para MD5 (hash de 32 caracteres hexadecimais)
    // Usado para nunca enviar a senha em texto puro
    public static String md5(String senha) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(senha.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}