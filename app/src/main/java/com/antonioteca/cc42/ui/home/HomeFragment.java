package com.antonioteca.cc42.ui.home;

import static com.antonioteca.cc42.utility.Util.generateQrCode;
import static com.antonioteca.cc42.utility.Util.showModalQrCode;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentHomeBinding;
import com.antonioteca.cc42.factory.EventViewModelFactory;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.repository.EventRepository;
import com.antonioteca.cc42.utility.EventAdapter;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.EventViewModel;
import com.antonioteca.cc42.viewmodel.SharedViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class HomeFragment extends Fragment {

    private Context context;

    private EventAdapter eventAdapter;
    private EventViewModel eventViewModel;
    private SharedViewModel sharedViewModel;

    private FragmentHomeBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        EventRepository eventRepository = new EventRepository(context);
        EventViewModelFactory eventViewModelFactory = new EventViewModelFactory(eventRepository);
        eventViewModel = new ViewModelProvider(this, eventViewModelFactory).get(EventViewModel.class);
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.recyclerviewEventsList.setHasFixedSize(true);
        binding.recyclerviewEventsList.setLayoutManager(new LinearLayoutManager(context));

        User user = new User(context);
        user.coalition = new Coalition(context);

        binding.swipeRefreshLayout.setOnRefreshListener(() -> eventViewModel.getEvents(context));
        binding.fabGenerateQrCodeUser.setOnClickListener(v -> {
            Bitmap bitmapQrCode = generateQrCode(context, "user" + user.getUid() + "#" + user.getLogin() + "#" + user.getDisplayName());
            showModalQrCode(context, bitmapQrCode, user.getLogin() + "\n" + user.getDisplayName());
        });

        binding.textViewCoalition.setText(user.coalition.getName());
        binding.textViewFullName.setText(user.getDisplayName());

        String colorCoalition = user.coalition.getColor();
        if (colorCoalition != null) {
            int color = Color.parseColor(colorCoalition);
            ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(colorCoalition));
            binding.progressBar.setIndeterminateTintList(colorStateList);
            binding.textViewCoalition.setTextColor(color);
            binding.textViewFullName.setTextColor(color);
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
        eventViewModel.getEventsList(context, binding.progressBar).observe(getViewLifecycleOwner(), eventList -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefreshLayout.setRefreshing(false);
            if (eventList.get(0) != null) {
                eventAdapter = new EventAdapter(eventList);
                binding.recyclerviewEventsList.setAdapter(eventAdapter);
                // Aplicar a animação de layout
                // runLayoutAnimation(binding.recyclerviewEventsList, context);
            }
        });
        eventViewModel.getHttpSatus().observe(getViewLifecycleOwner(), httpStatus -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefreshLayout.setRefreshing(false);
            binding.textViewEmptyData.setVisibility(View.VISIBLE);
            binding.recyclerviewEventsList.setVisibility(View.GONE);
            Util.showAlertDialogBuild(String.valueOf(httpStatus.getCode()), httpStatus.getDescription(), context, () -> {
                binding.progressBar.setVisibility(View.VISIBLE);
                eventViewModel.getEvents(context);
            });
        });
        eventViewModel.getHttpException().observe(getViewLifecycleOwner(), httpException -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefreshLayout.setRefreshing(false);
            binding.textViewEmptyData.setVisibility(View.VISIBLE);
            binding.recyclerviewEventsList.setVisibility(View.GONE);
            Util.showAlertDialogBuild(String.valueOf(httpException.getCode()), httpException.getDescription(), context, () -> {
                binding.progressBar.setVisibility(View.VISIBLE);
                eventViewModel.getEvents(context);
            });
        });
        sharedViewModel.disabledRecyclerView().observe(getViewLifecycleOwner(), disabled -> binding.recyclerviewEventsList.setOnTouchListener((v, event) -> disabled));
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