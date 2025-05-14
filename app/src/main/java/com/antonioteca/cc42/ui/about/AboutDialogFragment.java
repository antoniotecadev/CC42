package com.antonioteca.cc42.ui.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentAboutDialogBinding;

public class AboutDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        FragmentAboutDialogBinding binding = FragmentAboutDialogBinding.inflate(getLayoutInflater());
        builder.setIcon(R.mipmap.ic_check_cadet_42);
        builder.setTitle(getString(R.string.app_name));
        builder.setPositiveButton(getString(R.string.ok), (dialog, id) -> dismiss());
        builder.setView(binding.getRoot());
        AlertDialog dialog = builder.create();
        // dialog.setCanceledOnTouchOutside(false);

        binding.profileIntraLink.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://profile.intra.42.fr/users/ateca"));
            startActivity(browserIntent);
        });

        binding.githubLink.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/antoniotecadev"));
            startActivity(browserIntent);
        });

        binding.emailLink.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "antonioteca@hotmail.com", null));
            startActivity(Intent.createChooser(emailIntent, "Enviar e-mail"));
        });
        return dialog;
    }
}