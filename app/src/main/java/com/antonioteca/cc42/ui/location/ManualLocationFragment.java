package com.antonioteca.cc42.ui.location;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.factory.UserViewModelFactory;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Location;
import com.antonioteca.cc42.model.ReliabilityCalculator;
import com.antonioteca.cc42.model.ReliabilityResult;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.LocationSaveCallback;
import com.antonioteca.cc42.network.NotificationExpo.NotificationSender;
import com.antonioteca.cc42.network.NotificationFirebase.Notification;
import com.antonioteca.cc42.repository.UserRepository;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.UserViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.content.Context;
import android.graphics.Color;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ManualLocationFragment extends Fragment {

    private ProgressBar progressBar, searchProgressBar;
    private LocationsOverlayView overlay;
    private String selectedStudentDisplayName;
    private Location selectedStudentLocation;
    private TextView selectedLocationText;
    private UserViewModel userViewModel;
    private Location myCurrentLocation;
    private Context context;
    private User user;

    private String userId;
    private String userLogin;
    private String displayName;
    private String urlImageUser;
    private String campusId;
    private String cursusId;
    private String pushToken;

    public ManualLocationFragment() {
        // construtor vazio
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        user = new User(context);
        user.coalition = new Coalition(context);
        UserRepository userRepository = new UserRepository(context);
        UserViewModelFactory eventViewModelFactory = new UserViewModelFactory(userRepository);
        userViewModel = new ViewModelProvider(this, eventViewModelFactory).get(UserViewModel.class);

        this.userId = String.valueOf(user.getUid());
        this.userLogin = String.valueOf(user.getLogin());
        this.displayName = String.valueOf(user.getDisplayName());
        this.urlImageUser = String.valueOf(user.getImage());
        this.campusId = String.valueOf(user.getCampusId());
        this.cursusId = String.valueOf(user.getCursusId());
        this.pushToken = String.valueOf(user.getPushToken());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_manual_location, container, false);

        selectedLocationText = root.findViewById(R.id.selectedLocationText);
        overlay = root.findViewById(R.id.locationsOverlay);

        this.searchProgressBar = root.findViewById(R.id.searchProgressBar);
        this.progressBar = root.findViewById(R.id.progressBar);

        ImageView imageViewClose = root.findViewById(R.id.imageViewClose);
        ImageView studentAvatar = root.findViewById(R.id.studentAvatar);

        ImageButton searchButton = root.findViewById(R.id.searchButton);
        CardView cardView = root.findViewById(R.id.foundStudentCard);

        TextInputLayout searchInputLayout = root.findViewById(R.id.searchInputLayout);
        TextInputEditText searchInput = root.findViewById(R.id.searchInput);

        TextView studentName = root.findViewById(R.id.studentName);
        TextView studentLogin = root.findViewById(R.id.studentLogin);
        TextView locationText = root.findViewById(R.id.locationText);

        Button buttonNotify = root.findViewById(R.id.buttonNotify);
        Button buttonShare = root.findViewById(R.id.buttonShare);

        LinearLayout locationBadge = root.findViewById(R.id.locationBadge);

        View reliabilityIndicator = root.findViewById(R.id.reliabilityIndicator);
        LinearLayout reliabilityBadge = root.findViewById(R.id.reliabilityBadge);
        TextView reliabilityText = root.findViewById(R.id.reliabilityText);

        String colorCoalition = this.user.coalition.getColor();
        if (colorCoalition != null) {
            ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(colorCoalition));
            this.searchProgressBar.setIndeterminateTintList(colorStateList);
            this.progressBar.setIndeterminateTintList(colorStateList);
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
            userViewModel.getUserByLogin(this.context, searchInputString);
        });

        buttonNotify.setOnClickListener(v -> {
            if (this.selectedStudentLocation == null || this.selectedStudentLocation.pushToken == null) {
                Util.showAlertDialogBuild(getString(R.string.err), getString(R.string.notifyStudentError), this.context, null);
                return;
            }

            if (this.myCurrentLocation == null) {
                Util.showAlertDialogBuild(getString(R.string.warning), getString(R.string.needToSetLocation), this.context, null);
                return;
            }

            String title = getString(R.string.someoneIsLookingForYou);
            String body = getString(R.string.locationPromptMessage, this.userLogin) + "\n" + getString(R.string.sharedLocationBody, this.userLogin, this.myCurrentLocation.areaName);

            Map<String, Object> data = new HashMap<>();
            data.put("type", "location_search");
            sendNotification(title, body, data);
        });

        buttonShare.setOnClickListener(v -> {
            if (this.selectedStudentLocation == null || this.selectedStudentLocation.pushToken == null) {
                Util.showAlertDialogBuild(getString(R.string.err), getString(R.string.shareLocationError), this.context, null);
                return;
            }
            if (this.myCurrentLocation == null) {
                Util.showAlertDialogBuildSimple(getString(R.string.needToSetLocation), getString(R.string.needToSetLocationMessage), this.context);
                return;
            }
            String timeAgo = ReliabilityCalculator.getTimeAgo(this.context, this.myCurrentLocation.lastUpdated);
            new AlertDialog.Builder(this.context)
                    .setTitle(getString(R.string.confirmShareLocation))
                    .setMessage(getString(R.string.confirmShareLocationMessage, this.myCurrentLocation.areaName, timeAgo))
                    .setPositiveButton(getString(R.string.sendLocation), (dialog, which) -> {
                        String title = getString(R.string.sharedLocationWithYou, this.displayName);
                        String body = getString(R.string.sharedLocationBody, this.userLogin, this.myCurrentLocation.areaName);

                        Map<String, Object> data = new HashMap<>();
                        data.put("type", "location_shared");
                        data.put("sharedBy", this.displayName + " - " + this.userLogin);
                        data.put("location", this.myCurrentLocation.areaName);

                        sendNotification(title, body, data);
                    })
                    .setNegativeButton(getString(R.string.updateFirst), (dialog, which) -> dialog.dismiss())
                    .setNeutralButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                    .show();
        });


        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {

                locationText.setText(null);
                reliabilityText.setText(null);

                String uid = String.valueOf(user.uid);
                long campusId = user.campus.get(0).id;
                int cursusId = user.isStaff ? 0 : user.projectsUsers.get(0).cursusIds.get(0);
                String urlImageUser = user.getUrlImageUser();
                this.selectedStudentDisplayName = user.displayName;

                studentName.setText(user.displayName);
                studentLogin.setText(user.login);
                Util.setImageUserRegistered(this.context, urlImageUser == null ? null : urlImageUser.trim(), studentAvatar);

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
                                locationBadge.setVisibility(View.GONE);
                                reliabilityBadge.setVisibility(View.GONE);
                                buttonShare.setVisibility(View.GONE);
                                buttonNotify.setVisibility(View.GONE);
                                cardView.setVisibility(View.VISIBLE);
                                searchInputLayout.setError(e.getMessage());
                            }

                            @Override
                            public void onComplete(Location location) {
                                selectedStudentLocation = location;
                                searchProgressBar.setVisibility(View.GONE);
                                searchButton.setVisibility(View.VISIBLE);

                                if (location != null) {

                                    locationBadge.setVisibility(View.VISIBLE);
                                    reliabilityBadge.setVisibility(View.VISIBLE);

                                    locationText.setText(location.areaName);
                                    buttonShare.setVisibility(userId.equals(uid) || location.pushToken == null || location.pushToken.isEmpty() ? View.GONE : View.VISIBLE);
                                    buttonNotify.setVisibility(userId.equals(uid) || location.pushToken == null || location.pushToken.isEmpty() ? View.GONE : View.VISIBLE);

                                    ReliabilityResult reliability = ReliabilityCalculator.getReliability(context, location.lastUpdated);
                                    String reliabilityMessage = location.areaName + "\n\n";
                                    reliabilityMessage += getString(R.string.reliability) + " " + reliability.getLevel() + "\n";
                                    reliabilityMessage += reliability.getMessage();

                                    String reliabilityColor = reliability.getColor();
                                    updateReliabilityIndicator(reliabilityColor + "20", reliabilityBadge);
                                    updateReliabilityIndicator(reliabilityColor, reliabilityIndicator);
                                    reliabilityText.setTextColor(Color.parseColor(reliabilityColor));

                                    String message = reliability.getLevel() + "\n" + ReliabilityCalculator.getTimeAgo(context, location.lastUpdated);
                                    reliabilityText.setText(message);

                                    Util.showAlertDialogBuildSimple(getString(R.string.locationFound), user.displayName + " " + getString(R.string.studentAt) + "\n" + reliabilityMessage, context);
                                } else {
                                    buttonShare.setVisibility(View.GONE);
                                    buttonNotify.setVisibility(View.GONE);
                                    locationBadge.setVisibility(View.GONE);
                                    reliabilityBadge.setVisibility(View.GONE);
                                    searchInputLayout.setError(getString(R.string.locationNotFound));
                                    Util.showAlertDialogBuildSimple(getString(R.string.noLocation), getString(R.string.studentNoLocation, user.displayName), context);
                                }
                                cardView.setVisibility(View.VISIBLE);
                            }
                        });
            } else {
                this.searchProgressBar.setVisibility(View.GONE);
                searchButton.setVisibility(View.VISIBLE);
            }
        });

        userViewModel.getHttpSatus().observe(getViewLifecycleOwner(), httpStatus -> {
            this.searchProgressBar.setVisibility(View.GONE);
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
                                Util.showAlertDialogBuildSimple("🔴 " + getString(R.string.err), e.getMessage(), context);
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

    private void sendNotification(String title, String body, Map<String, Object> data) {
        this.progressBar.setVisibility(View.VISIBLE);
        if (this.selectedStudentLocation.pushToken.startsWith("Expo")) {
            new NotificationSender().sendExpoNotificationToUser(
                    this.context,
                    this.progressBar,
                    this.selectedStudentDisplayName,
                    this.selectedStudentLocation.pushToken,
                    title,
                    body,
                    data,
                    this.urlImageUser
            );
        } else {
            Notification.sendFCMNotificationToUser(
                    this.context,
                    this.progressBar,
                    this.selectedStudentDisplayName,
                    this.selectedStudentLocation.pushToken,
                    title,
                    body,
                    data,
                    this.urlImageUser
            );
        }
    }

    public void updateReliabilityIndicator(String colorString, @NonNull View reliabilityIndicator) {
        Drawable background = reliabilityIndicator.getBackground();

        // Importante: Chame mutate() para garantir que a mudança de cor
        // não afete outras instâncias deste drawable no seu app.
        background.mutate();

        try {
            int color = Color.parseColor(colorString);
            DrawableCompat.setTint(background, color);
        } catch (IllegalArgumentException e) {
            // Lida com o caso de uma string de cor inválida, se necessário
            e.printStackTrace();
        }
    }

    private void saveLocation(@NonNull Location location) throws NullPointerException {

        if (this.userId.equals("null")) {
            throw new NullPointerException("User ID not found in storage");
        } else if (this.campusId.equals("null")) {
            throw new NullPointerException("Campus ID not found in storage");
        } else if (this.cursusId.equals("null")) {
            throw new NullPointerException("Cursus ID not found in storage");
        } else if (this.pushToken.equals("null")) {
            throw new NullPointerException("Push Token not found in storage");
        }

        location.pushToken = this.pushToken;
        location.lastUpdated = System.currentTimeMillis();
        userViewModel.saveUserLocation(
                this.userId,
                this.campusId,
                this.cursusId,
                location,
                new LocationSaveCallback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        String localSelected = getString(R.string.localSelected) + " " + location.areaName;
                        selectedLocationText.setText(localSelected);
                        myCurrentLocation = location;
                        Util.showAlertDialogBuildSimple("🟢 " + getString(R.string.sucess), getString(R.string.locationSavedSuccess), context);
                    }

                    @Override
                    public void onError(Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Util.showAlertDialogBuildSimple("🔴 " + getString(R.string.err), getString(R.string.errorSavingLocation) + "\n" + e.getMessage(), context);
                    }

                    @Override
                    public void onComplete(Location location) {
                    }
                }
        );
    }
}