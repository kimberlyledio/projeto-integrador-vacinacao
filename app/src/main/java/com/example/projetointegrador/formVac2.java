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


public class formVac2 extends AppCompatActivity implements View.OnClickListener {

    // Botões da tela
    Button btnProximo2;
    ImageButton btVoltar2, btFechar2;


    EditText txtLote;       // lote da vacina
    EditText txtData_val;   // data de validade
    EditText txtFabricante; // fabricante da vacina
    EditText txtNomeRef;    // nome da vacina de reforço
    EditText txtDataRef;    // data do reforço
    EditText txtObsVac;     // observações

    RadioGroup rgDose2; // grupo de opções: possui reforço (Sim/Não)


    ScrollView formVac2;

    Pet pet; // objeto do pet dono da vacina, recebido da tela anterior

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_vac2);

        // Recupera o objeto Pet enviado pelo formVac1
        pet = (Pet) getIntent().getSerializableExtra("PET_OBJETO");


        btnProximo2 = findViewById(R.id.btnProximo2);
        btVoltar2   = findViewById(R.id.btVoltar2);
        btFechar2   = findViewById(R.id.btFechar2);


        btnProximo2.setOnClickListener(this);
        btVoltar2.setOnClickListener(this);
        btFechar2.setOnClickListener(this);


        txtLote       = findViewById(R.id.txtLote);
        txtData_val   = findViewById(R.id.txtData_val);
        txtFabricante = findViewById(R.id.txtFabricante);
        rgDose2       = findViewById(R.id.rgDose2);
        txtNomeRef    = findViewById(R.id.txtNomeRef);
        txtDataRef    = findViewById(R.id.txtDataRef);
        txtObsVac     = findViewById(R.id.txtObsVac);

        formVac2 = findViewById(R.id.formVac2);
    }

    @Override
    public void onClick(View view) {


        if (view.getId() == R.id.btnProximo2) {

            // Recupera o objeto Vacina montado no formVac1
            Vacina vacina = (Vacina) getIntent().getSerializableExtra("VACINA_OBJ");


            if (vacina == null) {
                Toast.makeText(this, "Erro ao carregar vacina!", Toast.LENGTH_SHORT).show();
                return;
            }


            int idSelecionadoVac = rgDose2.getCheckedRadioButtonId();


            if (idSelecionadoVac == -1) {
                Toast.makeText(this, "Selecione o tipo/dose da vacina!", Toast.LENGTH_SHORT).show();
                return;
            }


            RadioButton radioSelecionadoVac = findViewById(idSelecionadoVac);
            String dose2 = radioSelecionadoVac.getText().toString();

            String dataValidade = tratarDataParaEnvio(txtData_val.getText().toString());
            String dataReforco  = tratarDataParaEnvio(txtDataRef.getText().toString());

            // .trim() remove espaços extras no início e fim
            vacina.setLote(txtLote.getText().toString().trim());
            vacina.setData_val(dataValidade);
            vacina.setFabricante(txtFabricante.getText().toString().trim());
            vacina.setDose2(dose2);
            vacina.setNomeRef(txtNomeRef.getText().toString().trim());
            vacina.setDataRef(dataReforco);
            vacina.setObsVac(txtObsVac.getText().toString().trim());

            //  Navega para o formVac3 passando os objetos
            Intent intent = new Intent(this, form_vac3.class);
            intent.putExtra("VACINA_OBJ", vacina); // vacina com dados do form1 + form2
            intent.putExtra("PET_OBJETO", pet);     // pet dono da vacina
            startActivity(intent);
        }


        if (view.getId() == R.id.btVoltar2) {
            Intent form1 = new Intent(this, formVac1.class);
            form1.putExtra("PET_OBJETO", pet);
            startActivity(form1);
            finish();
        }


        if (view.getId() == R.id.btFechar2) {
            Intent main = new Intent(this, MainActivity.class);
            main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(main);
            finish();
        }
    }
    private String tratarDataParaEnvio(String dataInput) {
        if (dataInput == null || dataInput.trim().isEmpty()) {
            return null; // O banco de dados interpretará como campo vazio com sucesso
        }

        String dataLimpa = dataInput.trim();

        // Verifica se a data foi digitada com barras no formato brasileiro (ex: 20/07/2025)
        if (dataLimpa.contains("/")) {
            String[] partes = dataLimpa.split("/");
            if (partes.length == 3) {
                String dia = partes[0];
                String mes = partes[1];
                String ano = partes[2];

                // Remonta no formato do banco: AAAA-MM-DD
                return ano + "-" + mes + "-" + dia;
            }
        }

        return dataLimpa;
    }
}