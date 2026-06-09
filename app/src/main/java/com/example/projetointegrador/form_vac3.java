package com.example.projetointegrador;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class form_vac3 extends AppCompatActivity implements View.OnClickListener {


    Button btnProximo3;
    ImageButton btVoltar3, btFechar3;


    EditText txtNome_pro_vac; // nome da próxima vacina
    EditText txtData_pro_vac; // data da próxima aplicação
    EditText txtAnota_vac;    // anotações gerais

    CheckBox lembrar;     // checkbox para ativar lembrete
    ScrollView formVac3;  // scroll da tela

    Pet pet;              // objeto do pet dono da vacina
    Vacina vacinaDados;   // objeto Vacina acumulado desde o form1 e form2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_vac3);


        if (getIntent().hasExtra("VACINA_OBJ")) {
            vacinaDados = (Vacina) getIntent().getSerializableExtra("VACINA_OBJ");
        }

        // Recupera o Pet enviado pelo form2
        pet = (Pet) getIntent().getSerializableExtra("PET_OBJETO");


        btnProximo3 = findViewById(R.id.btnProximo3);
        btVoltar3   = findViewById(R.id.btVoltar3);
        btFechar3   = findViewById(R.id.btFechar3);


        btnProximo3.setOnClickListener(this);
        btVoltar3.setOnClickListener(this);
        btFechar3.setOnClickListener(this);


        txtNome_pro_vac = findViewById(R.id.txtNome_pro_vac);
        txtData_pro_vac = findViewById(R.id.txtData_pro_vac);
        txtAnota_vac    = findViewById(R.id.txtAnota_vac);
        lembrar         = findViewById(R.id.lembrar);
        formVac3        = findViewById(R.id.formVac3);
    }

    @Override
    public void onClick(View view) {


        if (view.getId() == R.id.btnProximo3) {


            if (vacinaDados == null) {
                Toast.makeText(this, "Erro interno: dados não localizados.", Toast.LENGTH_SHORT).show();
                return; // interrompe
            }

            String dataProVacTratada = tratarDataParaEnvio(txtData_pro_vac.getText().toString());

            // Adiciona os dados desta tela ao objeto Vacina já existente
            vacinaDados.setNome_pro_vac(txtNome_pro_vac.getText().toString().trim());
            vacinaDados.setData_pro_vac(dataProVacTratada);
            vacinaDados.setLembrar(lembrar.isChecked() ? "Sim" : "Não"); // checkbox → texto
            vacinaDados.setAnota_vac(txtAnota_vac.getText().toString().trim());

            salvarNoSupabase();
        }


        if (view.getId() == R.id.btVoltar3) {

            Intent form2 = new Intent(this, formVac2.class);
            form2.putExtra("VACINA_OBJ", vacinaDados);
            form2.putExtra("PET_OBJETO", pet);
            startActivity(form2);
            finish();
        }


        if (view.getId() == R.id.btFechar3) {
            Intent main = new Intent(this, MainActivity.class);
            main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(main);
            finish();
        }
    }


    // Salva o nome da vacina na tabela "vacinas" e recupera o id_vacina gerado pelo banco

    private void salvarNoSupabase() {
        OkHttpClient client = new OkHttpClient();

        // Monta o JSON com apenas o nome da vacina
        JsonObject jsonVacina = new JsonObject();
        jsonVacina.addProperty("nome_vacina", vacinaDados.getNomeVac());


        RequestBody bodyVacina = RequestBody.create(
                jsonVacina.toString(), MediaType.parse("application/json"));


        Request requestVacina = new Request.Builder()
                .url(SupabaseConfig.URL + "/rest/v1/vacinas")
                .post(bodyVacina)
                .addHeader("apikey",        SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .addHeader("Content-Type",  "application/json")
                .addHeader("Prefer",        "return=representation") // retorna o registro criado
                .build();


        client.newCall(requestVacina).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(() -> Toast.makeText(form_vac3.this,
                        "Erro ao criar vacina: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

                    String respBody = response.body() != null ? response.body().string() : "[]";

                    int idVacina = -1;
                    try {

                        idVacina = JsonParser.parseString(respBody)
                                .getAsJsonArray()
                                .get(0)
                                .getAsJsonObject()
                                .get("id_vacina")
                                .getAsInt();
                    } catch (Exception ignored) {}

                    final int finalIdVacina = idVacina;
                    runOnUiThread(() -> salvarNaCarteira(finalIdVacina));
                } else {
                    String erro = response.body() != null ? response.body().string() : "erro";
                    runOnUiThread(() -> Toast.makeText(form_vac3.this,
                            "Erro ao salvar vacina: " + erro, Toast.LENGTH_LONG).show());
                }
            }
        });
    }


    // Salva todos os dados na tabela "carteira_vacinacao"


    private void salvarNaCarteira(int idVacina) {
        OkHttpClient client = new OkHttpClient();

        // Monta o JSON com todos os campos da carteira de vacinação
        JsonObject json = new JsonObject();
        json.addProperty("id_animal",      pet != null ? pet.getId() : 0); // id do pet
        json.addProperty("id_vacina",      idVacina);          // id gerado
        json.addProperty("Data_aplicacao", vacinaDados.getData_aplicacao());
        json.addProperty("Hora_aplicacao", vacinaDados.getHora_aplicacao());
        json.addProperty("Nome_vet",       vacinaDados.getNome_vet());
        json.addProperty("CRMV_vet",       vacinaDados.getCRMV_vet());
        json.addProperty("dose",           vacinaDados.getDose());
        json.addProperty("Lote",           vacinaDados.getLote());
        json.addProperty("Data_val",       vacinaDados.getData_val());
        json.addProperty("Fabricante",     vacinaDados.getFabricante());
        json.addProperty("dose2",          vacinaDados.getDose2());
        json.addProperty("NomeRef",        vacinaDados.getNomeRef());
        json.addProperty("DataRef",        vacinaDados.getDataRef());
        json.addProperty("ObsVac",         vacinaDados.getObsVac());
        json.addProperty("Nome_pro_vac",   vacinaDados.getNome_pro_vac());
        json.addProperty("Data_pro_vac",   vacinaDados.getData_pro_vac());
        json.addProperty("lembrar",        vacinaDados.getLembrar());
        json.addProperty("Anota_vac",      vacinaDados.getAnota_vac());
        json.addProperty("status",         "Aplicada");

        RequestBody body = RequestBody.create(
                json.toString(), MediaType.parse("application/json"));


        Request request = new Request.Builder()
                .url(SupabaseConfig.URL + "/rest/v1/carteira_vacinacao")
                .post(body)
                .addHeader("apikey",        SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .addHeader("Content-Type",  "application/json")
                .addHeader("Prefer",        "return=minimal") // não precisa retornar dados
                .build();


        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(() -> Toast.makeText(form_vac3.this,
                        "Erro de conexão: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

                    runOnUiThread(() -> {
                        Toast.makeText(form_vac3.this,
                                "Vacina salva com sucesso!", Toast.LENGTH_SHORT).show();
                        Intent tela = new Intent(form_vac3.this, VacinasPetActivity.class);
                        tela.putExtra("PET_OBJETO", pet);
                        startActivity(tela);
                        finish();
                    });
                } else {

                    String erro = response.body() != null ? response.body().string() : "erro";
                    Log.e("SUPABASE_ERRO", erro);
                    runOnUiThread(() -> Toast.makeText(form_vac3.this,
                            "Erro ao salvar: " + erro, Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private String tratarDataParaEnvio(String dataInput) {
        if (dataInput == null || dataInput.trim().isEmpty()) {
            return null; // Retorna null legítimo se o usuário não agendar uma próxima vacina
        }

        String dataLimpa = dataInput.trim();

        // Converte o formato BR (DD/MM/AAAA) para o formato ISO (AAAA-MM-DD)
        if (dataLimpa.contains("/")) {
            String[] partes = dataLimpa.split("/");
            if (partes.length == 3) {
                String dia = partes[0];
                String mes = partes[1];
                String ano = partes[2];

                return ano + "-" + mes + "-" + dia;
            }
        }

        return dataLimpa;
    }
}