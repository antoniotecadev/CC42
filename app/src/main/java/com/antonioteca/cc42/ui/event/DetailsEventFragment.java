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
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentDetailsEventBinding;
import com.antonioteca.cc42.databinding.StarRatingBinding;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Event;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
import com.antonioteca.cc42.ui.meal.RatingProgressAdapter;
import com.antonioteca.cc42.ui.meal.RatingProgressItem;
import com.antonioteca.cc42.utility.Loading;
import com.antonioteca.cc42.utility.StarUtils;
import com.antonioteca.cc42.viewmodel.MealViewModel;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DetailsEventFragment extends Fragment {
    private User user;
    private int cursusId;
    private long eventId;
    private int rating = 0;
    private Context context;
    private Loading loading;
    private int numberOfRatings = 0;
    private HashMap<?, ?> ratingValuesUsers;
    private MealViewModel mealViewModel;
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
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDetailsEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int color;
        StarUtils.setColorCoalitionStar(binding.starRating, user);
        StarUtils.reduceStarSize(context, binding.starRatingDone, 30, 30);
        NavController navController = Navigation.findNavController(view);
        Event event = DetailsEventFragmentArgs.fromBundle(requireArguments()).getDetailsEvent();
        eventId = event.getId();
        cursusId = event.getCursus_ids().get(0);

        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(event.getKind().toUpperCase());
        }

        String colorCoalition = user.coalition.getColor();
        if (colorCoalition != null) {
            ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(colorCoalition));
            binding.progressBarEvent.setIndeterminateTintList(colorStateList);
        }

        if (event.getKind().equalsIgnoreCase("event"))
            color = Color.parseColor("#FF039BE5"); // light_blue_600
        else if (event.getKind().equalsIgnoreCase("hackathon"))
            color = Color.parseColor("#FF43A047");
        else
            color = Color.parseColor("#FFFFB300"); // orange
        //binding.linearLayoutCompatEventDetails.setBackgroundColor(color);
        Date eventDateBegin = parseDate(event.getBegin_at());
        Date eventDateEnd = parseDate(event.getEnd_at());
        String day = getFormattedDate(eventDateBegin, "d");
        String month = getFormattedDate(eventDateBegin, "MMMM");
        String time = getFormattedDate(eventDateBegin, "hh:mm a");
        String year = getFormattedDate(eventDateBegin, "yyyy");
        String daysUntil = getDaysUntil(eventDateBegin);
        binding.textViewKind.setText(event.getKind());
        binding.textViewName.setText(event.getName());
        binding.textViewDate.setText(month + " " + day + ", " + year + " at " + time);
        binding.textViewDuraction.setText(getEventDuration(eventDateBegin, eventDateEnd));
        binding.textViewDays.setText(daysUntil);
        binding.textViewLocation.setText(event.getLocation());
        binding.textViewPeople.setText(event.getNbr_subscribers() + " / " + event.getMax_people());
        setMarkdownText(binding.textViewDescription, event.getDescription());

        mealViewModel.getRatingValuesLiveData(context, firebaseDatabase, String.valueOf(user.getCampusId()), String.valueOf(cursusId), "events", String.valueOf(eventId))
                .observe(getViewLifecycleOwner(),
                        ratingValues -> {
                            String averageRating = ratingValues.get(1).toString().replace(",", "."); // média da avaliação total sem ser arrendodando ex: 4.5
                            HashMap<?, ?> ratingCounts = (HashMap<?, ?>) ratingValues.get(2); // Total de avaliação para cada estrela
                            numberOfRatings = (int) ratingValues.get(3); // Total de números de avaliações geral de uma refeição
                            ratingValuesUsers = (HashMap<?, ?>) ratingValues.get(4); // Avaliações de cada usuário
                            //bundle.putSerializable("ratingValuesUsers", ratingValuesUsers);
                            Integer ratingValueUser = (Integer) ratingValuesUsers.get(String.valueOf(user.getUid())); // Avaliação do usuário actual

                            // ratingValues.get(0): média da avaliação total arrendodando ex: 5
                            fillStars(binding.starRatingDone, (int) ratingValues.get(0), Double.valueOf(averageRating), false);
                            if (ratingValueUser != null) {
                                binding.textViewTapToRate.setTextColor(getResources().getColor(R.color.green));
                                fillStars(binding.starRating, ratingValueUser, null, false);
                                binding.textViewTapToRate.setText(user.getLogin());
                                binding.starRating.star1.setClickable(false);
                                binding.starRating.star2.setClickable(false);
                                binding.starRating.star3.setClickable(false);
                                binding.starRating.star4.setClickable(false);
                                binding.starRating.star5.setClickable(false);
                            }

                            List<RatingProgressItem> ratingProgressItems = new ArrayList<>();
                            for (int i = 1; i <= ratingCounts.size(); i++) { // i: estrela
                                int ratingCount = (int) ratingCounts.get(i); // Total de avaliação para estrela
                                int percentage = (ratingCount * 100 / numberOfRatings);
                                ratingProgressItems.add(new RatingProgressItem(ratingCount, percentage));
                            }
                            RatingProgressAdapter adapter = new RatingProgressAdapter(ratingProgressItems);
                            binding.recyclerViewRating.setLayoutManager(new LinearLayoutManager(context));
                            binding.recyclerViewRating.setAdapter(adapter);
                            binding.numberOfRatings.setText(numberOfRatings + " " + getString(R.string.ratings));
                            binding.averageRating.setText(averageRating);
                        });

        // Configura os cliques das estrelas
        binding.starRating.star1.setOnClickListener(v -> fillStars(binding.starRating, 1, null, true));
        binding.starRating.star2.setOnClickListener(v -> fillStars(binding.starRating, 2, null, true));
        binding.starRating.star3.setOnClickListener(v -> fillStars(binding.starRating, 3, null, true));
        binding.starRating.star4.setOnClickListener(v -> fillStars(binding.starRating, 4, null, true));
        binding.starRating.star5.setOnClickListener(v -> fillStars(binding.starRating, 5, null, true));

        binding.fabGenerateQrCode.setOnClickListener(v -> {
            try {
                DetailsEventFragmentDirections.ActionDetailsEventFragmentToQrCodeFragment actionDetailsEventFragmentToQrCodeFragment = DetailsEventFragmentDirections.actionDetailsEventFragmentToQrCodeFragment("event" + event.getId() + "#" + user.getUid(), event.getKind(), event.getName(), user.getCampusId(), event.getCursus_ids().get(0));
                navController.navigate(actionDetailsEventFragmentToQrCodeFragment);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        });

        binding.fabOpenAttendanceList.setOnClickListener(v -> {
            try {
                DetailsEventFragmentDirections.ActionDetailsEventFragmentToAttendanceListFragment actionDetailsEventFragmentToAttendanceListFragment = DetailsEventFragmentDirections.actionDetailsEventFragmentToAttendanceListFragment(event.getId(), event.getCursus_ids().get(0), event.getKind(), event.getName(), String.valueOf(binding.textViewDate.getText()));
                navController.navigate(actionDetailsEventFragmentToAttendanceListFragment);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        });
    }

    // Método para lidar com o clique nas estrelas
    private void fillStars(StarRatingBinding starRatingBinding, int selectedRating, Double ratingAverage, boolean isOnClick) {
        if (rating != selectedRating && !loading.isLoading) {
            if (ratingAverage == null)
                rating = selectedRating;
            if (isOnClick) {
                StarUtils.resetStars(starRatingBinding); // Reseta todas as estrelas
                loading.isLoading = true;
                binding.progressBarEvent.setVisibility(View.VISIBLE);
            }
            StarUtils.selectedRating(starRatingBinding, selectedRating);
            if (isOnClick) {
                mealViewModel.rate(
                        context,
                        firebaseDatabase,
                        loading,
                        binding.progressBarEvent,
                        String.valueOf(user.getCampusId()),
                        String.valueOf(cursusId),
                        String.valueOf(eventId),
                        String.valueOf(user.getUid()),
                        selectedRating,
                        "events");
            } else if (ratingAverage != null) {
                StarUtils.starHalf(starRatingBinding, ratingAverage, selectedRating/*ratingAverageRounded*/);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}