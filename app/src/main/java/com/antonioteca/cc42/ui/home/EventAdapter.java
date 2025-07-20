package com.antonioteca.cc42.ui.home;

import static com.antonioteca.cc42.utility.Util.dpToPx;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.model.Event;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String colorCoalition;
    private final List<Event> eventList = new ArrayList<>();
    private final List<Event> eventListEnd = new ArrayList<>();
    private boolean showEventListEnd = false;
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    {
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static final int TYPE_EVENT;
    private static final int TYPE_FOOTER;

    static {
        TYPE_EVENT = 0;
        TYPE_FOOTER = 1;
    }

    public EventAdapter(@NonNull List<Event> eventList, String colorCoalition) {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        for (Event event : eventList) {
            OffsetDateTime end = OffsetDateTime.parse(event.getEnd_at());
            if (end.isAfter(now))
                this.eventList.add(event);
            else
                this.eventListEnd.add(event);
        }
        this.colorCoalition = colorCoalition;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        if (viewType == TYPE_FOOTER) {
//            ItemRecyclerviewEventFooterBinding binding = ItemRecyclerviewEventFooterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_event_footer, parent, false);
            return new EventViewHolderFooter(view);
        } else {
//            ItemRecyclerviewEventBinding binding = ItemRecyclerviewEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_event, parent, false);
            return new EventViewHolder(view);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof EventViewHolder eventViewHolder) {
            Event event = eventList.get(position);
            TextView textViewName = eventViewHolder.view.findViewById(R.id.textViewName);
            TextView textViewLocation = eventViewHolder.view.findViewById(R.id.textViewLocation);
            TextView textViewTimeBegin = eventViewHolder.view.findViewById(R.id.textViewTimeBegin);
            TextView textViewTimeEnd = eventViewHolder.view.findViewById(R.id.textViewTimeEnd);

            try {
                Date dateBegin = inputFormat.parse(event.getBegin_at());
                Date dateEnd = inputFormat.parse(event.getEnd_at());
                textViewTimeBegin.setText(dateBegin != null ? outputFormat.format(dateBegin) : event.getBegin_at());
                textViewTimeEnd.setText(dateEnd != null ? outputFormat.format(dateEnd) : event.getEnd_at());
            } catch (Exception ignored) {
            }
            textViewName.setText(event.getName());
            textViewLocation.setText(event.getLocation());
            eventViewHolder.itemView.setOnClickListener(v -> {
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
        } else if (holder instanceof EventViewHolderFooter footerViewHolder) {
            TextView textView = footerViewHolder.view.findViewById(R.id.textViewFooter);
            textView.setTextColor(Color.parseColor(colorCoalition));
            textView.setText(showEventListEnd ? "Ocultar eventos realizados" : "Ver eventos realizados");
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) textView.getLayoutParams();
            params.bottomMargin = dpToPx(70, textView.getContext()); // Defina a margem inferior em pixels
            textView.setLayoutParams(params);
            int startPosition = eventList.size();
            textView.setOnClickListener(v -> {
                if (showEventListEnd) {
                    int count = eventListEnd.size();
                    eventList.removeAll(eventListEnd);
                    showEventListEnd = false;
                    notifyItemRangeRemoved(startPosition - count, count);
                    notifyItemChanged(eventList.size());
                } else {
                    eventList.addAll(eventListEnd);
                    showEventListEnd = true;
                    notifyItemRangeInserted(startPosition, eventListEnd.size());
                    notifyItemChanged(eventList.size());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size() + 1; // +1 para o footer
    }

    @Override
    public int getItemViewType(int position) {
        if (position < eventList.size())
            return TYPE_EVENT;
        else
            return TYPE_FOOTER;
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {

        private final View view;

        public EventViewHolder(@NonNull View view) {
            super(view);
            this.view = view;
            int[] colors = {
                    Color.parseColor("#444444"),
                    Color.parseColor(colorCoalition),
            };

            GradientDrawable gradientDrawable = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    colors
            );
            view.findViewById(R.id.linearLayoutEvent).setBackground(gradientDrawable);
        }
    }

    public static class EventViewHolderFooter extends RecyclerView.ViewHolder {

        private final View view;

        public EventViewHolderFooter(@NonNull View view) {
            super(view);
            this.view = view;
        }
    }
}
