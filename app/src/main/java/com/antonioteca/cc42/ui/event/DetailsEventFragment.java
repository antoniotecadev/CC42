package com.antonioteca.cc42.ui.event;

import static com.antonioteca.cc42.utility.DateUtils.getDaysUntil;
import static com.antonioteca.cc42.utility.DateUtils.getEventDuration;
import static com.antonioteca.cc42.utility.DateUtils.getFormattedDate;
import static com.antonioteca.cc42.utility.DateUtils.parseDate;
import static com.antonioteca.cc42.utility.Util.setMarkdownText;

import android.annotation.SuppressLint;
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
import com.antonioteca.cc42.databinding.FragmentDetailsEventBinding;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Event;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
import com.antonioteca.cc42.utility.Loading;
import com.antonioteca.cc42.utility.StarUtils;
import com.antonioteca.cc42.viewmodel.EventViewModel;
import com.antonioteca.cc42.viewmodel.MealViewModel;
import com.antonioteca.cc42.viewmodel.SharedViewModel;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.Objects;

public class DetailsEventFragment extends Fragment {
    private User user;
    private int rating = 0;
    private Context context;
    private Loading loading;
    private boolean userIspresent;
    private MealViewModel mealViewModel;
    private EventViewModel eventViewModel;
    private SharedViewModel sharedViewModel;
    private FirebaseDatabase firebaseDatabase;
    private FragmentDetailsEventBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        loading = new Loading();
        user = new User(context);
        user.coalition = new Coalition(context);
        firebaseDatabase = FirebaseDataBaseInstance.getInstance().database;
        mealViewModel = new ViewModelProvider(this).get(MealViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDetailsEventBinding.inflate(inflater, container, false);
        if (!user.isStaff()) {
            binding.fabGenerateQrCode.setVisibility(View.GONE);
            binding.fabOpenAttendanceList.setVisibility(View.GONE);
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
        Event event = DetailsEventFragmentArgs.fromBundle(requireArguments()).getDetailsEvent();
        String type = "events";
        long userId = user.getUid();
        String eventId = String.valueOf(event.getId());
        int campusId = user.getCampusId();
        int cursusId = event.getCursus_ids().get(0);

        eventViewModel.getUserIsPresent(context, getLayoutInflater(), firebaseDatabase, String.valueOf(campusId), String.valueOf(cursusId), eventId, String.valueOf(userId))
                .observe(getViewLifecycleOwner(), userIspresent -> {
                    if (userIspresent) {
                        this.userIspresent = true;
                        binding.starRating.getRoot().setVisibility(View.VISIBLE);
                    } else {
                        binding.textViewTapToRate.setTextColor(context.getResources().getColor(R.color.red));
                        binding.textViewTapToRate.setText(R.string.text_absent);
                    }
                });

        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(event.getKind().toUpperCase());
        }

        String colorCoalition = user.coalition.getColor();
        if (colorCoalition != null) {
            ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(colorCoalition));
            binding.progressBarEvent.setIndeterminateTintList(colorStateList);
            binding.buttonSendComment.setBackgroundTintList(colorStateList);
        }

        Date eventDateBegin = parseDate(event.getBegin_at());
        Date eventDateEnd = parseDate(event.getEnd_at());
        String day = getFormattedDate(eventDateBegin, "d");
        String month = getFormattedDate(eventDateBegin, "MMMM");
        String time = getFormattedDate(eventDateBegin, "hh:mm a");
        String year = getFormattedDate(eventDateBegin, "yyyy");
        String daysUntil = getDaysUntil(eventDateBegin);
        binding.textViewKind.setText(event.getKind());
        binding.textViewName.setText(event.getName());
        String textDate = month + " " + day + ", " + year + " at " + time;
        binding.textViewDate.setText(textDate);
        binding.textViewDuraction.setText(getEventDuration(eventDateBegin, eventDateEnd));
        binding.textViewDays.setText(daysUntil);
        binding.textViewLocation.setText(event.getLocation());
        String textPeople = event.getNbr_subscribers() + " / " + event.getMax_people();
        binding.textViewPeople.setText(textPeople);
        setMarkdownText(binding.textViewDescription, event.getDescription());

        if (System.currentTimeMillis() < Objects.requireNonNullElse(eventDateEnd, new Date()).getTime()) {
            binding.starRatingDone.getRoot().setVisibility(View.GONE);
            binding.starRatingDoneContainer.setVisibility(View.GONE);
            binding.numberOfRatings.setVisibility(View.GONE);
            binding.textViewTapToRate.setVisibility(View.GONE);
            binding.starRating.getRoot().setVisibility(View.GONE);
        }

