package com.antonioteca.cc42.utility;

import static com.antonioteca.cc42.utility.DateUtils.getDaysUntil;
import static com.antonioteca.cc42.utility.DateUtils.getFormattedDate;
import static com.antonioteca.cc42.utility.DateUtils.parseDate;

import android.graphics.Color;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.ItemRecycleviewEventBinding;
import com.antonioteca.cc42.model.Event;
import com.antonioteca.cc42.ui.home.HomeFragmentDirections;

import java.util.Date;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> eventList;

    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventAdapter.EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycleview_event, parent, false);
        ItemRecycleviewEventBinding binding = ItemRecycleviewEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new EventViewHolder(binding);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull EventAdapter.EventViewHolder holder, int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Event event = eventList.get(position);
        Date eventDateBegin = parseDate(event.getBegin_at());
        if (eventDateBegin != null) {
            String day = getFormattedDate(eventDateBegin, "d");
            String month = getFormattedDate(eventDateBegin, "MMMM");
            String time = getFormattedDate(eventDateBegin, "hh:mm a");
            String daysUntil = getDaysUntil(eventDateBegin);
            holder.binding.textViewDateDay.setText(day);
            holder.binding.textViewDateMonth.setText(month);
            holder.binding.textViewTimeBegin.setText(time);
            holder.binding.textViewDays.setText(daysUntil);
        }
        int color;
        if (event.getKind().equalsIgnoreCase("event"))
            color = Color.parseColor("#FF039BE5"); // light_blue_600
        else if (event.getKind().equalsIgnoreCase("hackathon"))
            color = Color.parseColor("#FF43A047");
        else
            color = Color.parseColor("#FFFFB300"); // orange
        holder.binding.textViewKind.setText(event.getKind());
        holder.binding.textViewName.setText(event.getName());
        holder.binding.textViewLocation.setText(event.getLocation());
        holder.binding.textViewKind.setTextColor(color);
        holder.binding.textViewTimeBegin.setTextColor(color);
        holder.binding.textViewLocation.setTextColor(color);
        holder.binding.dividerLeft.setBackgroundColor(color);
        holder.binding.dividerTop.setBackgroundColor(color);
        holder.binding.dividerRight.setBackgroundColor(color);
        holder.binding.dividerBottom.setBackgroundColor(color);
        holder.binding.dividerLeftMiddle.setBackgroundColor(color);
        holder.binding.imageViewTimeBegin.setColorFilter(color);
        holder.binding.imageViewLocation.setColorFilter(color);
        holder.binding.imageViewDay.setColorFilter(color);
        // Definindo o efeito de clique
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Carrega a animação de fade
                Animation fadeAnimation = AnimationUtils.loadAnimation(v.getContext(), R.anim.fade);
                // Aplica a animação ao item clicado
                v.startAnimation(fadeAnimation);
                HomeFragmentDirections.ActionNavHomeToDetailsEventFragment actionNavHomeToDetailsEventFragment = HomeFragmentDirections.actionNavHomeToDetailsEventFragment(event);
                Navigation.findNavController(v).navigate((NavDirections) actionNavHomeToDetailsEventFragment);
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
    public static class EventViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        private final ItemRecycleviewEventBinding binding;

        public EventViewHolder(ItemRecycleviewEventBinding binding) {
            super(binding.getRoot());
            // Define click listener for the ViewHolder's View
            this.binding = binding;
            // Registra o item para o menu de contexto
            binding.getRoot().setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            // Inflar o menu de contexto
            MenuInflater inflater = new MenuInflater(view.getContext());
            inflater.inflate(R.menu.context_menu_event_list, contextMenu);
        }
    }
}