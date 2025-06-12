package com.antonioteca.cc42;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.antonioteca.cc42.databinding.ActivityMainBinding;
import com.antonioteca.cc42.factory.TokenViewModelFactory;
import com.antonioteca.cc42.factory.UserViewModelFactory;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.model.Token;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.network.NetworkConstants;
import com.antonioteca.cc42.repository.TokenRepository;
import com.antonioteca.cc42.repository.UserRepository;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.TokenViewModel;
import com.antonioteca.cc42.viewmodel.UserViewModel;

public class MainActivity extends AppCompatActivity {

    private Token token;
    private TokenViewModel tokenViewModel;
    private UserViewModel userViewModel;

    private boolean initOnNewIntent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        token = new Token(this);
        TokenRepository tokenRepository = new TokenRepository(this);
        UserRepository userRepository = new UserRepository(this);
        TokenViewModelFactory tokenViewModelFactory = new TokenViewModelFactory(tokenRepository);
        UserViewModelFactory userViewModelFactory = new UserViewModelFactory(userRepository);
        tokenViewModel = new ViewModelProvider(this, tokenViewModelFactory).get(TokenViewModel.class);
        userViewModel = new ViewModelProvider(this, userViewModelFactory).get(UserViewModel.class);
        try {
            handleNotificationIntent(getIntent());
        } catch (Exception e) {
            Util.showAlertDialogBuild(getString(R.string.err), e.getMessage(), MainActivity.this, null);
        }
        tokenViewModel.getHttpSatus().observe(this, httpStatus -> {
            if (initOnNewIntent) {
                if (httpStatus == HttpStatus.OK)
                    userViewModel.getUser(MainActivity.this);
                else
                    Util.showAlertDialogBuild(String.valueOf(httpStatus.getCode()), httpStatus.getDescription(), MainActivity.this, null);
            }
        });

        tokenViewModel.getHttpException().observe(this, httpException -> {
            if (initOnNewIntent)
                Util.showAlertDialogBuild(String.valueOf(httpException.getCode()), httpException.getDescription(), MainActivity.this, null);
        });

        userViewModel.getUser().observe(this, user -> {
            if (initOnNewIntent) {
                if (user != null) {
                    if (userViewModel.saveUser(user))
                        redirectToHome();
                    else
                        Util.showAlertDialogBuild(getString(R.string.err), getString(R.string.msg_err_save_sess_user), MainActivity.this, null);
                } else
                    Util.showAlertDialogBuild(String.valueOf(HttpStatus.NOT_FOUND.getCode()), HttpStatus.NOT_FOUND.getDescription(), MainActivity.this, null);
            }
        });

        userViewModel.getHttpSatus().observe(this, httpStatus -> {
            if (initOnNewIntent)
                Util.showAlertDialogBuild(String.valueOf(httpStatus.getCode()), httpStatus.getDescription(), MainActivity.this, null);
        });

        userViewModel.getHttpException().observe(this, httpException -> {
            if (initOnNewIntent)
                Util.showAlertDialogBuild(String.valueOf(httpException.getCode()), httpException.getDescription(), MainActivity.this, null);
        });
    }

    private void redirectToHome() {
        Intent intent = new Intent(this, NavigationDrawerActivity.class);
        startActivity(intent);
        finish();
    }

    private void redirectToLogin(int fragmentId) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_container);
        // limpar loginFragment_nav da pilha de navegação
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.splashFragment_nav, true)
                .build();
        navController.navigate(fragmentId, null, navOptions);
    }

    private void handleNotificationIntent(Intent intent) { // Segundo plano
        if (intent != null && intent.getExtras() != null && token.getAccessToken() != null && !token.isTokenExpired(token.getTokenExpirationTime())) {
            String targetFragment = intent.getStringExtra("key5");
            if ("DetailsMealFragment".equals(targetFragment)) {

                String body = intent.getStringExtra("body");
                String title = intent.getStringExtra("title");
                String imageUrl = intent.getStringExtra("image");

                String id = intent.getStringExtra("key0");
                String createdBy = intent.getStringExtra("key1");
                String createdData = intent.getStringExtra("key2");
                String quantity = intent.getStringExtra("key3");
                String cursusId = intent.getStringExtra("key4");
                String description = intent.getStringExtra("key6");
                Meal meal = new Meal(id, title, body, description, Integer.parseInt(quantity != null ? quantity : "0"), imageUrl, 0, createdBy, createdData);

                Intent i = new Intent(this, NavigationDrawerActivity.class);
                i.setAction("OPEN_FRAGMENT_ACTION_BACKGROUND");
                i.putExtra("cursusId", Integer.parseInt(cursusId));
                i.putExtra("detailsMeal", meal);
                startActivity(i);
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Util.setColorStatusBar(this, Color.parseColor("#AFC9F1"));
        int fragmentId = getIntent().getIntExtra("fragment_id", -1);
        if (fragmentId != -1)
            redirectToLogin(fragmentId);
    }

    // capturar código de autorização do redirecionamento
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (uri != null && uri.toString().startsWith(NetworkConstants.SCHEME_HOST)) {
            initOnNewIntent = true;
            String code = uri.getQueryParameter(NetworkConstants.CODE);
            tokenViewModel.getAccessTokenUser(code, this);
        } else {
            try {
                handleNotificationIntent(intent);
            } catch (Exception e) {
                Util.showAlertDialogBuild(getString(R.string.err), e.getMessage(), MainActivity.this, null);
            }
        }
    }
}