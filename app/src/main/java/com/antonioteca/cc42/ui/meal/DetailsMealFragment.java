package com.antonioteca.cc42.ui.meal;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
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
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
import com.antonioteca.cc42.utility.DateUtils;
import com.antonioteca.cc42.utility.Loading;
import com.antonioteca.cc42.utility.MealsUtils;
import com.antonioteca.cc42.utility.StarUtils;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.MealViewModel;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DetailsMealFragment extends Fragment {


    private User user;
    private int rating = 0;
    private Loading loading;
    private Context context;
    private DatabaseReference refMeals;
    private MealViewModel mealViewModel;
    private HashMap<?, ?> ratingValuesUsers;
    private FirebaseDatabase firebaseDatabase;
    private FragmentDetailsMealBinding binding;
    private ValueEventListener valueEventListener;
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

        mealViewModel.getRatingValuesLiveData(context, firebaseDatabase, binding.progressBarMeal, String.valueOf(campusId), String.valueOf(cursusId), type, mealId)
                .observe(getViewLifecycleOwner(),
                        ratingValues -> {
                            if (isSubscribed)
                                binding.starRating.getRoot().setVisibility(View.VISIBLE);
                            if (ratingValues.isEmpty()) {
                                if (!isSubscribed) {
                                    binding.textViewTapToRate.setTextColor(context.getResources().getColor(R.color.red));
                                    binding.textViewTapToRate.setText(R.string.text_unsigned);
                                }
                            } else
                                ratingValuesUsers = StarUtils.getRate(
                                        context,
                                        userId,
                                        isSubscribed,
                                        ratingValues,
                                        binding.starRatingDone,
                                        binding.starRating,
                                        binding.textViewTapToRate,
                                        binding.numberOfRatings,
                                        binding.averageRating,
                                        binding.recyclerViewRating,
                                        loading,
                                        campusId,
                                        cursusId,
                                        type,
                                        mealId,
                                        rating,
                                        firebaseDatabase,
                                        binding.progressBarMeal,
                                        mealViewModel);
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
        }

        binding.textViewType.setText(meal.getType());
        binding.textViewName.setText(mealName);
        binding.textViewDescription.setText(mealDescription);
        binding.textViewDate.setText(DateUtils.formatDate(DateUtils.parseDate(meal.getCreatedDate())));
        binding.textViewQuantity.setText(context.getString(R.string.quantity) + ": " + meal.getQuantity());
        MealsUtils.loadingImageMeal(context, meal.getPathImage(), binding.imageViewMeal, roundedCorners, requestOptions);
        // Configura os cliques das estrelas
        binding.starRating.star1.setOnClickListener(v -> rating = StarUtils.fillStars(binding.starRating, 1, null, true, context, loading, userId, campusId, cursusId, type, mealId, rating, firebaseDatabase, binding.progressBarMeal, mealViewModel));
        binding.starRating.star2.setOnClickListener(v -> rating = StarUtils.fillStars(binding.starRating, 2, null, true, context, loading, userId, campusId, cursusId, type, mealId, rating, firebaseDatabase, binding.progressBarMeal, mealViewModel));
        binding.starRating.star3.setOnClickListener(v -> rating = StarUtils.fillStars(binding.starRating, 3, null, true, context, loading, userId, campusId, cursusId, type, mealId, rating, firebaseDatabase, binding.progressBarMeal, mealViewModel));
        binding.starRating.star4.setOnClickListener(v -> rating = StarUtils.fillStars(binding.starRating, 4, null, true, context, loading, userId, campusId, cursusId, type, mealId, rating, firebaseDatabase, binding.progressBarMeal, mealViewModel));
        binding.starRating.star5.setOnClickListener(v -> rating = StarUtils.fillStars(binding.starRating, 5, null, true, context, loading, userId, campusId, cursusId, type, mealId, rating, firebaseDatabase, binding.progressBarMeal, mealViewModel));

        refMeals = firebaseDatabase.getReference("challenge")
                .child("meals")
                .child(mealId);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && binding != null && getContext() != null) {
                    boolean isStart = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                    if (isStart) {
                        binding.btnChallenge.setVisibility(View.VISIBLE);
                        binding.btnFinishedChallenge.setVisibility(View.VISIBLE);
                        if (!mealViewModel.isView) {
                            mealViewModel.isView = true;
                            String message = getString(R.string.msg_sucess_challenge_started_description);
                            Util.showAlertDialogMessage(context, getLayoutInflater(), getString(R.string.msg_sucess_challenge_started), message, "#4CAF50", user.getImage(), null);
                        }
                    } else {
                        binding.btnChallenge.setVisibility(View.GONE);
                        binding.btnFinishedChallenge.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Util.showAlertDialogBuild(getString(R.string.err), error.getMessage(), context, null);
            }
        };
        refMeals.child("start_challenge").addValueEventListener(valueEventListener);

        binding.btnChallenge.setOnClickListener(v -> {
            DetailsMealFragmentDirections.ActionDetailsMealFragmentToChallengeFragment actionDetailsMealFragmentToChallengeFragment = DetailsMealFragmentDirections.actionDetailsMealFragmentToChallengeFragment(mealId);
            navController.navigate(actionDetailsMealFragmentToChallengeFragment);
        });

        binding.btnFinishedChallenge.setOnClickListener(v -> Util.showAlertDialogBuild(getString(R.string.challenge), getString(R.string.message_finished_challenge), context, () -> {
            DatabaseReference refMeals = firebaseDatabase.getReference("challenge")
                    .child("meals")
                    .child(meal.getId());

            Map<String, Object> updates = new HashMap<>();
            updates.put("start_challenge", false);
            refMeals.updateChildren(updates).addOnSuccessListener(aVoid -> Util.showAlertDialogBuild(getString(R.string.challenge), getString(R.string.msg_sucess_challenge_finished_description), context, null)).addOnFailureListener(e -> {
                String message = context.getString(R.string.msg_error_finished_challenge) + ": " + e.getMessage();
                Util.showAlertDialogBuild(getString(R.string.err), message, context, null);
            });
        }));

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

        MenuProvider menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_details, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_navigation_drawer);
//                if (menuItem.getItemId() == R.id.action_register_face_id_camera_front) {
//                    DetailsMealFragmentDirections.ActionDetailsMealFragmentToFaceRecognitionFragment actionDetailsMealFragmentToFaceRecognitionFragment
//                            = DetailsMealFragmentDirections.actionDetailsMealFragmentToFaceRecognitionFragment(true, 1, String.valueOf(campusId), String.valueOf(cursusId));
//                    Navigation.findNavController(view).navigate(actionDetailsMealFragmentToFaceRecognitionFragment);
//                } else if (menuItem.getItemId() == R.id.action_register_face_id_camera_back) {
//                    DetailsMealFragmentDirections.ActionDetailsMealFragmentToFaceRecognitionFragment actionDetailsMealFragmentToFaceRecognitionFragment
//                            = DetailsMealFragmentDirections.actionDetailsMealFragmentToFaceRecognitionFragment(true, 0, String.valueOf(campusId), String.valueOf(cursusId));
//                    Navigation.findNavController(view).navigate(actionDetailsMealFragmentToFaceRecognitionFragment);
//                }
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        };
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (refMeals != null && valueEventListener != null)
            refMeals.removeEventListener(valueEventListener);
    }
}