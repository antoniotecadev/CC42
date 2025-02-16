package com.antonioteca.cc42.ui.meal;

import static com.antonioteca.cc42.utility.Util.generateQrCodeWithLogo;
import static com.antonioteca.cc42.utility.Util.showModalQrCode;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentDetailsMealBinding;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.utility.Util;


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
        DetailsMealFragmentArgs args = DetailsMealFragmentArgs.fromBundle(requireArguments());
        Meal meal = args.getDetailsMeal();
        int cursusId = args.getCursusId();
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(meal.getType() + " (" + meal.getQuantity() + ")");
        }
        binding.textViewName.setText(meal.getName());
        binding.textViewDescription.setText(meal.getDescription());
        binding.textViewDate.setText(meal.getDate());
        Util.loadingImageMeal(context, meal.getPathImage(), binding.imageViewMeal, true);
        binding.fabGenerateQrCode.setOnClickListener(v -> {
            Bitmap bitmapQrCode = generateQrCodeWithLogo(view.getContext(), "meal" + meal.getId());
            showModalQrCode(context, bitmapQrCode, meal.getName(), meal.getDescription());
        });
        binding.fabOpenSubscriptionList.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.subscriptionListFragment) {
                DetailsMealFragmentDirections.ActionDetailsMealFragmentToSubscriptionListFragment actionDetailsMealFragmentToSubscriptionListFragment =
                        DetailsMealFragmentDirections.actionDetailsMealFragmentToSubscriptionListFragment(meal, cursusId);
                Navigation.findNavController(v).navigate(actionDetailsMealFragmentToSubscriptionListFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}