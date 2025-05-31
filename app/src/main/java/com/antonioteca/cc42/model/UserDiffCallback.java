package com.antonioteca.cc42.model;

import androidx.recyclerview.widget.DiffUtil;

import java.util.ArrayList;
import java.util.List;

public class UserDiffCallback extends DiffUtil.Callback {

    private final List<User> oldList;
    private final List<User> newList;

    public UserDiffCallback(List<User> oldList, List<User> newList) {
        this.oldList = oldList != null ? oldList : new ArrayList<>(); // Evitar NPE
        this.newList = newList != null ? newList : new ArrayList<>(); // Evitar NPE
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // Verifica se os itens representam a MESMA entidade (ex: mesmo ID)
        if (oldList.isEmpty() || newList.isEmpty() ||
                oldItemPosition >= oldList.size() || newItemPosition >= newList.size()) {
            return false;
        }
        return oldList.get(oldItemPosition).uid.equals(newList.get(newItemPosition).uid);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // Verifica se os DADOS VISUAIS do item são os mesmos
        // Isso é chamado apenas se areItemsTheSame retornar true para este par.
        if (oldList.isEmpty() || newList.isEmpty() ||
                oldItemPosition >= oldList.size() || newItemPosition >= newList.size()) {
            return false;
        }
        // Aqui, a implementação de User.equals() é usada.
        // Se User.equals() compara todos os campos relevantes para a UI, então está correto.
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }

    // Opcional: Para atualizações parciais (payloads) se apenas uma parte do conteúdo mudou.
    // @Nullable
    // @Override
    // public Object getChangePayload(int oldItemPosition, int newItemPosition) {
    //     // Se você quiser fazer atualizações mais granulares no onBindViewHolder.
    //     // Por exemplo, se apenas o nome mudou, você pode retornar um Bundle
    //     // indicando essa mudança específica.
    //     return super.getChangePayload(oldItemPosition, newItemPosition);
    // }
}
