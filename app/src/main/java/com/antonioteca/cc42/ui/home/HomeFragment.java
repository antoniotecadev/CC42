package com.antonioteca.cc42.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.antonioteca.cc42.databinding.FragmentHomeBinding;
import com.antonioteca.cc42.factory.EventViewModelFactory;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Event;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.repository.EventRepository;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.EventViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class HomeFragment extends Fragment {

    private EventViewModel eventViewModel;

    private FragmentHomeBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = requireContext();
        EventRepository eventRepository = new EventRepository(context);
        EventViewModelFactory eventViewModelFactory = new EventViewModelFactory(eventRepository);
        eventViewModel = new ViewModelProvider(this, eventViewModelFactory).get(EventViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Context context = requireContext();
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        User user = new User(context);
        user.coalition = new Coalition(context);

        int colorTextview = Color.parseColor(user.coalition.getColor());

        TextView textViewCoalition = binding.textViewCoalition;
        textViewCoalition.setText(user.coalition.getName());
        textViewCoalition.setTextColor(colorTextview);

        TextView textViewFullname = binding.textViewFullName;
        textViewFullname.setText(user.getDisplayName());
        textViewFullname.setTextColor(colorTextview);

        CollapsingToolbarLayout collapsingToolbarLayout = binding.collapsingToolbarLayout;
        collapsingToolbarLayout.setTitle(user.getLogin());
        String imageUrlCoalition = user.coalition.getImageUrl();
        if (imageUrlCoalition != null) {
            Glide.with(this)
                    .load(imageUrlCoalition)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            collapsingToolbarLayout.setBackground(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // Definir background placeholder caso a imagem não carregue
                        }
                    });
        }
        eventViewModel.getEvents(context);
        eventViewModel.getEvents().observe(getViewLifecycleOwner(), new Observer<Event>() {
            @Override
            public void onChanged(Event event) {
                Toast.makeText(context, "Eventos retornados", Toast.LENGTH_SHORT).show();
            }
        });
        eventViewModel.getHttpSatus().observe(getViewLifecycleOwner(), new Observer<HttpStatus>() {
            @Override
            public void onChanged(HttpStatus httpStatus) {
                Util.showAlertDialogBuild(String.valueOf(httpStatus.getCode()), httpStatus.getDescription(), context);
            }
        });
        eventViewModel.getHttpException().observe(getViewLifecycleOwner(), new Observer<HttpException>() {
            @Override
            public void onChanged(HttpException httpException) {
                Util.showAlertDialogBuild(String.valueOf(httpException.getCode()), httpException.getDescription(), context);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}