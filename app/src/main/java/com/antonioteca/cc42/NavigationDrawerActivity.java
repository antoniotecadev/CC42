package com.antonioteca.cc42;

import static com.antonioteca.cc42.utility.Util.setAppLanguage;
import static com.antonioteca.cc42.utility.Util.setColorCoalition;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.antonioteca.cc42.dao.daofarebase.DaoEventFirebase;
import com.antonioteca.cc42.dao.daofarebase.DaoSusbscriptionFirebase;
import com.antonioteca.cc42.databinding.ActivityNavigationDrawerBinding;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Meal;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class NavigationDrawerActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "HEADS_UP_NOTIFICATION";

    private AppBarConfiguration mAppBarConfiguration;

    private String uid;
    private String userLogin;
    private String displayName;
    private String campusId;
    private String cursusId;
    private Context context;
    private Bundle args;
    private MenuProvider menuProvider;
    private FloatingActionButton fabOpenCameraScannerQrCode;
    private ProgressBar progressBarMarkAttendance;
    private SharedViewModel sharedViewModel;
    private FirebaseDatabase firebaseDatabase;
    private NavController navController;

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result)
                    openCameraScannerQrCodeEvent(new ScanOptions());
                else
                    Util.showAlertDialogBuild(getString(R.string.err), getString(R.string.msg_permis_camera_denied), context, null);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Carregar o idioma salvo antes de chamar super.onCreate()
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String language = preferences.getString("language_preference", null);
        setAppLanguage(language == null ? "en" : language, getResources(), this, false);
        super.onCreate(savedInstanceState);
        ActivityNavigationDrawerBinding binding = ActivityNavigationDrawerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation_drawer);
        handleNotificationIntent(getIntent());

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
        progressBarMarkAttendance = binding.appBarNavigationDrawer.progressBarMarkAttendance;

        boolean isSubscribed = user.getSubscribedToTopicMealNotification();
        if (!isSubscribed) {
            FirebaseMessaging.getInstance().subscribeToTopic("/topics/meals").addOnCompleteListener(task -> {
                if (task.isSuccessful())
                    user.setSubscribedToTopicMealNotification(true);
                else {
                    Exception e = task.getException();
                    if (e != null)
                        Util.showAlertDialogMessage(this, getLayoutInflater(), getString(R.string.err), "Topic: " + e.getMessage(), "#E53935", null);
                }
            });
        }

        menuProvider = new MenuProvider() {
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
        };

        fabOpenCameraScannerQrCode.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                activityResultLauncher.launch(android.Manifest.permission.CAMERA);
            } else {
                openCameraScannerQrCodeEvent(new ScanOptions());
            }
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each.
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_cursu_list_meal)
                .setOpenableLayout(drawer)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navController.addOnDestinationChangedListener((navCont, navDestination, bundle) -> {
            if (navDestination.getId() == R.id.nav_home) {
                addMenuProvider(menuProvider, this);
                fabOpenCameraScannerQrCode.setVisibility(View.VISIBLE);
            } else {
                removeMenuProvider(menuProvider);
                fabOpenCameraScannerQrCode.setVisibility(View.INVISIBLE);
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

        asNotificationPermission();
        createNotificationChannel();
    }

    private void asNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED)
                requestPermissionLauncherNotification.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private final ActivityResultLauncher<String> requestPermissionLauncherNotification = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (!result)
                    Util.showAlertDialogBuild(getString(R.string.err), getString(R.string.msg_permis_notification_denied), context, null);
            }
    );

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Meals Channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Mensagem enviada quando uma refeição é criada.");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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

    private void openCameraScannerQrCodeEvent(@NonNull ScanOptions scanOptions) {
        scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        scanOptions.setPrompt(getString(R.string.align_camera_qr_code));
        scanOptions.setOrientationLocked(false); // unlock orientation of camera
        scanOptions.setCameraId(0);
        scanOptions.setBeepEnabled(true);
        scanOptions.setBarcodeImageEnabled(false); // Não capturar e retornar a imagem do código scaneado
        barScanOptionsActivityResultLauncher.launch(scanOptions);
    }

    private final ActivityResultLauncher<ScanOptions> barScanOptionsActivityResultLauncher = registerForActivityResult(new ScanContract(), result -> {
        String resultContents = result.getContents();
        if (resultContents == null) return;
        if (resultContents.isEmpty()) {
            Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), getString(R.string.msg_qr_code_invalid), "#FDD835", null);
        } else {
            if (resultContents.startsWith("cc42event")) {
                Util.setVisibleProgressBar(progressBarMarkAttendance, fabOpenCameraScannerQrCode, sharedViewModel);
                DaoEventFirebase.markAttendance(
                        firebaseDatabase,
                        resultContents.replace("cc42event", ""), /* id event*/
                        uid,
                        userLogin,
                        displayName,
                        cursusId,
                        campusId,
                        context,
                        getLayoutInflater(),
                        progressBarMarkAttendance,
                        fabOpenCameraScannerQrCode,
                        sharedViewModel,
                        null
                );
            } else if (resultContents.startsWith("cc42meal")) {
                Util.setVisibleProgressBar(progressBarMarkAttendance, fabOpenCameraScannerQrCode, sharedViewModel);
                DaoSusbscriptionFirebase.subscription(
                        firebaseDatabase,
                        resultContents.replace("cc42event", ""), /* id meal*/
                        uid,
                        userLogin,
                        displayName,
                        cursusId,
                        campusId,
                        context,
                        getLayoutInflater(),
                        progressBarMarkAttendance,
                        fabOpenCameraScannerQrCode,
                        sharedViewModel,
                        null
                );
            } else
                Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), getString(R.string.msg_qr_code_invalid), "#FDD835", null);
        }
    });

    public static class MyFirebaseMessagingService extends FirebaseMessagingService {
        @Override
        public void onMessageReceived(@NonNull RemoteMessage message) {
            super.onMessageReceived(message);
            if (message.getNotification() != null) {
                String title = message.getNotification().getTitle();
                String body = message.getNotification().getBody();
                if (message.getNotification().getImageUrl() != null) {
                    String imageUrl = message.getNotification().getImageUrl().toString();
                    Glide.with(this)
                            .asBitmap()
                            .load(imageUrl)
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                                    showNotification(title, body, bitmap, message, getApplicationContext(), imageUrl);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                    showNotification(title, body, null, message, getApplicationContext(), imageUrl);
                                }
                            });
                } else
                    showNotification(title, body, null, message, getApplicationContext(), "");
            }
        }

        @Override
        public void onNewToken(@NonNull String token) {
            super.onNewToken(token);
        }
    }

    private static void showNotification(String title, String body, Bitmap image, RemoteMessage message, Context context, String imageUrl) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_check_cadet_42)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        // Adicionar imagem a notificação
        if (image != null) {
            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(image)
                    .bigLargeIcon(null));
        }

        // Abrir o app ao clicar na notificação
        Intent intent = new Intent(context, NavigationDrawerActivity.class);

        if (message.getData().size() > 0) {
            String[] partsBody = body.split(": ", 2);
            String id = message.getData().get("key1");
            String dataCreated = message.getData().get("key2");
            String quantity = message.getData().get("key3");
            String cursusId = message.getData().get("key4");
            Meal meal = new Meal(id, title, partsBody[1], Integer.parseInt(quantity != null ? quantity : "0"), partsBody[0], dataCreated, imageUrl);

            // Entrar em fragment específico e passar dados da efeição
            intent.setAction("OPEN_FRAGMENT_ACTION_FOREGROUND");
            intent.putExtra("cursusId", Integer.parseInt(cursusId != null ? cursusId : "0"));
            intent.putExtra("detailsMeal", meal);
        }

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else
            pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent); // Definir a intenção

        // Exibir a notificaçào
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify((int) message.getSentTime(), builder.build());
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null &&
                ("OPEN_FRAGMENT_ACTION_FOREGROUND".equals(intent.getAction()) ||
                        "OPEN_FRAGMENT_ACTION_BACKGROUND".equals(intent.getAction()))) { // Primeiro plano
            int cursusId = (int) intent.getIntExtra("cursusId", 0);
            Meal meal = (Meal) intent.getParcelableExtra("detailsMeal");
            args = new Bundle();
            args.putInt("cursusId", cursusId);
            args.putParcelable("detailsMeal", meal);
            navController.navigate(R.id.action_detailsMealFragment, args);
            intent.replaceExtras(new Bundle());
            intent.setAction(null);
            args.clear();
        }
    }

    /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings_general, menu);
        return true;
    }*/

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNotificationIntent(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation_drawer);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (args != null)
            args.clear();
    }
}