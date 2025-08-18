package com.antonioteca.cc42.model;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardFetcher {
    private final DatabaseReference databaseReference;

    public LeaderboardFetcher() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public interface LeaderboardCallback {
        void onLeaderboardFetched(List<UserScore> topUsers);

        void onError(String errorMessage);
    }

    public void fetchTopUsersForMeal(String mealId, int numberOfMealsAvailable, LeaderboardCallback callback) {
        if (mealId == null || mealId.isEmpty()) {
            callback.onError("Meal ID não pode ser nulo ou vazio.");
            return;
        }
        if (numberOfMealsAvailable <= 0) {
            callback.onError("Número de refeições disponíveis deve ser maior que zero.");
            return;
        }

        DatabaseReference usersRef = databaseReference.child("challenge")
                .child("meals")
                .child(mealId)
                .child("users");

        // 1. Criar a consulta: Ordenar por "totalScore" em ordem decrescente e limitar ao número de refeições
        // O Firebase Realtime Database ordena em ordem crescente por padrão.
        // Para obter decrescente, recuperamos todos, ordenamos no cliente e depois pegamos os N primeiros.
        // Ou, se o número de usuários for muito grande, você pode usar orderByChild e pegar os "last N"
        // depois de inverter a ordem no cliente, ou pegar os "first N" se você armazenar scores negativos (não ideal).
        // A forma mais direta para "top N" com ordenação decrescente é ordenar no cliente após buscar.

        // Abordagem mais simples se o número de usuários por meal não for excessivamente grande (ex: algumas centenas):
        // Buscar todos os usuários e ordenar/limitar no cliente.
        Query query = usersRef.orderByChild("totalScore"); // Ordena por totalScore ASCENDENTE

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<UserScore> allUsers = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // O 'key' do snapshot será o ID do usuário (ex: "intra42:228901")
                        String userId = userSnapshot.getKey();
                        Long score = userSnapshot.child("totalScore").getValue(Long.class);
                        Long attempts = userSnapshot.child("attempts").getValue(Long.class);

                        if (userId != null && score != null && attempts != null) {
                            allUsers.add(new UserScore(userId.replace("intra42:", ""), score, attempts));
                        }
                    }

                    // ordene a lista em ordem DECRESCENTE de totalScore
                    // Collections.sort(allUsers, (u1, u2) -> Long.compare(u2.getTotalScore(), u1.getTotalScore()));
                    // Se houver empate no totalScore, você pode adicionar um critério de desempate aqui,
                    // por exemplo, menor número de tentativas:
                    Collections.sort(allUsers, (u1, u2) -> {
                        int scoreCompare = Long.compare(u2.getTotalScore(), u1.getTotalScore());
                        if (scoreCompare == 0) {
                            return Long.compare(u1.getAttempts(), u2.getAttempts()); // Menor tentativa primeiro
                        }
                        return scoreCompare;
                    });


                    // Pegar os primeiros 'numberOfMealsAvailable' usuários
                    List<UserScore> topUsers = new ArrayList<>();
                    for (int i = 0; i < Math.min(allUsers.size(), numberOfMealsAvailable); i++) {
                        topUsers.add(allUsers.get(i));
                    }
                    callback.onLeaderboardFetched(topUsers);

                } else {
                    callback.onLeaderboardFetched(new ArrayList<>()); // Nenhum usuário encontrado
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }
}
