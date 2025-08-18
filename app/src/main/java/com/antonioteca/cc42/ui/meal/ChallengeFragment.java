package com.antonioteca.cc42.ui.meal;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentChallengeBinding;
import com.antonioteca.cc42.model.Question;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.utility.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.client.android.BeepManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChallengeFragment extends Fragment {

    private ValueEventListener valueEventListener;
    private FragmentChallengeBinding binding;
    private BeepManager beepManager;

    // UI
    private TextView tvCategory, tvDifficulty, tvQuestion, tvCode, tvTimer;
    private View codeContainer;
    private RadioGroup rgOptions;
    private RadioButton rbA, rbB, rbC, rbD;
    private Button btnSubmit;

    // Firebase
    private DatabaseReference questionsRef;
    private DatabaseReference gameRootRef;

    // Estado
    private Question currentQuestion;
    private String currentQuestionId;
    private CountDownTimer countDownTimer;
    private CountDownTimer lockCountDownTimer;
    private long startTimeMillis;
    private int countError = 0;
    private boolean locked = false;       // bloqueio ao errar
    private boolean awaitingNext = false; // botão em modo "Próximo
    private Context context;
    private String login;
    private User user;
    // Constantes
    // 10s

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        user = new User(context);
        beepManager = new BeepManager(requireActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChallengeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String mealId = ChallengeFragmentArgs.fromBundle(getArguments()).getMealId();
        login = user.getLogin();
        tvCategory = binding.textCategory;
        tvDifficulty = binding.textDifficulty;
        tvQuestion = binding.textQuestion;
        codeContainer = binding.codeContainer;
        tvCode = binding.textCode;
        tvTimer = binding.textTimer;
        rgOptions = binding.radioGroupOptions;
        rbA = binding.optionA;
        rbB = binding.optionB;
        rbC = binding.optionC;
        rbD = binding.optionD;
        btnSubmit = binding.buttonSubmit;

        questionsRef = FirebaseDatabase.getInstance().getReference("challenge").child("questions");
        gameRootRef = FirebaseDatabase.getInstance().getReference("challenge").child("meals").child(mealId);

        btnSubmit.setOnClickListener(v -> onPrimaryButtonClick());

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && binding != null && getContext() != null) {
                    boolean isStart = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                    if (!isStart) {
                        locked(true, false);
                        String message = getString(R.string.msg_sucess_challenge_finished_description);
                        Util.showAlertDialogMessage(context, getLayoutInflater(), getString(R.string.msg_sucess_challenge_finished), message, "#FDD835", user.getImage(), () -> {
                            requireActivity().onBackPressed();
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Util.showAlertDialogBuild(getString(R.string.err), error.getMessage(), context, null);
            }
        };
        gameRootRef.child("start_challenge").addValueEventListener(valueEventListener);

        loadRandomQuestion();
    }

    // --- UI helpers ---
    private void setOptionsEnabled(boolean enabled) {
        for (int i = 0; i < rgOptions.getChildCount(); i++) {
            rgOptions.getChildAt(i).setEnabled(enabled);
        }
    }

    private void resetButtonToSubmit() {
        awaitingNext = false;
        binding.textResult.setText("");
        btnSubmit.setText(login + " Responder ✅");
        btnSubmit.setEnabled(true);
    }

    private void switchButtonToNext() {
        awaitingNext = true;
        btnSubmit.setText(login + " Próximo ➡");
        btnSubmit.setEnabled(true);
    }

    private void onPrimaryButtonClick() {
        if (awaitingNext) {
            // carregar outra questão
            resetButtonToSubmit();
            loadRandomQuestion();
        } else {
            // estamos no modo "Responder"
            checkAnswer();
        }
    }

    private void toast(String m) {
        if (getContext() != null) Toast.makeText(getContext(), m, Toast.LENGTH_SHORT).show();
    }

    // --- Carregar questão aleatória ---
    private void loadRandomQuestion() {
        cancelTimerIfAny();
        locked = false;
        awaitingNext = false;

        tvTimer.setText("");
        tvQuestion.setText("Carregando desafio...");
        setOptionsEnabled(false);
        rgOptions.clearCheck();
        rbA.setText("…");
        rbB.setText("…");
        rbC.setText("…");
        rbD.setText("…");
        rbA.setTag(null);
        rbB.setTag(null);
        rbC.setTag(null);
        rbD.setTag(null);

        codeContainer.setVisibility(View.GONE);
        tvCode.setText("");

        questionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    toast("Sem questões disponíveis. ⚠");
                    return;
                }
                List<String> ids = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ids.add(ds.getKey());
                }
                currentQuestionId = ids.get(new Random().nextInt(ids.size()));
                currentQuestion = snapshot.child(currentQuestionId).getValue(Question.class);
                if (currentQuestion == null) {
                    toast("Erro ao ler questão. ❌");
                    return;
                }
                displayQuestion(currentQuestion);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toast("Erro Firebase: " + error.getMessage());
            }
        });
    }

    private void displayQuestion(Question q) {
        if (q.difficulty.equalsIgnoreCase("easy"))
            tvDifficulty.setTextColor(getResources().getColor(R.color.success));
        else if (q.difficulty.equalsIgnoreCase("medium"))
            tvDifficulty.setTextColor(getResources().getColor(R.color.warning));
        else if (q.difficulty.equalsIgnoreCase("hard"))
            tvDifficulty.setTextColor(getResources().getColor(R.color.error));
        else
            tvDifficulty.setTextColor(getResources().getColor(R.color.textColorPrimary));

        tvCategory.setText("Categoria: " + safe(q.category));
        tvDifficulty.setText("Dificuldade: " + safe(q.difficulty));
        tvQuestion.setText(safe(q.question));

        if (!TextUtils.isEmpty(q.code)) {
            codeContainer.setVisibility(View.VISIBLE);
            tvCode.setText(q.code);
        } else {
            codeContainer.setVisibility(View.GONE);
        }

        // Embaralhar visualmente as opções (mantendo tag com a letra original)
        List<Map.Entry<String, String>> entries = new ArrayList<>(q.options.entrySet());
        Collections.shuffle(entries, new Random());

        RadioButton[] btns = new RadioButton[]{rbA, rbB, rbC, rbD};
        for (int i = 0; i < btns.length; i++) {
            Map.Entry<String, String> e = entries.get(i);
            btns[i].setText(e.getValue());
            btns[i].setTag(e.getKey()); // "a","b","c","d"
        }

        setOptionsEnabled(true);
        resetButtonToSubmit();
        startTimer(Math.max(1, q.time_limit));
    }

    // --- Timer ---
    private void startTimer(int seconds) {
        cancelTimerIfAny();
        cancelLockTimerIfAny();
        startTimeMillis = System.currentTimeMillis();
        tvTimer.setTextColor(getResources().getColor(R.color.textColorPrimary));

        countDownTimer = new CountDownTimer(seconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Tempo: " + (millisUntilFinished / 1000) + "s 🕓");
            }

            @Override
            public void onFinish() {
                tvTimer.setText("Tempo esgotado! 🥵");
                setOptionsEnabled(false);
                btnSubmit.setEnabled(false);

                // registra timeout como incorreto (0 pts) e libera "Próximo"
                registerResult(false, seconds, true);
                switchButtonToNext();
            }
        }.start();
    }

    private void cancelTimerIfAny() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void cancelLockTimerIfAny() {
        if (lockCountDownTimer != null) {
            lockCountDownTimer.cancel();
            lockCountDownTimer = null;
        }
    }

    // --- Responder ---
    private void checkAnswer() {
        if (locked) {
            toast("Aguarde o desbloqueio para tentar novamente. 😴");
            return;
        }
        int selectedId = rgOptions.getCheckedRadioButtonId();
        if (selectedId == -1) {
            toast("Seleciona uma opção.");
            return;
        }
        RadioButton rb = rgOptions.findViewById(selectedId);
        Object tag = rb.getTag();
        if (tag == null) {
            toast("Opção inválida.");
            return;
        }
        String chosenLetter = tag.toString();

        cancelTimerIfAny();
        long timeTakenSec = Math.max(1, (System.currentTimeMillis() - startTimeMillis) / 1000L);

        boolean correct = chosenLetter.equals(currentQuestion.answer);

        if (correct) {
            toast("Acertou! Tempo: " + timeTakenSec + "s");
            binding.textResult.setText("Acertou! ✅ Tempo: " + timeTakenSec + "s 🕓");
            setOptionsEnabled(false);
            btnSubmit.setEnabled(false);
            registerResult(true, timeTakenSec, false);
            switchButtonToNext();
        } else {
            countError++;
            int timeLockedSeconds = (10 * countError);
            final String timeLockedMessage = getTimeLockedMessage(timeLockedSeconds);

            toast("Errou! Bloqueado por " + timeLockedMessage + ".");
            binding.textResult.setText("Errou! ❌ Bloqueado por " + timeLockedMessage + ". 🕓");
            registerResult(false, timeTakenSec, false);

            locked(true, false);
            startLockTimer(timeLockedSeconds); // << INICIA O TIMER DE BLOQUEIO AQUI

            // após bloqueio, permite tentar de novo A MESMA questão e reinicia o timer
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                locked(false, true);
                rgOptions.clearCheck();
                if (getContext() != null) { // Verifica se o fragmento ainda está anexado
                    binding.textResult.setText("");
                    tvTimer.setTextColor(getResources().getColor(R.color.textColorPrimary)); // Restaura cor normal
                    startTimer(Math.max(1, currentQuestion.time_limit));
                }
                cancelLockTimerIfAny(); // Garante que o timer visual de bloqueio seja parado se ainda estiver rodando
            }, (long) timeLockedSeconds * 1000); // Certifique-se de usar timeLockedSeconds aqui para o delay
        }
    }

    @NonNull
    private static String getTimeLockedMessage(int timeLockedSeconds) {
        String timeLockedMessage;

        if (timeLockedSeconds >= 60) {
            int minutes = timeLockedSeconds / 60;
            int seconds = timeLockedSeconds % 60;
            if (seconds == 0) {
                timeLockedMessage = minutes + (minutes == 1 ? " minuto" : " minutos");
            } else {
                timeLockedMessage = minutes + (minutes == 1 ? " minuto" : " minutos") + " e " + seconds + "s";
            }
        } else {
            timeLockedMessage = timeLockedSeconds + "s";
        }
        return timeLockedMessage;
    }

    private void locked(boolean locked, boolean enabled) {
        this.locked = locked;
        btnSubmit.setEnabled(enabled);
        setOptionsEnabled(enabled);
    }

    // Novo método para iniciar o timer de bloqueio
    private void startLockTimer(int lockSeconds) {
        cancelTimerIfAny(); // Garante que o timer da questão está parado
        cancelLockTimerIfAny(); // Cancela qualquer timer de bloqueio anterior
        tvTimer.setTextColor(getResources().getColor(R.color.error)); // Cor de erro/bloqueio

        lockCountDownTimer = new CountDownTimer(lockSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long remainingSeconds = millisUntilFinished / 1000;
                String timeLockedMessage;
                if (remainingSeconds >= 60) {
                    long minutes = remainingSeconds / 60;
                    long seconds = remainingSeconds % 60;
                    if (seconds == 0) {
                        timeLockedMessage = minutes + (minutes == 1 ? " minuto" : " minutos");
                    } else {
                        timeLockedMessage = minutes + (minutes == 1 ? " minuto" : " minutos") + " e " + seconds + "s";
                    }
                } else {
                    timeLockedMessage = remainingSeconds + "s";
                }
                tvTimer.setText("Bloqueado: " + timeLockedMessage + " ⏳");
            }

            @Override
            public void onFinish() {
                tvTimer.setText("Desbloqueado! ✅");
                tvTimer.setTextColor(getResources().getColor(R.color.success));
                // Não faz o rgOptions.clearCheck() nem startTimer aqui diretamente.
                // Isso será feito pelo postDelayed original em checkAnswer.
                // Apenas atualiza a UI para indicar que o bloqueio acabou.
                // A lógica de reativar os botões e o timer principal
                // continua no Handler.postDelayed do checkAnswer.
            }
        }.start();
    }

    // --- Registro de resultado / pontuação ---
    private void registerResult(boolean correct, long timeTakenSec, boolean timeout) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return; // pode exigir login antes de abrir o fragment

        String uid = user.getUid();
        int tl = currentQuestion != null ? currentQuestion.time_limit : 30;
        int points = computePoints(correct, timeTakenSec, tl, timeout);

        DatabaseReference userRef = gameRootRef.child("users").child(uid);
