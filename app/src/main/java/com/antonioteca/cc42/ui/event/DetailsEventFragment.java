package com.antonioteca.cc42.ui.event;

import static com.antonioteca.cc42.utility.DateUtils.getDaysUntil;
import static com.antonioteca.cc42.utility.DateUtils.getEventDuration;
import static com.antonioteca.cc42.utility.DateUtils.getFormattedDate;
import static com.antonioteca.cc42.utility.DateUtils.parseDate;
import static com.antonioteca.cc42.utility.Util.generateQrCode;
import static com.antonioteca.cc42.utility.Util.setMarkdownText;
import static com.antonioteca.cc42.utility.Util.showModalQrCode;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentDetailsEventBinding;
import com.antonioteca.cc42.model.Event;

import java.util.Date;

public class DetailsEventFragment extends Fragment {

    private FragmentDetailsEventBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int color;
        binding = FragmentDetailsEventBinding.inflate(inflater, container, false); // Inflate the layout for this fragment
        Event event = DetailsEventFragmentArgs.fromBundle(requireArguments()).getDetailsEvent();
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(event.getKind().toUpperCase());
        }
        if (event.getKind().equalsIgnoreCase("event"))
            color = Color.parseColor("#FF039BE5"); // light_blue_600
        else if (event.getKind().equalsIgnoreCase("hackathon"))
            color = Color.parseColor("#FF43A047");
        else
            color = Color.parseColor("#FFFFB300"); // orange
        binding.linearLayoutCompatEventDetails.setBackgroundColor(color);
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
        binding.fabGenerateQrCode.setOnClickListener(view -> {
            Bitmap bitmapQrCode = generateQrCode(view.getContext(), "event" + event.getId());
            showModalQrCode(view.getContext(), bitmapQrCode, event.getKind(), event.getName());
        });
        binding.fabOpenAttendanceList.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.subscriptionListFragment) {
                DetailsEventFragmentDirections.ActionDetailsEventFragmentToAttendanceListFragment actionDetailsEventFragmentToAttendanceListFragment = DetailsEventFragmentDirections.actionDetailsEventFragmentToAttendanceListFragment(event.getId(), event.getCursus_ids().get(0), event.getKind(), event.getName(), String.valueOf(binding.textViewDate.getText()));
                Navigation.findNavController(v).navigate(actionDetailsEventFragmentToAttendanceListFragment);
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}