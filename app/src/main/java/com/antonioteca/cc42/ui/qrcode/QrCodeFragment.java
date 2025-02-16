package com.antonioteca.cc42.ui.qrcode;

import static com.antonioteca.cc42.utility.Util.generateQrCodeWithLogo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.antonioteca.cc42.databinding.FragmentQrCodeBinding;

public class QrCodeFragment extends Fragment {

    private FragmentQrCodeBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentQrCodeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        QrCodeFragmentArgs args = QrCodeFragmentArgs.fromBundle(getArguments());
        String content = args.getContent();
        String title = args.getTitle();
        String description = args.getDescription();
        Bitmap bitmapQrCode = generateQrCodeWithLogo(view.getContext(), content);
        binding.textViewTitle.setText(title);
        binding.textViewDescription.setText(description);
        binding.imageViewQrCode.setImageBitmap(bitmapQrCode);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}