        mealViewModel.getRatingValuesLiveData(context, firebaseDatabase, binding.progressBarEvent, String.valueOf(user.getCampusId()), String.valueOf(cursusId), type, eventId)
                .observe(getViewLifecycleOwner(),
                        ratingValues -> {
                            if (!ratingValues.isEmpty())
                                StarUtils.getRate(
                                        context,
                                        userId,
                                        false,
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
                                        eventId,
                                        rating,
                                        firebaseDatabase,
                                        binding.progressBarEvent,
                                        mealViewModel);
                            binding.progressBarEvent.setVisibility(View.INVISIBLE);
                        });

        // Configura os cliques das estrelas
        binding.starRating.star1.setOnClickListener(v -> rating = StarUtils.fillStars(binding.starRating, 1, null, true, context, loading, userId, campusId, cursusId, type, eventId, rating, firebaseDatabase, binding.progressBarEvent, mealViewModel));
        binding.starRating.star2.setOnClickListener(v -> rating = StarUtils.fillStars(binding.starRating, 2, null, true, context, loading, userId, campusId, cursusId, type, eventId, rating, firebaseDatabase, binding.progressBarEvent, mealViewModel));
        binding.starRating.star3.setOnClickListener(v -> rating = StarUtils.fillStars(binding.starRating, 3, null, true, context, loading, userId, campusId, cursusId, type, eventId, rating, firebaseDatabase, binding.progressBarEvent, mealViewModel));
        binding.starRating.star4.setOnClickListener(v -> rating = StarUtils.fillStars(binding.starRating, 4, null, true, context, loading, userId, campusId, cursusId, type, eventId, rating, firebaseDatabase, binding.progressBarEvent, mealViewModel));
        binding.starRating.star5.setOnClickListener(v -> rating = StarUtils.fillStars(binding.starRating, 5, null, true, context, loading, userId, campusId, cursusId, type, eventId, rating, firebaseDatabase, binding.progressBarEvent, mealViewModel));

        // Obter comentário
        sharedViewModel.getCommentLiveData(context, firebaseDatabase, type, eventId, String.valueOf(campusId), String.valueOf(cursusId), String.valueOf(userId))
                .observe(getViewLifecycleOwner(), comment -> {
                    if (comment != null && !comment.isEmpty()) {
                        binding.textViewComment.setText(comment);
                        binding.textViewComment.setVisibility(View.VISIBLE);
                        binding.commentInputLayout.setVisibility(View.GONE);
                        binding.buttonSendComment.setVisibility(View.GONE);
                    } else if (comment == null && userIspresent) {
                        binding.commentInputLayout.setVisibility(View.VISIBLE);
                        binding.buttonSendComment.setVisibility(View.VISIBLE);
                    }
                });

        binding.buttonSendComment.setOnClickListener(v -> sharedViewModel.sendComment(context, firebaseDatabase, type, eventId, String.valueOf(campusId), String.valueOf(cursusId), String.valueOf(userId), binding.buttonSendComment, binding.commentInputLayout, binding.progressBarEvent));

        binding.fabGenerateQrCode.setOnClickListener(v -> {
            try {
                DetailsEventFragmentDirections.ActionDetailsEventFragmentToQrCodeFragment actionDetailsEventFragmentToQrCodeFragment = DetailsEventFragmentDirections.actionDetailsEventFragmentToQrCodeFragment("event" + event.getId() + "#" + user.getUid(), event.getKind(), event.getName(), user.getCampusId(), event.getCursus_ids().get(0));
                navController.navigate(actionDetailsEventFragmentToQrCodeFragment);
            } catch (IllegalArgumentException ignored) {
            }
        });

        binding.fabOpenAttendanceList.setOnClickListener(v -> {
            try {
                DetailsEventFragmentDirections.ActionDetailsEventFragmentToAttendanceListFragment actionDetailsEventFragmentToAttendanceListFragment = DetailsEventFragmentDirections.actionDetailsEventFragmentToAttendanceListFragment(event.getId(), event.getCursus_ids().get(0), event.getKind(), event.getName(), String.valueOf(binding.textViewDate.getText()));
                navController.navigate(actionDetailsEventFragmentToAttendanceListFragment);
            } catch (IllegalArgumentException ignored) {
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
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        };
        if (user.isStaff())
            requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner());
        else
            requireActivity().removeMenuProvider(menuProvider);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}