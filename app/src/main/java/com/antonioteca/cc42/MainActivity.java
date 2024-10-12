package com.antonioteca.cc42;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.antonioteca.cc42.databinding.ActivityMainBinding;
import com.antonioteca.cc42.factory.TokenViewModelFactory;
import com.antonioteca.cc42.factory.UserViewModelFactory;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.network.NetworkConstants;
import com.antonioteca.cc42.repository.TokenRepository;
import com.antonioteca.cc42.repository.UserRepository;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.TokenViewModel;
import com.antonioteca.cc42.viewmodel.UserViewModel;

public class MainActivity extends AppCompatActivity {

    private TokenViewModel tokenViewModel;
    private UserViewModel userViewModel;

    private boolean initOnNewIntent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        TokenRepository tokenRepository = new TokenRepository(this);
        UserRepository userRepository = new UserRepository(this);
        TokenViewModelFactory tokenViewModelFactory = new TokenViewModelFactory(tokenRepository);
        UserViewModelFactory userViewModelFactory = new UserViewModelFactory(userRepository);
        tokenViewModel = new ViewModelProvider(this, tokenViewModelFactory).get(TokenViewModel.class);
        userViewModel = new ViewModelProvider(this, userViewModelFactory).get(UserViewModel.class);

        tokenViewModel.getHttpSatus().observe(this, new Observer<HttpStatus>() {
            @Override
            public void onChanged(HttpStatus httpStatus) {
                if (initOnNewIntent) {
                    if (httpStatus == HttpStatus.OK)
                        userViewModel.getUser(MainActivity.this);
                    else
                        Util.showAlertDialogBuild(String.valueOf(httpStatus.getCode()), httpStatus.getDescription(), MainActivity.this);
                }
            }
        });

        tokenViewModel.getHttpException().observe(this, new Observer<HttpException>() {
            @Override
            public void onChanged(HttpException httpException) {
                if (initOnNewIntent)
                    Util.showAlertDialogBuild(String.valueOf(httpException.getCode()), httpException.getDescription(), MainActivity.this);
            }
        });

        userViewModel.getUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (initOnNewIntent) {
                    if (user != null) {
                        if (userViewModel.saveUser(user))
                            redirectToHome();
                        else
                            Util.showAlertDialogBuild(getString(R.string.err), getString(R.string.msg_err_save_sess_user), MainActivity.this);
                    } else
                        Util.showAlertDialogBuild(String.valueOf(HttpStatus.NOT_FOUND.getCode()), HttpStatus.NOT_FOUND.getDescription(), MainActivity.this);
                }
            }
        });

        userViewModel.getHttpSatus().observe(this, new Observer<HttpStatus>() {
            @Override
            public void onChanged(HttpStatus httpStatus) {
                if (initOnNewIntent)
                    Util.showAlertDialogBuild(String.valueOf(httpStatus.getCode()), httpStatus.getDescription(), MainActivity.this);
            }
        });

        userViewModel.getHttpException().observe(this, new Observer<HttpException>() {
            @Override
            public void onChanged(HttpException httpException) {
                if (initOnNewIntent)
                    Util.showAlertDialogBuild(String.valueOf(httpException.getCode()), httpException.getDescription(), MainActivity.this);
            }
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

    @Override
    protected void onResume() {
        super.onResume();
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
        }
    }
}