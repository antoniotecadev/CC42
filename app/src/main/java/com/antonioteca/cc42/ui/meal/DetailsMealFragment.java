package com.antonioteca.cc42.ui.meal;

import static com.antonioteca.cc42.utility.Util.generateQrCode;
import static com.antonioteca.cc42.utility.Util.showModalQrCode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.antonioteca.cc42.databinding.FragmentDetailsMealBinding;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.utility.Util;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;


public class DetailsMealFragment extends Fragment {


    private Context context;
    private FragmentDetailsMealBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDetailsMealBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Meal meal = DetailsMealFragmentArgs.fromBundle(requireArguments()).getDetailsMeal();
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(String.valueOf(meal.getQuantity()));
        }
        Glide.with(this)
                .load(meal.getPathImage())
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        binding.linearLayoutCompatMeailDetails.setBackground(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Definir background placeholder caso a imagem não carregue
                    }
                });
        binding.textViewName.setText(meal.getName());
        binding.textViewDescription.setText(meal.getDescription());
        binding.textViewDate.setText(meal.getDate());
        Util.loadingImageMeal(context, meal.getPathImage(), binding.imageViewMeal);
        binding.fabGenerateQrCode.setOnClickListener(v -> {
            Bitmap bitmapQrCode = generateQrCode(view.getContext(), "meal" + meal.getId());
            showModalQrCode(context, bitmapQrCode, meal.getName(), meal.getDescription());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}