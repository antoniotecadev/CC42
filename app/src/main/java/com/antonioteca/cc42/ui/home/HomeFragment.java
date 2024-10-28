package com.antonioteca.cc42.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentHomeBinding;
import com.antonioteca.cc42.factory.EventViewModelFactory;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Event;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.repository.EventRepository;
import com.antonioteca.cc42.utility.EventAdapter;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.EventViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.List;

public class HomeFragment extends Fragment {

    private Context context;

    private EventAdapter eventAdapter;
    private EventViewModel eventViewModel;

    private FragmentHomeBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        EventRepository eventRepository = new EventRepository(context);
        EventViewModelFactory eventViewModelFactory = new EventViewModelFactory(eventRepository);
        eventViewModel = new ViewModelProvider(this, eventViewModelFactory).get(EventViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.recyclerviewEventsList.setHasFixedSize(true);
        binding.recyclerviewEventsList.setLayoutManager(new LinearLayoutManager(context));

        User user = new User(context);
        user.coalition = new Coalition(context);

        binding.textViewCoalition.setText(user.coalition.getName());
        binding.textViewFullName.setText(user.getDisplayName());

        String colorText = user.coalition.getColor();
        if (colorText != null) {
            int colorTextview = Color.parseColor(colorText);
            binding.textViewCoalition.setTextColor(colorTextview);
            binding.textViewFullName.setTextColor(colorTextview);
        }

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
        eventViewModel.getEventsList(context, binding.progressBar).observe(getViewLifecycleOwner(), new Observer<List<Event>>() {
            @Override
            public void onChanged(List<Event> eventList) {
                binding.progressBar.setVisibility(View.GONE);
                if (eventList.get(0) != null) {
                    eventAdapter = new EventAdapter(eventList);
                    binding.recyclerviewEventsList.setAdapter(eventAdapter);
                    // Aplicar a animação de layout
                    // runLayoutAnimation(binding.recyclerviewEventsList, context);
                }
            }
        });
        eventViewModel.getHttpSatus().observe(getViewLifecycleOwner(), new Observer<HttpStatus>() {
            @Override
            public void onChanged(HttpStatus httpStatus) {
                binding.progressBar.setVisibility(View.GONE);
                Util.showAlertDialogBuild(String.valueOf(httpStatus.getCode()), httpStatus.getDescription(), context);
            }
        });
        eventViewModel.getHttpException().observe(getViewLifecycleOwner(), new Observer<HttpException>() {
            @Override
            public void onChanged(HttpException httpException) {
                binding.progressBar.setVisibility(View.GONE);
                Util.showAlertDialogBuild(String.valueOf(httpException.getCode()), httpException.getDescription(), context);
            }
        });
        return root;
    }

    private void runLayoutAnimation(RecyclerView recyclerView, Context context) {
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(animation);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}