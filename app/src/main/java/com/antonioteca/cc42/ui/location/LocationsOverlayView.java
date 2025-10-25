package com.antonioteca.cc42.ui.location;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;


import com.antonioteca.cc42.R;
import com.antonioteca.cc42.model.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Overlay simples que recebe uma lista de Location (com porcentagens) e posiciona botões
 * relativos ao tamanho do próprio ViewGroup (match_parent do mapa).
 * <p>
 * Observação: posições são relativas ao tamanho do container (0..1).
 */
public class LocationsOverlayView extends FrameLayout {

    private final List<Location> locations = new ArrayList<>();
    private final List<View> areaViews = new ArrayList<>();
    private String selectedLocationId = null;
    private OnLocationSelectedListener onLocationSelectedListener;

    public LocationsOverlayView(Context context) {
        super(context);
        init(context, null);
    }

    public LocationsOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LocationsOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context ctx, AttributeSet attrs) {
        setClipToPadding(false);
        setClipChildren(false);
    }

    public interface OnLocationSelectedListener {
        void onLocationSelected(Location location);

        void onLocationLongPressed(Location location);
    }

    public void setOnLocationSelectedListener(OnLocationSelectedListener listener) {
        this.onLocationSelectedListener = listener;
    }

    public void setLocations(List<Location> list) {
        locations.clear();
        locations.addAll(list);
        buildAreas();
    }

    private void buildAreas() {
        // remove antigos
        for (View v : areaViews) removeView(v);
        areaViews.clear();

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (final Location loc : locations) {
            // criando um botão transparente com texto
            AppCompatButton btn = new AppCompatButton(getContext());
            btn.setAllCaps(false);
            btn.setText(loc.areaName);
            btn.setTextSize(10f);
            btn.setGravity(Gravity.CENTER);
            btn.setPadding(2, 6, 2, 2);
            btn.setBackgroundColor(loc.color);
//            btn.setBackgroundResource(R.drawable.location_button_background); // veja drawable abaixo
            btn.setAlpha(0.85f);

            // clique
            btn.setOnClickListener(v -> {
                // marcar seleção visualmente
                selectedLocationId = loc.areaId;
                updateSelectionOutline();
                if (onLocationSelectedListener != null) {
                    onLocationSelectedListener.onLocationSelected(loc);
                } else {
                    Toast.makeText(getContext(), "Selected: " + loc.areaName, Toast.LENGTH_SHORT).show();
                }
            });

            // long press
            btn.setOnLongClickListener(v -> {
                if (onLocationSelectedListener != null) {
                    onLocationSelectedListener.onLocationLongPressed(loc);
                } else {
                    Toast.makeText(getContext(), "Long press: " + loc.areaName, Toast.LENGTH_SHORT).show();
                }
                return true;
            });

            // adiciona à view (layout params definidos no onLayout)
            addView(btn);
            areaViews.add(btn);

            // tag com a location para referencia
            btn.setTag(loc);
        }

        requestLayout();
    }

    private void updateSelectionOutline() {
        for (View v : areaViews) {
            Location loc = (Location) v.getTag();
            if (loc != null && loc.areaId.equals(selectedLocationId)) {
                // borda visível quando selecionado
                v.setBackgroundResource(R.drawable.location_button_background_selected);
            } else if (loc != null) {
                v.setBackgroundColor(loc.color);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Medimos normalmente
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // medir children com o mesmo tamanho do container (será reposicionado no onLayout)
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        for (View v : areaViews) {
            // vamos medir cada child com tamanho arbitrário; o onLayout define posição / tamanho
            int childW = MeasureSpec.makeMeasureSpec((int) (w * 0.15f), MeasureSpec.EXACTLY);
            int childH = MeasureSpec.makeMeasureSpec((int) (h * 0.08f), MeasureSpec.EXACTLY);
            v.measure(childW, childH);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int width = r - l;
        int height = b - t;

        for (int i = 0; i < areaViews.size(); i++) {
            View v = areaViews.get(i);
            Location loc = (Location) v.getTag();
            if (loc == null) continue;

            // Calcula a posição em px usando percentuais
            int left = (int) (loc.left * width);
            int top = (int) (loc.top * height);
            int w = (int) (loc.width * width);
            int h = (int) (loc.height * height);

            if (w < 40) w = 40;
            if (h < 24) h = 24;

            v.layout(left, top, left + w, top + h);
        }
        // aplica seleção visual
        updateSelectionOutline();
    }

    /**
     * Recupera a Location selecionada (ou null).
     */
    public Location getSelectedLocation() {
        for (Location loc : locations) {
            if (loc.areaId.equals(selectedLocationId)) return loc;
        }
        return null;
    }

    public void clearSelection() {
        selectedLocationId = null;
        updateSelectionOutline();
    }
}


