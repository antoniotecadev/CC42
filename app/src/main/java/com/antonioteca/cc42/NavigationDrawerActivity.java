package com.antonioteca.cc42;

import static com.antonioteca.cc42.network.NetworkConstants.CAMERA_PERMISSION_CODE;
import static com.antonioteca.cc42.utility.Util.setColorCoalition;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.antonioteca.cc42.dao.daofarebase.DaoEventFirebase;
import com.antonioteca.cc42.databinding.ActivityNavigationDrawerBinding;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Token;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
import com.antonioteca.cc42.utility.GlideApp;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.SharedViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.FirebaseDatabase;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class NavigationDrawerActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private String uid;
    private String userLogin;
    private String displayName;
    private String campusId;
    private String cursusId;
    private Context context;
    private FloatingActionButton fabOpenCameraScannerQrCode;
    private ProgressBar progressBarMarkAttendance;
    private SharedViewModel sharedViewModel;
    private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityNavigationDrawerBinding binding = ActivityNavigationDrawerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarNavigationDrawer.toolbar);
        User user = new User(NavigationDrawerActivity.this);
        uid = String.valueOf(user.getUid());
        userLogin = user.getLogin();
        displayName = user.getDisplayName();
        cursusId = String.valueOf(user.getCursusId());
        campusId = String.valueOf(user.getCampusId());
        context = NavigationDrawerActivity.this;
        firebaseDatabase = FirebaseDataBaseInstance.getInstance().database;
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        fabOpenCameraScannerQrCode = binding.appBarNavigationDrawer.fabOpenCameraScannerQrCode;
        progressBarMarkAttendance = binding.appBarNavigationDrawer.progressBarmarkAttendance;
        fabOpenCameraScannerQrCode.setOnClickListener(view -> {
            // Verificar se a permissão já foi concedida
            if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                // Solicitar a permissão
                ActivityCompat.requestPermissions(NavigationDrawerActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else
                openCameraScannerQrCodeEvent(new ScanOptions());
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each.
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation_drawer);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController navController, @NonNull NavDestination navDestination, @Nullable Bundle bundle) {
                if (navDestination.getId() == R.id.detailsEventFragment || navDestination.getId() == R.id.attendanceListFragment)
                    fabOpenCameraScannerQrCode.setVisibility(View.INVISIBLE);
                else
                    fabOpenCameraScannerQrCode.setVisibility(View.VISIBLE);
            }
        });
        // Obter o NavigationView
        // Obter o header view dentro do NavigationView
        View headerView = navigationView.getHeaderView(0);
        user.coalition = new Coalition(this);

        Toolbar toolbar = binding.appBarNavigationDrawer.toolbar;
        String colorCoalition = user.coalition.getColor();
        setColorCoalition(toolbar, colorCoalition);
        if (colorCoalition != null) {
            ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(colorCoalition));
            // fabOpenCameraScannerQrCode.setBackgroundTintList(colorStateList); // Opcional
            progressBarMarkAttendance.setIndeterminateTintList(colorStateList);
            navigationView.setItemTextColor(colorStateList);
            navigationView.setItemIconTintList(colorStateList);
        }
        LinearLayout linearLayout = headerView.findViewById(R.id.linearLayoutNavHeaderNavigationDrawer);
        String imageUrlCoalition = user.coalition.getImageUrl();
        if (imageUrlCoalition != null) {
            Glide.with(this)
                    .load(imageUrlCoalition)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            linearLayout.setBackground(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // Definir background placeholder caso a imagem não carregue
                        }
                    });
        }
        ImageView imageViewUser = headerView.findViewById(R.id.imageViewUser);
        TextView textViewFullNameUser = headerView.findViewById(R.id.fullNameUser);
        TextView textViewEmailUser = headerView.findViewById(R.id.emailUser);
        textViewFullNameUser.setText(user.getDisplayName());
        textViewEmailUser.setText(user.getEmail());

        String imageUrl = user.getImage();
        GlideApp.with(this)
                .load(imageUrl)
                .circleCrop() // Recorta a imagem para ser circular
                .placeholder(R.drawable.logo_42) // Imagem de substituição enquanto a imagem carrega
                .error(R.drawable.logo_42) // Imagem a ser mostrada caso ocorra um erro
                .into(imageViewUser);

        this.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_settings_general, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                Activity context = NavigationDrawerActivity.this;
                NavController navController = Navigation.findNavController(context, R.id.nav_host_fragment_content_navigation_drawer);
                if (menuItem.getItemId() == R.id.action_logout)
                    logout(context);
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        }, this);
    }

    private void logout(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(getString(R.string.action_logout))
                .setMessage(getString(R.string.msg_logout))
                .setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss())
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> removeSessionUser(context))
                .show();
    }

    private void removeSessionUser(Context context) {
        new User(context).clear();
        new Token(context).clear();
        redirectToLogin(context);
    }

    private void redirectToLogin(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("fragment_id", R.id.loginFragment_nav);
        startActivity(intent);
        finish();
    }

    private void openCameraScannerQrCodeEvent(ScanOptions scanOptions) {
        scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        scanOptions.setPrompt(getString(R.string.align_camera_qr_code));
        scanOptions.setOrientationLocked(false); // unlock orientation of camera
        scanOptions.setCameraId(0);
        scanOptions.setBeepEnabled(true);
        barScanOptionsActivityResultLauncher.launch(scanOptions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE)
            openCameraScannerQrCodeEvent(new ScanOptions());
        else
            Util.showAlertDialogBuild(getString(R.string.err), getString(R.string.msg_permis_camera_denied), this, null);
    }

    private final ActivityResultLauncher<ScanOptions> barScanOptionsActivityResultLauncher = registerForActivityResult(new ScanContract(), result -> {
        String eventId = result.getContents();
        if (eventId == null)
            Toast.makeText(context, R.string.cancelled, Toast.LENGTH_LONG).show();
        else if (eventId.startsWith("cc42")) {
            Util.setVisibleProgressBar(progressBarMarkAttendance, fabOpenCameraScannerQrCode, sharedViewModel);
            DaoEventFirebase.markAttendance(
                    firebaseDatabase,
                    eventId.replace("cc42", ""),
                    uid,
                    userLogin,
                    displayName,
                    cursusId,
                    campusId,
                    context,
                    getLayoutInflater(),
                    progressBarMarkAttendance,
                    fabOpenCameraScannerQrCode,
                    sharedViewModel
            );
        } else
            Toast.makeText(context, R.string.msg_qr_code_invalid, Toast.LENGTH_LONG).show();
    });

    /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings_general, menu);
        return true;
    }*/

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation_drawer);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}