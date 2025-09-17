package com.antonioteca.cc42.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentHomeBinding;
import com.antonioteca.cc42.databinding.ListItemMessageBinding;
import com.antonioteca.cc42.factory.EventViewModelFactory;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Message;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private User user;
    private String userLogin;
    private String displayName;

    private Context context;
    private EventAdapter eventAdapter;
    private FragmentHomeBinding binding;
    private EventViewModel eventViewModel;
    private SharedViewModel sharedViewModel;

    // Constantes para SharedPreferences
    private static final String PREFS_NAME = "HomeFragmentPrefs";
    private static final String KEY_LAST_SEEN_MESSAGE_ID = "lastSeenMessageId";
    private static final String KEY_DONT_SHOW_AGAIN_UNTIL_NEW = "dontShowAgainUntilNew";

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
        if (user.isStaff())
            binding.fabGenerateQrCodeUser.setVisibility(View.GONE);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userLogin = user.getLogin();
        String userImage = user.getImage();
        int campusId = user.getCampusId();
        int cursusId = user.getCursusId();
        displayName = user.getDisplayName();

        binding.recyclerviewEventsList.setHasFixedSize(true);
        binding.recyclerviewEventsList.setLayoutManager(new LinearLayoutManager(context));

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            setupVisibility(binding, View.GONE, true, View.GONE, View.VISIBLE);
            eventViewModel.getEvents(context);
        });

        binding.fabGenerateQrCodeUser.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.qrCodeFragment) {
                String content = "user" + user.getUid() + "#" + userLogin + "#" + displayName + "#" + user.getCursusId() + "#" + user.getCampusId() + "#" + userImage;
                HomeFragmentDirections.ActionNavHomeToQrCodeFragment actionNavHomeToQrCodeFragment =
                        HomeFragmentDirections.actionNavHomeToQrCodeFragment(content, userLogin, displayName, 0, 0);
                Navigation.findNavController(v).navigate(actionNavHomeToQrCodeFragment);
            }
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
        } else
            colorCoalition = String.format("#%06X", (0xFFFFFF) & ContextCompat.getColor(context, R.color.light_blue_900));
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

        String finalColorCoalition = colorCoalition;
        eventViewModel.getEventsList(context, binding.progressBar).observe(getViewLifecycleOwner(), eventList -> {
            setupVisibility(binding, View.GONE, false, View.GONE, View.VISIBLE);
            if (!eventList.isEmpty() && eventList.get(0) != null) {
                eventAdapter = new EventAdapter(eventList, finalColorCoalition, context);
                binding.recyclerviewEventsList.setAdapter(eventAdapter);
                // Aplicar a animação de layout
                // runLayoutAnimation(binding.recyclerviewEventsList, context);
            }
        });

        eventViewModel.getHttpSatus().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                HttpStatus httpStatus = event.getContentIfNotHandled();
                setupVisibility(binding, View.GONE, false, View.VISIBLE, View.GONE);
                if (httpStatus != null) {
                    Util.showAlertDialogBuild(String.valueOf(httpStatus.getCode()), httpStatus.getDescription(), context, () -> {
                        setupVisibility(binding, View.VISIBLE, false, View.GONE, View.VISIBLE);
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
                        setupVisibility(binding, View.VISIBLE, false, View.GONE, View.VISIBLE);
                        eventViewModel.getEvents(context);
                    });
                }
            }
        });
        sharedViewModel.disabledRecyclerView().observe(getViewLifecycleOwner(), disabled -> binding.recyclerviewEventsList.setOnTouchListener((v, event) -> disabled));
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), outApp(getActivity(), context));
        // Lógica para buscar e exibir a mensagem mais recente
        if (cursusId != 0)
            fetchAndShowLatestMessageDialog(String.valueOf(campusId), String.valueOf(cursusId));
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

