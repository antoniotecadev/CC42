package com.antonioteca.cc42.ui.home;

import static com.antonioteca.cc42.utility.Util.generateQrCodeWithLogo;
import static com.antonioteca.cc42.utility.Util.showModalQrCode;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.repository.EventRepository;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.EventViewModel;
import com.antonioteca.cc42.viewmodel.SharedViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class HomeFragment extends Fragment {

    private User user;
    private Long uid;
    private String userLogin;
    private String displayName;
    private Integer campusId;
    private Integer cursusId;

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
        user = new User(context);
        user.coalition = new Coalition(context);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        uid = user.getUid();
        userLogin = user.getLogin();
        displayName = user.getDisplayName();
        cursusId = user.getCursusId();
        campusId = user.getCampusId();
        binding.recyclerviewEventsList.setHasFixedSize(true);
        binding.recyclerviewEventsList.setLayoutManager(new LinearLayoutManager(context));
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            setupVisibility(binding, View.GONE, true, View.GONE, View.VISIBLE);
            eventViewModel.getEvents(context);
        });
        binding.fabGenerateQrCodeUser.setOnClickListener(v -> {
            Bitmap bitmapQrCode = generateQrCodeWithLogo(context, "user" + uid + "#" + userLogin + "#" + displayName + "#" + cursusId + "#" + campusId);
            showModalQrCode(context, bitmapQrCode, user.getLogin(), user.getDisplayName());
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
            if (!eventList.isEmpty() && eventList.get(0) != null) {
                setupVisibility(binding, View.GONE, false, View.GONE, View.VISIBLE);
                eventAdapter = new EventAdapter(eventList);
                binding.recyclerviewEventsList.setAdapter(eventAdapter);
                // Aplicar a animação de layout
                // runLayoutAnimation(binding.recyclerviewEventsList, context);
            } else
                setupVisibility(binding, View.GONE, false, View.VISIBLE, View.GONE);
        });

        eventViewModel.getHttpSatus().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                HttpStatus httpStatus = event.getContentIfNotHandled();
                setupVisibility(binding, View.GONE, false, View.VISIBLE, View.GONE);
                if (httpStatus != null) {
                    Util.showAlertDialogBuild(String.valueOf(httpStatus.getCode()), httpStatus.getDescription(), context, () -> {
                        setupVisibility(binding, View.VISIBLE, false, View.GONE, View.GONE);
                        eventViewModel.getEvents(context);
                    });
                }
            }
        });

        eventViewModel.getHttpException().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                HttpException httpException = event.getContentIfNotHandled();
                setupVisibility(binding, View.GONE, false, View.VISIBLE, View.GONE);
                if (httpException != null) {
                    Util.showAlertDialogBuild(String.valueOf(httpException.getCode()), httpException.getDescription(), context, () -> {
                        setupVisibility(binding, View.VISIBLE, false, View.GONE, View.GONE);
                        eventViewModel.getEvents(context);
                    });
                }
            }
        });
        sharedViewModel.disabledRecyclerView().observe(getViewLifecycleOwner(), disabled -> binding.recyclerviewEventsList.setOnTouchListener((v, event) -> disabled));
        //requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), outApp(getActivity(), context));
        return root;
    }

    public static OnBackPressedCallback outApp(Activity activity, Context context) {
        return new OnBackPressedCallback(true) {
            private long backPressedTime;

            @Override
            public void handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    activity.finish();
                    return;
                } else Toast.makeText(context, R.string.press_again_out, Toast.LENGTH_SHORT).show();

                backPressedTime = System.currentTimeMillis();
            }
        };
    }

    private void setupVisibility(FragmentHomeBinding binding, int viewP, boolean refreshing, int viewT, int viewR) {
        binding.progressBar.setVisibility(viewP);
        binding.swipeRefreshLayout.setRefreshing(refreshing);
        binding.textViewEmptyData.setVisibility(viewT);
        binding.recyclerviewEventsList.setVisibility(viewR);
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