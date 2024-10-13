package com.antonioteca.cc42.utility;

import static com.antonioteca.cc42.utility.DateUtils.getDaysUntil;
import static com.antonioteca.cc42.utility.DateUtils.getFormattedDate;
import static com.antonioteca.cc42.utility.DateUtils.parseDate;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.ItemRecycleviewEventBinding;
import com.antonioteca.cc42.model.Event;

import java.util.Date;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private String daysUntil = null;
    private final List<Event> eventList;

    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventAdapter.EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycleview_event, parent, false);
        com.antonioteca.cc42.databinding.ItemRecycleviewEventBinding binding = ItemRecycleviewEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new EventViewHolder(binding);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull EventAdapter.EventViewHolder holder, int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Event event = eventList.get(position);
        String dateString = event.getBegin_at();
        Date eventDate = parseDate(dateString);
        if (eventDate != null) {
            String day = getFormattedDate(eventDate, "d");
            String month = getFormattedDate(eventDate, "MMMM");
            String time = getFormattedDate(eventDate, "hh:mm a");
            daysUntil = getDaysUntil(eventDate);
            holder.binding.textViewDateDay.setText(day);
            holder.binding.textViewDateMonth.setText(month);
            holder.binding.textViewTimeBegin.setText(time);
            holder.binding.textViewDays.setText(daysUntil);
        }

        int color;
        if (event.getKind().equalsIgnoreCase("Event"))
            color = Color.parseColor("#FF039BE5"); // light_blue_600
        else if (event.getKind().equalsIgnoreCase("Hackathon"))
            color = Color.parseColor("#FF43A047");
        else
            color = Color.parseColor("#FF01579B"); // light_blue_900
        int colorDays = daysUntil.equals("finish") ? Color.parseColor("#FFE53935") : color;
        holder.binding.textViewKind.setText(event.getKind());
        holder.binding.textViewName.setText(event.getName());
        holder.binding.textViewLocation.setText(event.getLocation());
        holder.binding.textViewKind.setTextColor(color);
        holder.binding.textViewTimeBegin.setTextColor(color);
        holder.binding.textViewDays.setTextColor(colorDays);
        holder.binding.textViewLocation.setTextColor(color);
        holder.binding.dividerLeft.setBackgroundColor(color);
        holder.binding.dividerTop.setBackgroundColor(color);
        holder.binding.dividerRight.setBackgroundColor(color);
        holder.binding.dividerBottom.setBackgroundColor(color);
        holder.binding.dividerLeftMiddle.setBackgroundColor(color);
        holder.binding.imageViewTimeBegin.setColorFilter(color);
        holder.binding.imageViewLocation.setColorFilter(color);
        holder.binding.imageViewDay.setColorFilter(colorDays);
        // Definindo o efeito de clique
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Carrega a animação de fade
                Animation fadeAnimation = AnimationUtils.loadAnimation(v.getContext(), R.anim.fade);
                // Aplica a animação ao item clicado
                v.startAnimation(fadeAnimation);

                // Aqui você pode fazer algo quando o item é clicado, como navegar para outra Activity
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {

        private final ItemRecycleviewEventBinding binding;

        public EventViewHolder(ItemRecycleviewEventBinding binding) {
            super(binding.getRoot());
            // Define click listener for the ViewHolder's View
            this.binding = binding;
        }
    }

}