//    private void runLayoutAnimation(RecyclerView recyclerView, Context context) {
//        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);
//        recyclerView.setLayoutAnimation(animation);
//        recyclerView.getAdapter().notifyDataSetChanged();
//        recyclerView.scheduleLayoutAnimation();
//    }

    private void fetchAndShowLatestMessageDialog(String campusId, String cursusId) {

        DatabaseReference messagesRef = FirebaseDataBaseInstance.getInstance().database
                .getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("messages");

        messagesRef.orderByChild("timestamp").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot latestMessageSnapshot = null;
                    String latestMessageId = null;

                    // Como limitToLast(1) retorna um snapshot com um único filho (a mensagem)
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        latestMessageSnapshot = snapshot;
                        latestMessageId = snapshot.getKey(); // ID da mensagem
                        break; // Só precisamos do primeiro (e único)
                    }

                    if (latestMessageSnapshot != null && latestMessageId != null) {
                        Message latestMessage = latestMessageSnapshot.getValue(Message.class); // Use sua classe Message

                        if (latestMessage != null) {
                            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            String lastSeenId = prefs.getString(KEY_LAST_SEEN_MESSAGE_ID, null);
                            boolean dontShowAgain = prefs.getBoolean(KEY_DONT_SHOW_AGAIN_UNTIL_NEW, false);

                            // Condições para mostrar o diálogo:
                            // 1. Não há "não mostrar novamente" activo, OU
                            // 2. Há "não mostrar novamente" activo, MAS a mensagem actual é DIFERENTE da última vista
                            //    (ou seja, é uma mensagem nova desde que o usuário pediu para não ver mais)
                            if (!dontShowAgain || (lastSeenId != null && !latestMessageId.equals(lastSeenId)) || lastSeenId == null) {
                                // Se for uma mensagem nova e o "não mostrar novamente" estava ativo, resetamos o "não mostrar novamente"
                                if (dontShowAgain) {
                                    prefs.edit().putBoolean(KEY_DONT_SHOW_AGAIN_UNTIL_NEW, false).apply();
                                    // Não precisamos actualizar lastSeenId aqui, pois o diálogo será mostrado
                                    // e o usuário decidirá se quer marcar como "não mostrar novamente"
                                }
                                showLatestMessageAlertDialog(latestMessage, latestMessageId);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Util.showAlertDialogBuild(getString(R.string.err), "Error fetching latest message: " + databaseError.getMessage(), getContext(), null);
            }
        });
    }

    private void showLatestMessageAlertDialog(Message message, String messageId) {
        if (getContext() == null) return; // Fragment não está mais anexado

        // Formatar o timestamp (se você tiver um)
        String formattedTimestamp = "";
        if (message.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            formattedTimestamp = sdf.format(new Date(message.getTimestamp()));
        }

        ListItemMessageBinding binding = ListItemMessageBinding.inflate(getLayoutInflater());
        binding.textViewMessageTitle.setText(message.getTitle());
        binding.textViewMessageTimestamp.setText(formattedTimestamp);
        binding.textViewMessageText.setText(message.getMessage());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()); // Use requireContext()
        builder.setView(binding.getRoot());
        builder.setPositiveButton(getText(R.string.ok), (dialog, which) -> dialog.dismiss());

        // Botão "Não mostrar novamente"
        // Este botão significa "não me mostre esta mensagem específica novamente,
        // mas me mostre se uma mais nova chegar".
        builder.setNeutralButton("Não mostrar novamente", (dialog, which) -> {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit()
                    .putString(KEY_LAST_SEEN_MESSAGE_ID, messageId) // Guarda o ID da mensagem que ele não quer mais ver
                    .putBoolean(KEY_DONT_SHOW_AGAIN_UNTIL_NEW, true) // Activa a flag
                    .apply();
            dialog.dismiss();
        });

        // Garante que o diálogo não seja dispensável ao tocar fora ou pressionar back,
        // forçando o usuário a interagir com os botões.
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}