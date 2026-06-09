package com.example.projetointegrador;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


public class formVac1 extends AppCompatActivity implements View.OnClickListener {


    Button btnProximo;
    ImageButton btVoltar, btFechar;


    EditText txtNome_vacina;    // nome da vacina
    EditText txtData_aplicacao; // data em que foi aplicada
    EditText txtHora_aplicacao; // horário da aplicação
    EditText txtNome_vet;       // nome do veterinário
    EditText txtCRMV_vet;       // CRMV do veterinário

    RadioGroup rgDose; // grupo de opções: 1ª Dose, 2ª Dose ou Reforço


    ScrollView formVac1;

    Pet pet; // objeto do pet dono da vacina, recebido da tela anterior

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_vac1);


        // Recupera o objeto Pet enviado pela tela anterior via Intent
        pet = (Pet) getIntent().getSerializableExtra("PET_OBJETO");


        btnProximo = findViewById(R.id.btnProximo);
        btVoltar   = findViewById(R.id.btVoltar);
        btFechar   = findViewById(R.id.btFechar);


        btnProximo.setOnClickListener(this);
        btVoltar.setOnClickListener(this);
        btFechar.setOnClickListener(this);


        txtNome_vacina    = findViewById(R.id.txtNome_vacina);
        txtData_aplicacao = findViewById(R.id.txtData_aplicacao);
        txtHora_aplicacao = findViewById(R.id.txtHora_aplicacao);
        txtNome_vet       = findViewById(R.id.txtNome_vet);
        txtCRMV_vet       = findViewById(R.id.txtCRMV_vet);
        rgDose            = findViewById(R.id.rgDose);

        formVac1 = findViewById(R.id.formVac1);
    }

    @Override
    public void onClick(View view) {


        if (view.getId() == R.id.btnProximo) {


            String nome_vacina    = txtNome_vacina.getText().toString().trim();
            String Nome_vet       = txtNome_vet.getText().toString().trim();
            String CRMV_vet       = txtCRMV_vet.getText().toString().trim();

            String data_aplicacao = tratarDataParaEnvio(txtData_aplicacao.getText().toString());
            String hora_aplicacao = txtHora_aplicacao.getText().toString().trim();

            if (hora_aplicacao.isEmpty()) {
                hora_aplicacao = null;
            }

            if (nome_vacina.isEmpty()) {
                txtNome_vacina.setError("Campo obrigatório");
                return;
            }

            //  Captura o RadioButton selecionado
            int idSelecionadoVac = rgDose.getCheckedRadioButtonId();


            if (idSelecionadoVac == -1) {
                Toast.makeText(this, "Selecione o tipo/dose da vacina!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pega o texto do RadioButton selecionado (ex: "1° Dose", "2° Dose", "Reforço")
            RadioButton radioSelecionadoVac = findViewById(idSelecionadoVac);
            String dose = radioSelecionadoVac.getText().toString();

            // Cria o objeto Vacina com os dados desta tela
            // Os campos do form2 e form3 serão adicionados nas próximas telas
            Vacina vacina = new Vacina(nome_vacina, dose, data_aplicacao, hora_aplicacao, Nome_vet, CRMV_vet);

            // Navega para o formVac2 passando os objetos
            Intent intent = new Intent(this, formVac2.class);


            intent.putExtra("VACINA_OBJ", vacina); // vacina com dados do form1
            intent.putExtra("PET_OBJETO", pet);     // pet dono da vacina

            startActivity(intent);
        }


        if (view.getId() == R.id.btVoltar) {
            finish();
        }


        if (view.getId() == R.id.btFechar) {
            Intent main = new Intent(this, MainActivity.class);
            main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(main);
            finish();
        }
    }

    private String tratarDataParaEnvio(String dataInput) {
        if (dataInput == null || dataInput.trim().isEmpty()) {
            return null; // Envia NULL legítimo se o campo estiver vazio
        }

        String dataLimpa = dataInput.trim();

        // Transforma o formato BR (DD/MM/AAAA) para o formato do banco (AAAA-MM-DD)
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