//        String pushId = userRef.child("history").push().getKey();
//        if (pushId == null) {
//            pushId = String.valueOf(System.currentTimeMillis());
//        }
//        Map<String, Object> entry = new HashMap<>();
//        entry.put("questionId", currentQuestionId);
//        entry.put("category", currentQuestion != null ? currentQuestion.category : null);
//        entry.put("correct", correct);
//        entry.put("timeout", timeout);
//        entry.put("timeTaken", timeTakenSec);
//        entry.put("pointsEarned", points);
//        entry.put("ts", ServerValue.TIMESTAMP);

//        String finalPushId = pushId;
//        userRef.child("history").child(pushId).setValue(entry)
//                .addOnSuccessListener(aVoid -> Log.d("FirebaseDB", "Entrada de histórico salva com sucesso em: " + userRef.child("history").child(finalPushId).toString()))
//                .addOnFailureListener(e -> Log.e("FirebaseDB", "Falha ao salvar entrada de histórico em: " + userRef.child("history").child(finalPushId).toString(), e));
//        userRef.child("lastChallenge").setValue(currentQuestionId)
//                .addOnSuccessListener(aVoid -> Log.d("FirebaseDB", "lastChallenge salvo com sucesso."))
//                .addOnFailureListener(e -> Log.e("FirebaseDB", "Falha ao salvar lastChallenge.", e));

        // soma pontuação
        if (correct) {
            userRef.child("totalScore").runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Long cur = currentData.getValue(Long.class);
                    if (cur == null) cur = 0L;
                    beepManager.playBeepSoundAndVibrate();
                    currentData.setValue(cur + points);
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    if (error != null) {
                        toast("Erro na transação totalScore: " + error.toException());
                    } else {
                        Log.d("FirebaseDB", "Transação totalScore " + (committed ? "commitada" : "não commitada"));
                    }
                }
            });
        } else
            Util.startVibration(context);

        // incrementa tentativas
        userRef.child("attempts").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Long cur = currentData.getValue(Long.class);
                if (cur == null) cur = 0L;
                currentData.setValue(cur + 1);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    toast("Erro na transação attempts: " + error.toException());
                } else {
                    Log.d("FirebaseDB", "Transação attempts " + (committed ? "commitada" : "não commitada"));
                }
            }
        });
    }

