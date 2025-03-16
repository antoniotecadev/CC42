package com.antonioteca.cc42.ui.meal;

public class RatingProgressItem {
    private final int starIcon; // Ícone da estrela
    private final int progress; // Porcentagem de avaliações

    public RatingProgressItem(int starIcon, int progress) {
        this.starIcon = starIcon;
        this.progress = progress;
    }

    public int getStarIcon() {
        return starIcon;
    }

    public int getProgress() {
        return progress;
    }
}
