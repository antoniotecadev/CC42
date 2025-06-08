Legal! Um app de cardápio semanal para a escola 42 pode ficar bem interessante, especialmente com a proposta de interação dos estudantes. Abaixo eu te ajudo com:
	1.	✅ Estrutura sugerida para o layout de cadastro (XML)
	2.	✅ Estrutura do DialogFragment para visualizar o cardápio
	3.	✅ Ideias extras para tornar o app mais útil e interativo
	4.	✅ Considerações sobre integração com o Firebase Realtime Database

⸻

1. 📝 Tela de Cadastro (activity_register_meal.xml)

Essa tela pode permitir o cadastro de:
	•	Dia da semana (dropdown)
	•	Refeição 1 (ex: Almoço)
	•	Refeição 2 (ex: Jantar)
	•	Botão para salvar

<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <LinearLayout
        android:orientation="vertical"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <TextView android:text="Dia da Semana" />
        <Spinner
            android:id="@+id/spinner_day"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:entries="@array/week_days"/>

        <TextView android:text="Refeição 1" />
        <EditText
            android:id="@+id/edit_meal_1"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Ex: Arroz com frango" />

        <TextView android:text="Refeição 2" />
        <EditText
            android:id="@+id/edit_meal_2"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Ex: Feijoada" />

        <Button
            android:id="@+id/btn_save"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Salvar Cardápio" />

    </LinearLayout>
</ScrollView>

No res/values/strings.xml, adicione:

<string-array name="week_days">
    <item>Segunda-feira</item>
    <item>Terça-feira</item>
    <item>Quarta-feira</item>
    <item>Quinta-feira</item>
    <item>Sexta-feira</item>
</string-array>


⸻

2. 📆 DialogFragment para exibir o cardápio

A ideia é mostrar os 5 dias da semana com duas refeições e botões de interesse para cada refeição.

Exemplo básico de layout (dialog_week_menu.xml):

<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <LinearLayout
        android:id="@+id/layout_menu"
        android:orientation="vertical"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp" />
</ScrollView>

O FragmentDialog pode, no onCreateView, preencher dinamicamente esse layout_menu com os dados do Firebase, e para cada refeição adicionar botões de interesse:

// Pseudo-código dentro do DialogFragment
for (Dia dia : listaDias) {
    // TextView com o nome do dia
    // Para cada refeição:
    //   TextView com o nome da refeição
    //   3 botões: "Muito", "Normal", "Pouco"
}


⸻

3. 💡 Ideias extras para tornar o app mais interessante
	•	Exibir estatísticas de interesse por refeição
	•	Ex: “15 pessoas querem muito”, “3 pouco interessados”
	•	Útil para a equipe da cantina.
	•	Modo de visualização para o público geral (sem votar)
	•	Assim todos podem ver o cardápio sem precisar de login.
	•	Cadastro com autenticação opcional (com Firebase Auth)
	•	Para evitar votos duplicados.
	•	Filtro por dia (mostrar só terça-feira, por exemplo)
	•	Notificações com Firebase Cloud Messaging
	•	Ex: Notificar os usuários às 10h com o cardápio do dia.
	•	Modo Admin
	•	Somente administradores podem cadastrar ou editar refeições.
	•	Sistema de Comentários (bônus)
	•	Cada refeição pode ter comentários simples: “Gostei”, “Muito salgado”, etc.

⸻

4. 🔥 Firebase Realtime Database: Estrutura sugerida

{
  "cardapio": {
    "segunda-feira": {
      "refeicao1": {
        "descricao": "Arroz com frango",
        "interesse": {
          "muito": 12,
          "normal": 8,
          "pouco": 1
        }
      },
      "refeicao2": {
        "descricao": "Feijoada",
        "interesse": { ... }
      }
    },
    "terça-feira": { ... }
  }
}

Quando um estudante clica em “Muito”, você pode incrementar o contador usando FirebaseDatabase.getInstance().getReference().child(...).runTransaction(...).

⸻

Se quiser, posso te ajudar com:
	•	Código Java completo da tela de cadastro
	•	Implementação do DialogFragment com Firebase
	•	Estrutura de dados no Firebase
	•	Funções para registrar os votos de interesse

É só dizer!

