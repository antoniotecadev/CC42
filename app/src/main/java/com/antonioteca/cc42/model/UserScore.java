package com.antonioteca.cc42.model;

public class UserScore {
    String userId; // ou o que for "intra42:xxxx"
    long totalScore;
    long attempts;

    // Construtor vazio necessário para o Firebase
    public UserScore() {
    }

    public UserScore(String userId, long totalScore, long attempts) {
        this.userId = userId;
        this.totalScore = totalScore;
        this.attempts = attempts;
    }

    public String getUserId() {
        return userId;
    }

    public long getTotalScore() {
        return totalScore;
    }

    public long getAttempts() {
        return attempts;
    }

    // Opcional: para facilitar a depuração
    @Override
    public String toString() {
        return "UserScore{" +
                "userId='" + userId + '\'' +
                ", totalScore=" + totalScore +
                ", attempts=" + attempts +
                '}';
    }
}
