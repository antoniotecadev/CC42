package com.antonioteca.cc42.ui.event;

import static com.antonioteca.cc42.utility.DateUtils.getDaysUntil;
import static com.antonioteca.cc42.utility.DateUtils.getEventDuration;
import static com.antonioteca.cc42.utility.DateUtils.getFormattedDate;
import static com.antonioteca.cc42.utility.DateUtils.parseDate;
import static com.antonioteca.cc42.utility.Util.setMarkdownText;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

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
        if (getActivity() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(event.getKind().toUpperCase());
        if (event.getKind().equalsIgnoreCase("event"))
            color = Color.parseColor("#FF039BE5"); // light_blue_600
        else if (event.getKind().equalsIgnoreCase("hackathon"))
            color = Color.parseColor("#FF43A047");
        else
            color = Color.parseColor("#FF01579B"); // light_blue_900
        binding.linearLayoutCompatEventDetails.setBackgroundColor(color);
        Date eventDateBegin = parseDate(event.getBegin_at());
        Date eventDateEnd = parseDate(event.getEnd_at());
        String day = getFormattedDate(eventDateBegin, "d");
        String month = getFormattedDate(eventDateBegin, "MMMM");
        String time = getFormattedDate(eventDateBegin, "hh:mm a");
        String year = getFormattedDate(eventDateBegin, "yyyy");
        assert eventDateBegin != null;
        String daysUntil = getDaysUntil(eventDateBegin);
        binding.textViewKind.setText(event.getKind());
        binding.textViewName.setText(event.getName());
        binding.textViewDate.setText(month + " " + day + ", " + year + " at " + time);
        assert eventDateEnd != null;
        binding.textViewDuraction.setText(getEventDuration(eventDateBegin, eventDateEnd));
        binding.textViewDays.setText(daysUntil);
        binding.textViewLocation.setText(event.getLocation());
        binding.textViewPeople.setText(event.getNbr_subscribers() + " / " + event.getMax_people());
        setMarkdownText(binding.textViewDescription, event.getDescription());
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}