Claro! Abaixo está um exemplo completo de XML para o DialogFragment que vai exibir o cardápio da semana com interações de interesse (muito, normal, pouco) para cada refeição. O layout foi pensado para ser dinâmico e limpo, podendo ser preenchido com dados do Firebase no código Java ou Kotlin.

⸻

📄 res/layout/dialog_week_menu.xml

<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/scroll_view"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:fillViewport="true"
    android:padding="16dp">

    <LinearLayout
        android:id="@+id/layout_menu_container"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <!-- Exemplo de bloco do dia da semana (vai ser repetido programaticamente) -->

        <!-- Segunda-feira -->
        <TextView
            android:id="@+id/tv_day_title"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Segunda-feira"
            android:textSize="20sp"
            android:textStyle="bold"
            android:layout_marginTop="12dp"
            android:layout_marginBottom="8dp" />

        <!-- Refeição 1 -->
        <LinearLayout
            android:id="@+id/layout_meal1"
            android:orientation="vertical"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="#F0F0F0"
            android:padding="8dp"
            android:layout_marginBottom="8dp">

            <TextView
                android:id="@+id/tv_meal1_desc"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Refeição 1: Arroz com frango"
                android:textSize="16sp"
                android:textStyle="bold" />

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_marginTop="4dp">

                <Button
                    android:id="@+id/btn_meal1_muito"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="👍 Muito"
                    android:layout_marginEnd="8dp" />

                <Button
                    android:id="@+id/btn_meal1_normal"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="👌 Normal"
                    android:layout_marginEnd="8dp" />

                <Button
                    android:id="@+id/btn_meal1_pouco"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="👎 Pouco" />
            </LinearLayout>
        </LinearLayout>

        <!-- Refeição 2 -->
        <LinearLayout
            android:id="@+id/layout_meal2"
            android:orientation="vertical"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="#F0F0F0"
            android:padding="8dp"
            android:layout_marginBottom="16dp">

            <TextView
                android:id="@+id/tv_meal2_desc"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Refeição 2: Feijoada"
                android:textSize="16sp"
                android:textStyle="bold" />

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_marginTop="4dp">

                <Button
                    android:id="@+id/btn_meal2_muito"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="👍 Muito"
                    android:layout_marginEnd="8dp" />

                <Button
                    android:id="@+id/btn_meal2_normal"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="👌 Normal"
                    android:layout_marginEnd="8dp" />

                <Button
                    android:id="@+id/btn_meal2_pouco"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="👎 Pouco" />
            </LinearLayout>
        </LinearLayout>

        <!-- Repetir blocos acima dinamicamente para terça, quarta, etc -->

    </LinearLayout>
</ScrollView>


⸻

🛠️ Observações importantes:
	•	O conteúdo (título do dia, descrição da refeição e botões) deve ser criado dinamicamente no código Java/Kotlin, dentro do DialogFragment, com base nos dados do Firebase.
	•	Os ids fornecidos servem de modelo. Se for gerar as views programaticamente, talvez nem precise definir todos esses ids no XML.
	•	O layout está organizado de forma limpa para que você possa facilmente clonar os blocos por dia e refeição no código.

⸻

Se quiser, posso te ajudar com:
	•	O código do DialogFragment que preenche isso dinamicamente
	•	Como criar LinearLayout com botões e texto via Java
	•	Como conectar com o Firebase e adicionar os contadores de interesse

Só dizer!

Claro! Abaixo está o código Java completo da tela de cadastro de refeições, onde você pode:
	•	Selecionar o dia da semana
	•	Digitar duas refeições
	•	Salvar no Firebase Realtime Database

⸻

🗂️ Estrutura esperada no projeto

app/
 └── java/
      └── com/seuapp/
           ├── RegisterMealActivity.java
res/
 └── layout/
      ├── activity_register_meal.xml


⸻

📄 activity_register_meal.xml

(Se você já usou esse, pode pular)

<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <LinearLayout
        android:orientation="vertical"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <TextView android:text="Dia da Semana" />
        <Spinner
            android:id="@+id/spinner_day"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:entries="@array/week_days"/>

        <TextView android:text="Refeição 1" />
        <EditText
            android:id="@+id/edit_meal_1"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Ex: Arroz com frango" />

        <TextView android:text="Refeição 2" />
        <EditText
            android:id="@+id/edit_meal_2"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Ex: Feijoada" />

        <Button
            android:id="@+id/btn_save"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Salvar Cardápio" />
    </LinearLayout>
