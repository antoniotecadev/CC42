package com.antonioteca.cc42.ui.home;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.databinding.ItemRecyclerviewEventBinding;
import com.antonioteca.cc42.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final String colorCoalition;
    private final List<Event> eventList;
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    {
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public EventAdapter(List<Event> eventList, String colorCoalition) {
        this.eventList = eventList;
        this.colorCoalition = colorCoalition;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_event, parent, false);
        ItemRecyclerviewEventBinding binding = ItemRecyclerviewEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new EventViewHolder(binding);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        try {
            Date dateBegin = inputFormat.parse(event.getBegin_at());
            Date dateEnd = inputFormat.parse(event.getEnd_at());
            holder.binding.textViewTimeBegin.setText(dateBegin != null ? outputFormat.format(dateBegin) : event.getBegin_at());
            holder.binding.textViewTimeEnd.setText(dateEnd != null ? outputFormat.format(dateEnd) : event.getEnd_at());
        } catch (Exception ignored) {
        }
        holder.binding.textViewName.setText(event.getName());
        holder.binding.textViewLocation.setText(event.getLocation());
        holder.itemView.setOnClickListener(v -> {
//                Carrega a animação de fade
//                Animation fadeAnimation = AnimationUtils.loadAnimation(v.getContext(), R.anim.fade);
//                Aplica a animação ao item clicado
//                v.startAnimation(fadeAnimation);
            HomeFragmentDirections.ActionNavHomeToDetailsEventFragment actionNavHomeToDetailsEventFragment = HomeFragmentDirections.actionNavHomeToDetailsEventFragment(event);
            try {
                Navigation.findNavController(v).navigate(actionNavHomeToDetailsEventFragment);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {

        private final ItemRecyclerviewEventBinding binding;

        public EventViewHolder(ItemRecyclerviewEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            int[] colors = {
                    Color.parseColor(colorCoalition),
                    Color.parseColor("#333333"),
            };

            GradientDrawable gradientDrawable = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    colors
            );
            binding.linearLayoutEvent.setBackground(gradientDrawable);
        }
    }
}
