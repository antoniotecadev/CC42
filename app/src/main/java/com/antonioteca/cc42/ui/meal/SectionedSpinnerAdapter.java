package com.antonioteca.cc42.ui.meal;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.antonioteca.cc42.R;

import java.util.List;
import java.util.Map;

public class SectionedSpinnerAdapter extends ArrayAdapter<String> {

    private final List<String> sections;
    private final Map<String, List<String>> sectionItems;

    public SectionedSpinnerAdapter(Context context, List<String> sections, Map<String, List<String>> sectionItems) {
        super(context, android.R.layout.simple_spinner_item); // Use o layout personalizado aqui
        setDropDownViewResource(R.layout.custom_spinner_dropdown_item_meals); // Layout para o dropdown
        this.sections = sections;
        this.sectionItems = sectionItems;
    }

    @Override
    public int getCount() { // Calcula o número total de itens no Spinner, incluindo os títulos das seções e os itens de cada seção.
        int totalItems = 0;
        for (List<String> items : sectionItems.values()) {
            totalItems += items.size();
        }
        return totalItems + sections.size();
    }

    @Override
    public String getItem(int position) { // Retorna o item correspondente à posição no Spinner.
        int currentPosition = 0;
        for (String section : sections) {
            if (position == currentPosition) { // Se a posição for um título de seção, retorna o título;
                return section;
            }
            currentPosition++;
            List<String> items = sectionItems.get(section);
            if (position < currentPosition + items.size()) { // caso contrário, retorna o item da seção.
                return items.get(position - currentPosition);
            }
            currentPosition += items.size();
        }
        return "";
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) { // Personaliza a exibição dos itens no Spinner.
        View view = super.getView(position, convertView, parent);
        TextView textView = view.findViewById(android.R.id.text1);
        String item = getItem(position);
        if (sections.contains(item)) { // Se for um título de seção, o texto fica em negrito;
            textView.setText(item);
            textView.setTypeface(null, Typeface.BOLD);
        } else { // se for um item normal, o texto fica normal.
            textView.setText(item);
            textView.setTypeface(null, Typeface.NORMAL);
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView textView = view.findViewById(android.R.id.text1);
        String item = getItem(position);
        if (sections.contains(item)) {
            textView.setText(item);
            textView.setTypeface(null, Typeface.BOLD); // Texto em negrito para seções no dropdown
        } else {
            textView.setText(item);
            textView.setTypeface(null, Typeface.NORMAL); // Texto normal para itens no dropdown
        }
        return view;
    }
}
