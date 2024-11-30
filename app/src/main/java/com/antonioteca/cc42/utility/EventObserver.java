package com.antonioteca.cc42.utility;

public class EventObserver<T> {
    private final T content;
    private boolean hasBeenHandled = false;

    public EventObserver(T content) {
        this.content = content;
    }

    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    public T peekContent() {
        return content;
    }
}