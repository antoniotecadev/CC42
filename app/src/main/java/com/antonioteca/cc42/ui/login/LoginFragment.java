package com.antonioteca.cc42.ui.login;

import static com.antonioteca.cc42.utility.Util.setColorCoalition;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.antonioteca.cc42.databinding.FragmentLoginBinding;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.network.NetworkConstants;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    private void signIn() {
        String url = NetworkConstants.BASE_URL + NetworkConstants.OAUTH_AUTHORIZE_ENDPOINT;
        Uri uri = Uri.parse(url)
                .buildUpon()
                .appendQueryParameter("client_id", NetworkConstants.UID)
                .appendQueryParameter("redirect_uri", NetworkConstants.SCHEME_HOST)
                .appendQueryParameter("response_type", NetworkConstants.CODE)
                .appendQueryParameter("scope", "public")
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        setColorCoalition(binding.loginFragment, new Coalition(requireContext()).getColor());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Button loginButton = binding.login;
        loginButton.setOnClickListener(viewOnClick -> signIn());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}