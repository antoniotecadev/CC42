package com.antonioteca.cc42.ui.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentLoginBinding;
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
//        setColorCoalition(binding.loginFragment, new Coalition(requireContext()).getColor());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Button buttonSignIn = binding.buttonSignIn;
        Uri videoUri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.qr_code_phone_gif);
        binding.videoViewQrCodePhoneGif.setVideoURI(videoUri);
        binding.videoViewQrCodePhoneGif.setOnPreparedListener(player -> {
            player.setLooping(true); // Loop infinito
            binding.videoViewQrCodePhoneGif.start(); // Iniciar o vídeoautomaticamente
        });
        String colorText = "<font color='#419259'><b>SIGN</b></font> <font color='#DFB50D'><b>IN</b></font>";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            buttonSignIn.setText(Html.fromHtml(colorText, Html.FROM_HTML_MODE_LEGACY));
        else
            buttonSignIn.setText(Html.fromHtml(colorText));
        buttonSignIn.setOnClickListener(viewOnClick -> signIn());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding.videoViewQrCodePhoneGif.isPlaying())
            binding.videoViewQrCodePhoneGif.stopPlayback(); // Parar a reprodução
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}