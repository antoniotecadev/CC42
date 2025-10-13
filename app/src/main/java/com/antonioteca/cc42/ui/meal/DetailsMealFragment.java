package com.antonioteca.cc42.ui.meal;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentDetailsMealBinding;
import com.antonioteca.cc42.databinding.StarRatingBinding;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
import com.antonioteca.cc42.utility.Loading;
import com.antonioteca.cc42.utility.MealsUtils;
import com.antonioteca.cc42.utility.StarUtils;
import com.antonioteca.cc42.viewmodel.MealViewModel;
import com.antonioteca.cc42.viewmodel.SharedViewModel;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class DetailsMealFragment extends Fragment {


    private User user;
    private int rating = 0;
    private Loading loading;
    private Context context;
    private MealViewModel mealViewModel;
    private SharedViewModel sharedViewModel;
    private HashMap<?, ?> ratingValuesUsers;
    private FirebaseDatabase firebaseDatabase;
    private FragmentDetailsMealBinding binding;
    RoundedCorners roundedCorners = new RoundedCorners(5);
    RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.ic_baseline_restaurant_60);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        loading = new Loading();
        user = new User(context);
        user.coalition = new Coalition(context);
        firebaseDatabase = FirebaseDataBaseInstance.getInstance().database;
        mealViewModel = new ViewModelProvider(this).get(MealViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetailsMealBinding.inflate(inflater, container, false);
        if (!user.isStaff()) {
            binding.fabGenerateQrCode.setVisibility(View.GONE);
            binding.fabOpenSubscriptionList.setVisibility(View.GONE);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StarUtils.loadStarZero(context, binding.recyclerViewRating);
        StarUtils.setColorCoalitionStar(binding.starRating, user);
        StarUtils.reduceStarSize(context, binding.starRatingDone, 30, 30);

        NavController navController = Navigation.findNavController(view);
        DetailsMealFragmentArgs args = DetailsMealFragmentArgs.fromBundle(requireArguments());
        Meal meal = args.getDetailsMeal();
        String type = "meals";
        String mealName = meal.getName();
        String mealDescription = meal.getDescription();
        boolean isSubscribed = meal.isSubscribed();
        long userId = user.getUid();
        String mealId = meal.getId();
        int campusId = user.getCampusId();
        int cursusId = args.getCursusId();

        String ratingCount = getString(R.string._0) + " " + getString(R.string.ratingsCount);
        binding.numberOfRatings.setText(ratingCount);
        mealViewModel.getRatingValuesLiveData(context, firebaseDatabase, binding.progressBarMeal, String.valueOf(campusId), String.valueOf(cursusId), type, mealId)
                .observe(getViewLifecycleOwner(),
                        ratingValues -> {
                            if (isSubscribed)
                                binding.starRating.getRoot().setVisibility(View.VISIBLE);
                            if (ratingValues.isEmpty()) {
                                if (!isSubscribed) {
                                    binding.textViewYourRatingAndComment.setVisibility(View.GONE);
                                    binding.textViewRateWithStars.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                                    binding.textViewRateWithStars.setText(R.string.needCheckinToRate);
                                    binding.textViewRateWithStars.setTextSize(14);
                                }
                            } else
                                ratingValuesUsers = StarUtils.getRate(
                                        context,
                                        userId,
                                        isSubscribed,
                                        ratingValues,
                                        binding.starRatingDone,
                                        binding.starRating,
                                        binding.textViewRateWithStars,
                                        binding.numberOfRatings,
                                        binding.averageRating,
                                        binding.recyclerViewRating,
                                        loading,
                                        type,
                                        rating);
                            binding.progressBarMeal.setVisibility(View.INVISIBLE);
                        });

        if (meal.isNotification) {
            binding.fabGenerateQrCode.setVisibility(View.GONE);
            binding.fabOpenSubscriptionList.setVisibility(View.GONE);
            mealViewModel.getUserIsSubscribed(context, getLayoutInflater(), firebaseDatabase, String.valueOf(campusId), String.valueOf(cursusId), mealId, String.valueOf(userId))
                    .observe(getViewLifecycleOwner(), isSubscribed1 -> {
                        if (isSubscribed1)
                            binding.starRating.getRoot().setVisibility(View.VISIBLE);
                    });
        }

        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(meal.getType());
        }

        String colorCoalition = user.coalition.getColor();
        if (colorCoalition != null) {
            ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(colorCoalition));
            binding.progressBarMeal.setIndeterminateTintList(colorStateList);
            binding.buttonSendComment.setBackgroundTintList(colorStateList);
        }

        binding.textViewType.setText(meal.getType());
        binding.textViewName.setText(mealName);
        binding.textViewDescription.setText(mealDescription);
        binding.textViewDate.setText(meal.getCreatedDate());
        String quantity = getString(R.string.quantity) + ": " + meal.getQuantityNotReceived();
        binding.textViewQuantity.setText(quantity);
        MealsUtils.loadingImageMeal(context, meal.getPathImage(), binding.imageViewMeal, roundedCorners, requestOptions);

        sharedViewModel.getResetLiveData().observe(getViewLifecycleOwner(), reset -> {
            if (reset) {
                this.rating = 0;
                binding.buttonSendComment.setText(R.string.sendComment);
            }
        });

        binding.starRating.star1.setOnClickListener(v -> markStar(1, binding.starRating));
        binding.starRating.star2.setOnClickListener(v -> markStar(2, binding.starRating));
        binding.starRating.star3.setOnClickListener(v -> markStar(3, binding.starRating));
        binding.starRating.star4.setOnClickListener(v -> markStar(4, binding.starRating));
        binding.starRating.star5.setOnClickListener(v -> markStar(5, binding.starRating));

        binding.commentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendButtonText();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.commentEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.anonymousCommentCheckBox.setVisibility(View.VISIBLE);
            }
        });

        // Obter comentário
        sharedViewModel.getCommentLiveData(context, firebaseDatabase, type, mealId, String.valueOf(campusId), String.valueOf(cursusId), String.valueOf(userId))
                .observe(getViewLifecycleOwner(), comment -> {
                    if (comment != null && !comment.getComment().isEmpty()) {
                        binding.textViewComment.setText(comment.isAnonymous() ? "(" + getString(R.string.anonymousComment) + ")\n" + comment.getComment() : "(" + getString(R.string.comment) + ")\n" + comment.getComment());
                        binding.textViewComment.setVisibility(View.VISIBLE);
                        binding.commentInputLayout.setVisibility(View.GONE);
                        binding.buttonSendComment.setVisibility(View.GONE);
                    } else if (comment == null && isSubscribed) {
                        binding.commentInputLayout.setVisibility(View.VISIBLE);
                        binding.buttonSendComment.setVisibility(View.VISIBLE);
                    }
                    binding.anonymousCommentCheckBox.setVisibility(View.GONE);
                });

        mealViewModel.hasSecondPortion(context, firebaseDatabase, binding.buttonSubscribeSecondPortion, String.valueOf(campusId), String.valueOf(cursusId), String.valueOf(mealId), String.valueOf(userId));

        binding.buttonSendComment.setOnClickListener(v -> {
            boolean hasComment = !Objects.requireNonNull(binding.commentEditText.getText()).toString().isEmpty();
            boolean hasRating = rating > 0;

            if (hasRating && !hasComment) {
                sharedViewModel.rate(
                        context,
                        firebaseDatabase,
                        loading,
                        binding.progressBarMeal,
                        String.valueOf(campusId),
                        String.valueOf(cursusId),
                        type,
                        mealId,
                        String.valueOf(userId),
                        this.rating
                );
            } else if (!hasRating && hasComment) {
                boolean isAnonymous = binding.anonymousCommentCheckBox.isChecked();
                sharedViewModel.sendComment(context, firebaseDatabase, type, mealId, String.valueOf(campusId), String.valueOf(cursusId), String.valueOf(userId), binding.buttonSendComment, binding.commentInputLayout, isAnonymous, binding.progressBarMeal);
            } else if (hasRating) {
                Toast.makeText(context, R.string.sendRatingAndComment, Toast.LENGTH_SHORT).show();
            }
        });

        binding.fabGenerateQrCode.setOnClickListener(v -> {
            try {
                rating = 0; // Para poder mostrar a classificação, ao voltar <-
                DetailsMealFragmentDirections.ActionDetailsMealFragmentToQrCodeFragment actionDetailsMealFragmentToQrCodeFragment = DetailsMealFragmentDirections.actionDetailsMealFragmentToQrCodeFragment("meal" + mealId + "#" + userId, mealName, Objects.requireNonNullElse(mealDescription, ""), campusId, cursusId);
                navController.navigate(actionDetailsMealFragmentToQrCodeFragment);
            } catch (IllegalArgumentException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.fabOpenSubscriptionList.setOnClickListener(v -> {
            try {
                rating = 0; // Para poder mostrar a classificação, ao voltar <-
                DetailsMealFragmentDirections.ActionDetailsMealFragmentToSubscriptionListFragment actionDetailsMealFragmentToSubscriptionListFragment = DetailsMealFragmentDirections.actionDetailsMealFragmentToSubscriptionListFragment(meal, cursusId).setRatingValuesUsers(ratingValuesUsers);
                navController.navigate(actionDetailsMealFragmentToSubscriptionListFragment);
            } catch (IllegalArgumentException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.buttonSubscribeSecondPortion.setOnClickListener(v -> mealViewModel.subscribeSecondPortion(context, firebaseDatabase, String.valueOf(campusId), String.valueOf(cursusId), String.valueOf(mealId), mealName, meal.getPathImage(), String.valueOf(userId), binding.buttonSubscribeSecondPortion, binding.progressBarMeal, true));

        MenuProvider menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_details, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_navigation_drawer);
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        };
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner());
    }

    private void markStar(int selectedRating, StarRatingBinding starRatingBinding) {
        StarUtils.resetStars(starRatingBinding); // Reseta todas as estrelas
        StarUtils.fillStars(starRatingBinding, selectedRating, null, loading, this.rating); // Preenche a estrela selecionada)
        this.rating = selectedRating;
        updateSendButtonText();
    }

    private void updateSendButtonText() {
        boolean hasComment = !Objects.requireNonNull(binding.commentEditText.getText()).toString().isEmpty();
        boolean hasRating = rating > 0;

        if (hasRating && !hasComment) {
            binding.buttonSendComment.setText(R.string.sendRating);
        } else if (!hasRating && hasComment) {
            binding.buttonSendComment.setText(R.string.sendComment);
        } else if (hasRating) { // Implies hasComment is true, or we want this text anyway if both are present
            binding.buttonSendComment.setText(R.string.sendRatingAndComment);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}