//    Como Mandar Bem no Desafio e Ganhar Mais Pontos! 🚀 Olá, pessoal! Queremos que vocês se divirtam e aprendam com o nosso desafio. Para deixar a competição ainda mais emocionante e justa, ajustamos um pouquinho como os pontos são calculados. É bem simples de entender: O Segredo é: Acertar Rápido! ⚡
//    Vale Muitos Pontos Acertar: Cada questão que você acerta já te dá uma boa quantidade de pontos. O objetivo principal é sempre tentar responder corretamente!
//    Quanto Mais Rápido, Mais Pontos Extras:
//    Imagine que cada pergunta tem um "bolão" máximo de pontos esperando por você assim que ela aparece.
//    Se você responder bem rapidinho, quase no instante em que a pergunta surge, você leva quase o bolão todo!
//    A cada segundinho que passa antes de você responder, um pouquinho desses pontos do "bolão" vai diminuindo.
//    Então, mesmo que você demore um pouco mais (mas ainda acerte dentro do tempo limite!), você ainda ganha pontos, só que um pouco menos do que quem respondeu super rápido. Em Resumo:
//            ✅ Acerte a pergunta: Isso é o mais importante!
//            ⏱️ Seja Ágil: Tente responder o mais rápido que conseguir, pensando bem, claro! Responder rápido te dá um SUPER BÔNUS nos pontos. O que NÃO dá pontos:
//            ❌ Errar a resposta.
//⏰ Deixar o tempo acabar sem responder. Dica Extra: Não se preocupe em ser o mais rápido do universo em TODAS as perguntas. O mais importante é acertar. Mas, se você souber a resposta, não demore muito para clicar, porque cada segundo vale ouro para a sua pontuação! 😉 O objetivo é simples: Mostre que você sabe e tente ser rápido nisso! Boa sorte a todos e que vençam os melhores (e mais ágeis)! 🎉

    private int computePoints(boolean correct, long timeTakenSec, int timeLimit, boolean timeout) {
        if (!correct || timeout) return 0;

        int actualTimeLimit = Math.max(1, timeLimit);
        long timeTaken = Math.max(1, timeTakenSec); // Garante que é pelo menos 1 segundo

        // Defina uma pontuação máxima alta para a questão
        // e uma pontuação mínima para quem acerta no último segundo.
        int maxPossiblePoints = 5000; // Ex: 5000 pontos se responder no primeiro segundo
        int minPossiblePoints = 100;  // Ex: 100 pontos se responder no último segundo permitido

        if (timeTaken > actualTimeLimit) { // Segurança extra, embora o timer deva parar
            return 0;
        }

        // Calcular quantos "passos" de tempo existem
        int totalTimeSteps = actualTimeLimit; // Se cada segundo é um passo

        // Calcular quantos pontos são "perdidos" por passo de tempo,
        // de forma que no último passo ainda se tenha minPossiblePoints.
        // (maxPossiblePoints - minPossiblePoints) é a faixa de pontos que varia com o tempo.
        // (totalTimeSteps - 1) é o número de "decaimentos" de pontos possíveis do primeiro ao último segundo.
        double pointsLostPerStep;
        if (totalTimeSteps > 1) {
            pointsLostPerStep = (double) (maxPossiblePoints - minPossiblePoints) / (totalTimeSteps - 1);
        } else { // Se o timeLimit for 1 segundo
            return maxPossiblePoints; // Ou alguma outra lógica para timeLimit=1
        }

        // Pontuação = Máxima - (quantos passos de tempo se passaram * perda por passo)
        // (timeTaken - 1) porque no primeiro segundo (timeTaken=1) não se perde nada.
        int score = (int) (maxPossiblePoints - ((timeTaken - 1) * pointsLostPerStep));

        // Garante que não caia abaixo do mínimo (pode acontecer por arredondamento do double)
        // e também não exceda o máximo (improvável aqui, mas boa prática)
        return Math.max(minPossiblePoints, Math.min(score, maxPossiblePoints));
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        cancelTimerIfAny();
        cancelLockTimerIfAny();
        if (gameRootRef != null && valueEventListener != null)
            gameRootRef.removeEventListener(valueEventListener);
    }
}
