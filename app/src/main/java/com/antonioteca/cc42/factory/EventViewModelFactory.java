package com.antonioteca.cc42.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.antonioteca.cc42.repository.EventRepository;
import com.antonioteca.cc42.viewmodel.EventViewModel;

public class EventViewModelFactory implements ViewModelProvider.Factory {

    private final EventRepository eventRepository;

    public EventViewModelFactory(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EventViewModel.class)) {
            return (T) new EventViewModel(eventRepository);
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