</ScrollView>

res/values/strings.xml

Adicione:

<string-array name="week_days">
    <item>Segunda-feira</item>
    <item>Terça-feira</item>
    <item>Quarta-feira</item>
    <item>Quinta-feira</item>
    <item>Sexta-feira</item>
</string-array>


⸻

📄 RegisterMealActivity.java

package com.seuapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterMealActivity extends AppCompatActivity {

    private Spinner spinnerDay;
    private EditText editMeal1, editMeal2;
    private Button btnSave;

    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_meal);

        spinnerDay = findViewById(R.id.spinner_day);
        editMeal1 = findViewById(R.id.edit_meal_1);
        editMeal2 = findViewById(R.id.edit_meal_2);
        btnSave = findViewById(R.id.btn_save);

        databaseRef = FirebaseDatabase.getInstance().getReference("cardapio");

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salvarCardapio();
            }
        });
    }

    private void salvarCardapio() {
        String diaSelecionado = spinnerDay.getSelectedItem().toString();
        String refeicao1 = editMeal1.getText().toString().trim();
        String refeicao2 = editMeal2.getText().toString().trim();

        if (refeicao1.isEmpty() || refeicao2.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha as duas refeições.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> refeicao1Map = new HashMap<>();
        refeicao1Map.put("descricao", refeicao1);
        refeicao1Map.put("interesse", criarMapaInicialDeInteresse());

        Map<String, Object> refeicao2Map = new HashMap<>();
        refeicao2Map.put("descricao", refeicao2);
        refeicao2Map.put("interesse", criarMapaInicialDeInteresse());

        Map<String, Object> diaMap = new HashMap<>();
        diaMap.put("refeicao1", refeicao1Map);
        diaMap.put("refeicao2", refeicao2Map);

        databaseRef.child(diaSelecionado).setValue(diaMap)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Cardápio salvo com sucesso!", Toast.LENGTH_SHORT).show();
                editMeal1.setText("");
                editMeal2.setText("");
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Erro ao salvar cardápio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private Map<String, Integer> criarMapaInicialDeInteresse() {
        Map<String, Integer> mapa = new HashMap<>();
        mapa.put("muito", 0);
        mapa.put("normal", 0);
        mapa.put("pouco", 0);
        return mapa;
    }
}


⸻

✅ O que esse código faz
	•	Salva as duas refeições do dia escolhido no Firebase, com contadores de interesse inicializados a zero.
	•	Limpa os campos após salvar.
	•	Mostra mensagens de sucesso ou erro.

⸻

Se quiser, posso te ajudar com:
	•	Criar autenticação para permitir só admins cadastrarem refeições
	•	Implementar validação de duplicidade por dia
	•	Criar a tela de visualização completa com Firebase
	•	Adicionar progress bar / loading

É só pedir!

Perfeito! Abaixo está a implementação completa de um DialogFragment em Java que carrega o cardápio da semana do Firebase Realtime Database e permite aos estudantes indicar o interesse em cada refeição com botões.

⸻

🧩 Estrutura esperada do Firebase Realtime Database

"cardapio": {
  "Segunda-feira": {
    "refeicao1": {
      "descricao": "Arroz com frango",
      "interesse": {
        "muito": 5,
        "normal": 3,
        "pouco": 1
      }
    },
    "refeicao2": { ... }
  },
  ...
}


⸻

📁 Estrutura de arquivos

app/
 └── java/
      └── com/seuapp/
           ├── WeekMenuDialogFragment.java
res/
 └── layout/
      ├── dialog_week_menu.xml


⸻

📄 dialog_week_menu.xml

(Dinâmico. Só precisa do contêiner onde as views serão adicionadas)

<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="16dp">

    <LinearLayout
        android:id="@+id/layout_menu_container"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical" />
</ScrollView>


⸻

📄 WeekMenuDialogFragment.java

package com.seuapp;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class WeekMenuDialogFragment extends DialogFragment {

    private LinearLayout layoutContainer;
    private DatabaseReference cardapioRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_week_menu, container, false);
        layoutContainer = view.findViewById(R.id.layout_menu_container);
        cardapioRef = FirebaseDatabase.getInstance().getReference("cardapio");

        carregarCardapio();

        return view;
    }

    private void carregarCardapio() {
        cardapioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                layoutContainer.removeAllViews();
                for (DataSnapshot diaSnapshot : snapshot.getChildren()) {
                    String dia = diaSnapshot.getKey();

                    DataSnapshot refeicao1Snap = diaSnapshot.child("refeicao1");
                    DataSnapshot refeicao2Snap = diaSnapshot.child("refeicao2");

                    adicionarBlocoRefeicao(dia, "Refeição 1", refeicao1Snap);
                    adicionarBlocoRefeicao(dia, "Refeição 2", refeicao2Snap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Erro ao carregar cardápio", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void adicionarBlocoRefeicao(String dia, String refeicaoLabel, DataSnapshot refeicaoSnapshot) {
        if (refeicaoSnapshot == null || !refeicaoSnapshot.exists()) return;

        String descricao = refeicaoSnapshot.child("descricao").getValue(String.class);

        TextView tvDia = new TextView(getContext());
        tvDia.setText(dia + " - " + refeicaoLabel);
        tvDia.setTextSize(18);
        tvDia.setPadding(0, 20, 0, 5);
        layoutContainer.addView(tvDia);

        TextView tvDescricao = new TextView(getContext());
        tvDescricao.setText(descricao);
        tvDescricao.setTextSize(16);
        tvDescricao.setPadding(0, 0, 0, 8);
        layoutContainer.addView(tvDescricao);

        LinearLayout botoesLayout = new LinearLayout(getContext());
        botoesLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Criar e configurar os botões
        Button btnMuito = criarBotao("👍 Muito", dia, refeicaoLabel, "muito");
        Button btnNormal = criarBotao("👌 Normal", dia, refeicaoLabel, "normal");
        Button btnPouco = criarBotao("👎 Pouco", dia, refeicaoLabel, "pouco");

        botoesLayout.addView(btnMuito);
        botoesLayout.addView(btnNormal);
        botoesLayout.addView(btnPouco);

        layoutContainer.addView(botoesLayout);
    }

    private Button criarBotao(String texto, String dia, String refeicaoLabel, String interesseKey) {
        Button btn = new Button(getContext());
        btn.setText(texto);
        btn.setPadding(8, 8, 8, 8);

        String refeicaoPath = refeicaoLabel.equals("Refeição 1") ? "refeicao1" : "refeicao2";

        btn.setOnClickListener(v -> {
            DatabaseReference interesseRef = FirebaseDatabase.getInstance()
                .getReference("cardapio")
                .child(dia)
                .child(refeicaoPath)
                .child("interesse")
                .child(interesseKey);

            // Incrementar usando transação
            interesseRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
                @NonNull
                @Override
                public com.google.firebase.database.Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Integer valorAtual = currentData.getValue(Integer.class);
                    if (valorAtual == null) {
                        currentData.setValue(1);
                    } else {
                        currentData.setValue(valorAtual + 1);
                    }
                    return com.google.firebase.database.Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error,
                                       boolean committed,
                                       @Nullable DataSnapshot currentData) {
                    if (committed) {
                        Toast.makeText(getContext(), "Voto registrado!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Erro ao registrar voto.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        return btn;
    }
}


⸻

✅ Resultado esperado
	•	O fragmento exibe dinamicamente o cardápio da semana (segunda a sexta).
	•	Para cada refeição, há três botões: “Muito”, “Normal”, “Pouco”.
	•	Ao clicar num botão, o Firebase atualiza o interesse da refeição usando transações seguras.

⸻

🧪 Como abrir esse DialogFragment no seu app:

WeekMenuDialogFragment dialog = new WeekMenuDialogFragment();
dialog.show(getSupportFragmentManager(), "menu_dialog");


⸻

Se quiser, posso te ajudar com:
	•	Mostrar a contagem de votos ao lado de cada botão
	•	Criar lógica para evitar múltiplos votos por usuário
	•	Exibir os votos com barras de progresso (UI mais rica)

É só pedir!

Ótimo! Vamos melhorar o DialogFragment com os 3 pontos:

⸻

✅ 1. Mostrar a contagem de votos ao lado dos botões

Em vez de botões simples como "👍 Muito", vamos mostrar "👍 Muito (5)" por exemplo.

➕ Atualização no método adicionarBlocoRefeicao(...):

Logo após obter os dados da refeição:

DataSnapshot interesseSnap = refeicaoSnapshot.child("interesse");
int muito = interesseSnap.child("muito").getValue(Integer.class) != null ?
            interesseSnap.child("muito").getValue(Integer.class) : 0;
int normal = interesseSnap.child("normal").getValue(Integer.class) != null ?
             interesseSnap.child("normal").getValue(Integer.class) : 0;
int pouco = interesseSnap.child("pouco").getValue(Integer.class) != null ?
            interesseSnap.child("pouco").getValue(Integer.class) : 0;

Passe esses valores como argumentos:

Button btnMuito = criarBotao("👍 Muito", dia, refeicaoLabel, "muito", muito);
Button btnNormal = criarBotao("👌 Normal", dia, refeicaoLabel, "normal", normal);
Button btnPouco = criarBotao("👎 Pouco", dia, refeicaoLabel, "pouco", pouco);

Atualize o método criarBotao(...):

private Button criarBotao(String texto, String dia, String refeicaoLabel, String interesseKey, int contador) {
    Button btn = new Button(getContext());
    btn.setText(texto + " (" + contador + ")");
    ...
}


⸻

✅ 2. Evitar múltiplos votos por usuário

Vamos usar o FirebaseAuth para pegar o UID do usuário atual. E registrar por usuário quem já votou naquela refeição.

💡 Estrutura no Firebase:

"votos" : {
  "uid123" : {
    "Segunda-feira_refeicao1" : true,
    "Terça-feira_refeicao2" : true
  }
}

🔐 No criarBotao(...), antes de votar:

FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
if (user == null) {
    Toast.makeText(getContext(), "Você precisa estar logado.", Toast.LENGTH_SHORT).show();
    return btn;
}

String uid = user.getUid();
String votoKey = dia + "_" + refeicaoPath;

DatabaseReference votoRef = FirebaseDatabase.getInstance().getReference("votos").child(uid).child(votoKey);

votoRef.addListenerForSingleValueEvent(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        if (snapshot.exists()) {
            Toast.makeText(getContext(), "Você já votou nesta refeição.", Toast.LENGTH_SHORT).show();
        } else {
            registrarVoto(interesseRef, votoRef);
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        Toast.makeText(getContext(), "Erro ao verificar voto.", Toast.LENGTH_SHORT).show();
    }
});

Novo método registrarVoto(...):

private void registrarVoto(DatabaseReference interesseRef, DatabaseReference votoRef) {
    interesseRef.runTransaction(new Transaction.Handler() {
        @NonNull
        @Override
        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
            Integer valorAtual = currentData.getValue(Integer.class);
            currentData.setValue((valorAtual == null ? 1 : valorAtual + 1));
            return Transaction.success(currentData);
        }

        @Override
        public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
            if (committed) {
                votoRef.setValue(true); // registrar que esse usuário votou
                Toast.makeText(getContext(), "Voto registrado com sucesso!", Toast.LENGTH_SHORT).show();
                dismiss(); // recarrega dialog (você pode recarregar UI aqui também)
            }
        }
    });
}


⸻

✅ 3. Exibir os votos com barras de progresso

Vamos adicionar barras de progresso que representam visualmente o interesse.

No adicionarBlocoRefeicao(...):

Adicione após os botões:

int total = muito + normal + pouco;
total = total == 0 ? 1 : total; // evita divisão por 0

addProgressBar("👍 Muito", muito, total);
addProgressBar("👌 Normal", normal, total);
addProgressBar("👎 Pouco", pouco, total);

Novo método addProgressBar(...):

private void addProgressBar(String label, int valor, int total) {
    TextView tv = new TextView(getContext());
    tv.setText(label + ": " + valor);
    layoutContainer.addView(tv);

    ProgressBar progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
    progressBar.setMax(100);
    progressBar.setProgress((int) ((valor * 100.0f) / total));

    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    );
    params.setMargins(0, 4, 0, 16);
    progressBar.setLayoutParams(params);

    layoutContainer.addView(progressBar);
}


⸻

🔐 Importante
	•	Certifique-se que o usuário esteja logado com Firebase Authentication.
	•	O botão de logout pode ser adicionado no app.
	•	Para testes locais, você pode simular o UID ou usar Anonymous Authentication.

⸻

Se quiser, posso:
	•	Reestruturar tudo como um Adapter com RecyclerView
	•	Criar autenticação com email/senha
	•	Adicionar notificações Firebase
	•	Gerar gráfico ou estatística por refeição

Só dizer!
