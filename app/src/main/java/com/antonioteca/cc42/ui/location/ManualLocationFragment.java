package com.antonioteca.cc42.ui.location;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.factory.UserViewModelFactory;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Location;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.LocationSaveCallback;
import com.antonioteca.cc42.repository.UserRepository;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.UserViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.messaging.FirebaseMessaging;


import android.content.Context;
import android.graphics.Color;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ManualLocationFragment extends Fragment {

    private ProgressBar progressBar, searchProgressBar;
    private LocationsOverlayView overlay;
    private TextView selectedLocationText;
    private UserViewModel userViewModel;
    private Context context;
    private User user;

    private String userId;
    private String campusId;
    private String cursusId;
    private String pushToken;

    public ManualLocationFragment() {
        // construtor vazio
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFirebaseMessageToken();
        context = requireContext();
        user = new User(context);
        user.coalition = new Coalition(context);
        UserRepository userRepository = new UserRepository(context);
        UserViewModelFactory eventViewModelFactory = new UserViewModelFactory(userRepository);
        userViewModel = new ViewModelProvider(this, eventViewModelFactory).get(UserViewModel.class);

        userId = String.valueOf(user.getUid());
        campusId = String.valueOf(user.getCampusId());
        cursusId = String.valueOf(user.getCursusId());
        pushToken = String.valueOf(user.getPushToken());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_manual_location, container, false);

        selectedLocationText = root.findViewById(R.id.selectedLocationText);
        overlay = root.findViewById(R.id.locationsOverlay);

        searchProgressBar = root.findViewById(R.id.searchProgressBar);
        progressBar = root.findViewById(R.id.progressBar);

        ImageView imageViewClose = root.findViewById(R.id.imageViewClose);
        ImageView studentAvatar = root.findViewById(R.id.studentAvatar);

        ImageButton searchButton = root.findViewById(R.id.searchButton);
        CardView cardView = root.findViewById(R.id.foundStudentCard);

        TextInputLayout searchInputLayout = root.findViewById(R.id.searchInputLayout);
        TextInputEditText searchInput = root.findViewById(R.id.searchInput);

        TextView studentName = root.findViewById(R.id.studentName);
        TextView studentLogin = root.findViewById(R.id.studentLogin);
        TextView locationText = root.findViewById(R.id.locationText);
        TextView reliabilityText = root.findViewById(R.id.reliabilityText);

        Button buttonNotify = root.findViewById(R.id.buttonNotify);
        Button buttonShare = root.findViewById(R.id.buttonShare);

        String colorCoalition = user.coalition.getColor();
        if (colorCoalition != null) {
            ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(colorCoalition));
            searchProgressBar.setIndeterminateTintList(colorStateList);
            progressBar.setIndeterminateTintList(colorStateList);
        }

        List<Location> schoolLocations = new ArrayList<>();
        schoolLocations.add(new Location("formal_auditorium", "Auditório Formal", 0.20f, 0.04f, 0.29f, 0.17f, Color.parseColor("#803498DB")));
        schoolLocations.add(new Location("direction", "Direção", 0.37f, 0.04f, 0.15f, 0.09f, Color.parseColor("#8034D5DB")));
        schoolLocations.add(new Location("copa", "Copa", 0.19f, 0.33f, 0.25f, 0.18f, Color.parseColor("#802ECC71")));
        schoolLocations.add(new Location("reception", "Recepção", 0.46f, 0.08f, 0.11f, 0.12f, Color.parseColor("#809B59B6")));
        schoolLocations.add(new Location("cluster_1", "Cluster 1", 0.42f, 0.24f, 0.35f, 0.16f, Color.parseColor("#80F1C40F")));
        schoolLocations.add(new Location("cluster_2", "Cluster 2", 0.42f, 0.58f, 0.33f, 0.16f, Color.parseColor("#80F10FA6")));
        schoolLocations.add(new Location("wc", "WC", 0.62f, 0.07f, 0.12f, 0.102f, Color.parseColor("#80E74C3C")));
        schoolLocations.add(new Location("cluster_3", "Cluster 3", 0.58f, 0.70f, 0.21f, 0.12f, Color.parseColor("#8048BC1A")));
        schoolLocations.add(new Location("libriary", "Biblioteca", 0.82f, 0.69f, 0.22f, 0.13f, Color.parseColor("#801A73BC")));
        schoolLocations.add(new Location("bocal", "Bocal", 0.71f, 0.69f, 0.08f, 0.11f, Color.parseColor("#80BC7B1A")));
        schoolLocations.add(new Location("informal_auditorium", "Auditório Informal", 0.19f, 0.58f, 0.25f, 0.18f, Color.parseColor("#80D9E622")));
        schoolLocations.add(new Location("server_room", "Servidores", 0.19f, 0.82f, 0.10f, 0.18f, Color.parseColor("#80E67E22")));
        schoolLocations.add(new Location("hallway", "Corredor", 0.37f, 0.20f, 0.70f, 0.05f, Color.parseColor("#6EEDDEAA")));
        schoolLocations.add(new Location("decompression_zone", "Zona de Descompressão", 0.44f, 0.91f, 0.07f, 0.39f, Color.parseColor("#8095A5A6")));
        overlay.setLocations(schoolLocations);

        imageViewClose.setOnClickListener(v -> cardView.setVisibility(View.GONE));

        searchButton.setOnClickListener(v -> {
            String searchInputString = Objects.requireNonNullElse(searchInput.getText(), "").toString();
            if (searchInputString.trim().isEmpty()) {
                searchInputLayout.requestFocus();
                searchInputLayout.setError(getString(R.string.enterValidLogin));
                return;
            } else if (!Objects.requireNonNullElse(searchInputLayout.getError(), "").toString().isEmpty()) {
                searchInputLayout.setError(null);
            }
            v.setVisibility(View.GONE);
            cardView.setVisibility(View.GONE);
            searchProgressBar.setVisibility(View.VISIBLE);
            userViewModel.getUserByLogin(context, searchInputString);
        });


        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {

                locationText.setText(null);
                reliabilityText.setText(null);

                String uid = String.valueOf(user.uid);
                long campusId = user.campus.get(0).id;
                int cursusId = user.isStaff ? 0 : user.projectsUsers.get(0).cursusIds.get(0);
                String urlImageUser = user.getUrlImageUser();

                studentName.setText(user.displayName);
                studentLogin.setText(user.login);
                Util.setImageUserRegistered(context, urlImageUser == null ? urlImageUser : urlImageUser.trim(), studentAvatar);

                userViewModel.getUserLocation(
                        uid,
                        String.valueOf(campusId),
                        String.valueOf(cursusId), new LocationSaveCallback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError(Exception e) {
                                searchProgressBar.setVisibility(View.GONE);
                                searchButton.setVisibility(View.VISIBLE);
                                buttonShare.setVisibility(View.GONE);
                                buttonNotify.setVisibility(View.GONE);
                                cardView.setVisibility(View.VISIBLE);
                                searchInputLayout.setError(e.getMessage());
                            }

                            @Override
                            public void onComplete(Location location) {
                                searchProgressBar.setVisibility(View.GONE);
                                searchButton.setVisibility(View.VISIBLE);
                                buttonShare.setVisibility(userId.equals(uid) ? View.VISIBLE : View.GONE);
                                if (location != null) {
                                    locationText.setText(location.areaName);
                                    reliabilityText.setText(location.lastUpdated + "");
                                    buttonShare.setVisibility(userId.equals(uid) || location.pushToken == null || location.pushToken.isEmpty() ? View.GONE : View.VISIBLE);
                                    buttonNotify.setVisibility(userId.equals(uid) || location.pushToken == null || location.pushToken.isEmpty() ? View.GONE : View.VISIBLE);
                                } else {
                                    buttonShare.setVisibility(View.GONE);
                                    buttonNotify.setVisibility(View.GONE);
                                    searchInputLayout.setError(getString(R.string.locationNotFound));
                                }
                                cardView.setVisibility(View.VISIBLE);
                            }
                        });
            } else {
                searchProgressBar.setVisibility(View.GONE);
                searchButton.setVisibility(View.VISIBLE);
            }
        });

        userViewModel.getHttpSatus().observe(getViewLifecycleOwner(), httpStatus -> {
            searchProgressBar.setVisibility(View.GONE);
            searchButton.setVisibility(View.VISIBLE);
            if (httpStatus != null) {
                searchInputLayout.setError(httpStatus.getCode() == 404 ? getString(R.string.errorSearching) : httpStatus.getDescription());
            }
        });

        overlay.setOnLocationSelectedListener(new LocationsOverlayView.OnLocationSelectedListener() {
            @Override
            public void onLocationSelected(final Location location) {
                // Mostra confirmação antes de "salvar"
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.confirmLocation)
                        .setMessage(getString(R.string.youAreAt) + " " + location.areaName + " ?")
                        .setNegativeButton(R.string.cancel, (dialog, which) -> overlay.clearSelection())
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            try {
                                progressBar.setVisibility(View.VISIBLE);
                                saveLocation(location);
                            } catch (NullPointerException e) {
                                progressBar.setVisibility(View.GONE);
                                Util.showAlertDialogBuild("🔴 " + getString(R.string.err), e.getMessage(), context, null);
                            }
                        })
                        .show();
            }

            @Override
            public void onLocationLongPressed(Location location) {
                // nesta versão base só mostramos um Toast
                Toast.makeText(requireContext(), "Long press em " + location.areaName, Toast.LENGTH_SHORT).show();
            }
        });
        return root;
    }

    private void saveLocation(@NonNull Location location) throws NullPointerException {

        if (userId.equals("null")) {
            throw new NullPointerException("User ID not found in storage");
        } else if (campusId.equals("null")) {
            throw new NullPointerException("Campus ID not found in storage");
        } else if (cursusId.equals("null")) {
            throw new NullPointerException("Cursus ID not found in storage");
        } else if (pushToken.equals("null")) {
            throw new NullPointerException("Push Token not found in storage");
        }

        location.pushToken = pushToken;
        location.lastUpdated = System.currentTimeMillis();
        userViewModel.saveUserLocation(
                userId,
                campusId,
                cursusId,
                location,
                new LocationSaveCallback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        Util.showAlertDialogBuild("🟢 " + getString(R.string.sucess), getString(R.string.locationSavedSuccess), context, null);
                        String localSelected = getString(R.string.localSelected) + " " + location.areaName;
                        selectedLocationText.setText(localSelected);
                    }

                    @Override
                    public void onError(Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Util.showAlertDialogBuild("🔴 " + getString(R.string.err), getString(R.string.errorSavingLocation) + "\n" + e.getMessage(), context, null);
                    }

                    @Override
                    public void onComplete(Location location) {
                    }
                }
        );
    }

    private void getFirebaseMessageToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Util.showAlertDialogBuild("FirebaseToken", "Fetching FCM registration token failed: " + task.getException(), context, null);
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    user.setPushToken(token);
                    // Log and toast
                    Log.d("FirebaseToken", token);
                });
    }
}