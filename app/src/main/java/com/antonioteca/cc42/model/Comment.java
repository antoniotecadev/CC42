package com.antonioteca.cc42.model;

public class Comment {

    private String comment;
    private boolean isAnonymous;

    public Comment() {
        // Construtor padrão necessário para o Firebase
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }
}
