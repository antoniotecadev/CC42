package com.antonioteca.cc42.ui.splash;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.antonioteca.cc42.NavigationDrawerActivity;
import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentSplashBinding;
import com.antonioteca.cc42.factory.TokenViewModelFactory;
import com.antonioteca.cc42.factory.UserViewModelFactory;
import com.antonioteca.cc42.model.Token;
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.repository.TokenRepository;
import com.antonioteca.cc42.repository.UserRepository;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.TokenViewModel;
import com.antonioteca.cc42.viewmodel.UserViewModel;

public class SplashFragment extends Fragment {

    private FragmentSplashBinding binding;

    private TokenViewModel tokenViewModel;
    private UserViewModel userViewModel;

    private Context context;
    private boolean isSplashActive = true;

    public SplashFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        TokenRepository tokenRepository = new TokenRepository(context);
        UserRepository userRepository = new UserRepository(context);
        TokenViewModelFactory tokenViewModelFactory = new TokenViewModelFactory(tokenRepository);
        UserViewModelFactory userViewModelFactory = new UserViewModelFactory(userRepository);
        tokenViewModel = new ViewModelProvider(this, tokenViewModelFactory).get(TokenViewModel.class);
        userViewModel = new ViewModelProvider(this, userViewModelFactory).get(UserViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSplashBinding.inflate(inflater, container, false);
        // setColorCoalition(binding.splashFragment, new Coalition(context).getColor()); // cor de background
        tokenViewModel.getHttpSatus().observe(getViewLifecycleOwner(), new Observer<HttpStatus>() {
            @Override
            public void onChanged(HttpStatus httpStatus) {
                if (!isSplashActive) {
                    if (httpStatus == HttpStatus.OK)
                        redirectToHome();
                    else
                        redirectToLogin();
                }
            }
        });

        tokenViewModel.getHttpException().observe(getViewLifecycleOwner(), new Observer<HttpException>() {
            @Override
            public void onChanged(HttpException httpException) {
                if (!isSplashActive) {
                    Util.showAlertDialogBuild(String.valueOf(httpException.getCode()), httpException.getDescription(), context, null);
                    redirectToLogin();
                }
            }
        });
//        QUANDO LOGAR NO CLIENTE
//        userViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
//            @Override
//            public void onChanged(User user) {
//                if (!isSplashActive) {
//                    if (user != null) {
//                        if (userViewModel.saveUser(user))
//                            redirectToHome();
//                        else
//                            redirectToLogin();
//                    } else
//                        redirectToLogin();
//                }
//            }
//        });

        userViewModel.getHttpSatus().observe(getViewLifecycleOwner(), new Observer<HttpStatus>() {
            @Override
            public void onChanged(HttpStatus httpStatus) {
                if (!isSplashActive) {
                    Util.showAlertDialogBuild(String.valueOf(httpStatus.getCode()), httpStatus.getDescription(), context, null);
                    redirectToLogin();
                }
            }
        });

        userViewModel.getHttpException().observe(getViewLifecycleOwner(), new Observer<HttpException>() {
            @Override
            public void onChanged(HttpException httpException) {
                if (!isSplashActive) {
                    Util.showAlertDialogBuild(String.valueOf(httpException.getCode()), httpException.getDescription(), context, null);
                    redirectToLogin();
                }
            }
        });
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    private void redirectToHome() {
        try {
            Intent intent = new Intent(context, NavigationDrawerActivity.class);
            startActivity(intent);
            requireActivity().finish();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void redirectToLogin() {
        try {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_splashFragment_to_loginFragment);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isSplashActive = false;
            Token token = new Token(context);
            String refreshToken = token.getRefreshToken();
            if (token.getAccessToken() == null || refreshToken == null)
                redirectToLogin();
            else if (token.isTokenExpired(token.getTokenExpirationTime()))
                redirectToLogin();
            else
                redirectToLogin();
        }, 5000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}