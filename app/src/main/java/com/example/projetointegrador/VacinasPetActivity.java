package com.example.projetointegrador;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VacinasPetActivity extends AppCompatActivity {

    LinearLayout containerVacinas;
    ImageButton btnCadasVac;
    ImageButton btVoltarPets;
    Pet pet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_minhas_vacinas);

        containerVacinas = findViewById(R.id.containerVacinas);
        btnCadasVac      = findViewById(R.id.btnCadasVac);
        btVoltarPets     = findViewById(R.id.btVoltarPets);

        pet = (Pet) getIntent().getSerializableExtra("PET_OBJETO");
        if (pet == null) { finish(); return; }

        btVoltarPets.setOnClickListener(v -> finish());

        btnCadasVac.setOnClickListener(v -> {
            Intent intent = new Intent(this, formVac1.class);
            intent.putExtra("PET_OBJETO", pet);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarVacinas();
    }

    private void carregarVacinas() {
        containerVacinas.removeAllViews();
        OkHttpClient client = new OkHttpClient();

        // Busca vacinas do Supabase filtrando pelo ID do pet
        Request request = new Request.Builder()
                .url(SupabaseConfig.URL + "/rest/v1/carteira_vacinacao"
                        + "?id_animal=eq." + pet.getId()
                        + "&select=id_registro,Data_aplicacao,Hora_aplicacao,Nome_vet,CRMV_vet,dose,Lote,Data_val,Fabricante,dose2,NomeRef,DataRef,Nome_pro_vac,Data_pro_vac,lembrar,ObsVac,Anota_vac,vacinas(id_vacina,nome_vacina)")
                .get()
                .addHeader("apikey",        SupabaseConfig.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    TextView tv = new TextView(VacinasPetActivity.this);
                    tv.setText("Erro de conexão: " + e.getMessage());
                    tv.setPadding(40, 20, 40, 20);
                    containerVacinas.addView(tv);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body() != null ? response.body().string() : "[]";

                runOnUiThread(() -> {
                    try {
                        JsonArray array = JsonParser.parseString(json).getAsJsonArray();

                        if (array.size() == 0) {
                            TextView tv = new TextView(VacinasPetActivity.this);
                            tv.setText("Nenhuma vacina cadastrada para " + pet.getNome());
                            tv.setPadding(40, 20, 40, 20);
                            containerVacinas.addView(tv);
                            return;
                        }

                        for (JsonElement el : array) {
                            JsonObject obj = el.getAsJsonObject();

                            // Monta objeto Vacina com os dados do Supabase
                            Vacina v = new Vacina();

                            if (obj.has("vacinas") && !obj.get("vacinas").isJsonNull()) {
                                JsonObject vacObj = obj.get("vacinas").getAsJsonObject();
                                if (vacObj.has("nome_vacina") && !vacObj.get("nome_vacina").isJsonNull()) {
                                    v.setNomeVac(vacObj.get("nome_vacina").getAsString());
                                } else {
                                    v.setNomeVac("Vacina sem nome");
                                }
                            } else {
                                v.setNomeVac("Vacina não identificada");
                            }


                            v.setDose(obterTextoValido(obj, "dose"));
                            v.setData_aplicacao(obterTextoValido(obj, "Data_aplicacao"));
                            v.setHora_aplicacao(obterTextoValido(obj, "Hora_aplicacao"));
                            v.setNome_vet(obterTextoValido(obj, "Nome_vet"));
                            v.setCRMV_vet(obterTextoValido(obj, "CRMV_vet"));
                            v.setLote(obterTextoValido(obj, "Lote"));
                            v.setData_val(obterTextoValido(obj, "Data_val"));
                            v.setFabricante(obterTextoValido(obj, "Fabricante"));
                            v.setDose2(obterTextoValido(obj, "dose2"));
                            v.setNomeRef(obterTextoValido(obj, "NomeRef"));
                            v.setDataRef(obterTextoValido(obj, "DataRef"));
                            v.setObsVac(obterTextoValido(obj, "ObsVac"));
                            v.setNome_pro_vac(obterTextoValido(obj, "Nome_pro_vac"));
                            v.setData_pro_vac(obterTextoValido(obj, "Data_pro_vac"));
                            v.setLembrar(obterTextoValido(obj, "lembrar"));
                            v.setAnota_vac(obterTextoValido(obj, "Anota_vac"));

                            if (obj.has("id_registro") && !obj.get("id_registro").isJsonNull()) {
                                v.setId_vacina(obj.get("id_registro").getAsInt());
                            }

                            adicionarCardVacina(v);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        TextView tv = new TextView(VacinasPetActivity.this);
                        tv.setText("Erro técnico: " + e.getMessage());
                        tv.setPadding(40, 20, 40, 20);
                        containerVacinas.addView(tv);
                    }


                });
            }
        });
    }

    private void adicionarCardVacina(Vacina v) {
        View cardView = getLayoutInflater()
                .inflate(R.layout.item_pet, containerVacinas, false);

        TextView txtNome    = cardView.findViewById(R.id.txtNomeCard);
        TextView txtEspecie = cardView.findViewById(R.id.txtEspecieCard);

        txtNome.setText(v.getNomeVac());
        txtEspecie.setText("Data: " + v.getData_aplicacao());

        cardView.setOnClickListener(view -> {
            Intent intent = new Intent(VacinasPetActivity.this, Perfil_Vacina.class);
            intent.putExtra("VACINA_OBJ", v);
            intent.putExtra("PET_OBJETO", pet);
            startActivity(intent);
        });

        containerVacinas.addView(cardView);
    }
    private String obterTextoValido(JsonObject obj, String chave) {
        if (obj.has(chave) && !obj.get(chave).isJsonNull()) {
            return obj.get(chave).getAsString();
        }
        return ""; // Retorna string vazia caso o campo seja nulo ou não exista
    }
}