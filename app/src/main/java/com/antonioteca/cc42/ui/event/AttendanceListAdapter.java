package com.antonioteca.cc42.ui.event;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.ItemRecyclerviewAttendanceListBinding;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.model.UserDiffCallback;
import com.antonioteca.cc42.utility.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AttendanceListAdapter extends RecyclerView.Adapter<AttendanceListAdapter.AttendanceListViewHolder> {

    private Context context;
    private final List<User> userList;
    private final List<User> userListFilter;

    public AttendanceListAdapter() {
        this.userList = new ArrayList<>();
        this.userListFilter = new ArrayList<>();
    }

    public void updateUserList(List<User> newUserList, Context context) {
        this.context = context;

        List<User> oldList = new ArrayList<>(this.userList);

        this.userList.addAll(newUserList);
        this.userListFilter.addAll(newUserList);

        // Calcule a diferença
        UserDiffCallback diffCallback = new UserDiffCallback(oldList, newUserList); // Passe cópias
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        // Despache as atualizações para o RecyclerView.
        // Isso irá chamar os métodos notifyItemInserted, notifyItemRemoved,
        // notifyItemMoved, ou notifyItemChanged (com ou sem payload)
        // apenas para os itens que realmente mudaram.
        diffResult.dispatchUpdatesTo(this);
    }
    // Dentro da classe AttendanceListAdapter

    public void updateAttendanceUser(Map<String, Boolean> usersIdsWithMarkedPresence) {
        // Passo 1: Otimização - Crie um mapa de usuários para busca rápida O(1).
        // Isso evita o loop aninhado e torna o algoritmo muito mais eficiente (O(N) em vez de O(N*M)).
        Map<String, User> userMap = new HashMap<>();
        for (User user : userListFilter) {
            userMap.put(String.valueOf(user.uid), user);
        }

        // Passo 2: Itere sobre o mapa de presenças que você recebeu.
        // Esta é a lista de todos que fizeram check-in.
        for (Map.Entry<String, Boolean> entry : usersIdsWithMarkedPresence.entrySet()) {
            String userId = entry.getKey();
            boolean hasCheckedOut = entry.getValue(); // true se fez check-out, false se não.

            // Encontra o usuário correspondente no mapa de usuários em tempo O(1).
            User user = userMap.get(userId);

            if (user != null) {
                // Atualiza o status conforme a sua regra de negócio.
                user.setIsCheckIn(true);       // Sempre tem check-in se está no mapa.
                user.setIsCheckOut(hasCheckedOut); // Define o status de check-out.

                // Remove o usuário do mapa para que possamos identificar quem não fez check-in.
                userMap.remove(userId);
            }
        }

        // Passo 3: Trate os usuários restantes.
        // Todos os usuários que sobraram em 'userMap' são aqueles que NÃO estão na lista de presença.
        // Portanto, eles devem ser marcados como ausentes (check-in = false).
        for (User user : userMap.values()) {
            user.setIsCheckIn(false);
            user.setIsCheckOut(false); // Ausente não pode ter check-out.
        }

        // Passo 4: Notifique o adapter sobre as mudanças.
        // Esta é a forma mais segura e eficiente de atualizar a UI.
        notifyDataSetChanged();
    }

    public void updateAttendanceUserSingle(Long uid, boolean isCheckIn) {
        int count = getItemCount();
        for (int i = 0; i < count; i++) {
            if (Objects.equals(this.userList.get(i).uid, uid)) {
                if (isCheckIn)
                    this.userList.get(i).setIsCheckIn(true);
                else
                    this.userList.get(i).setIsCheckOut(true);
                notifyItemChanged(i);
                this.userList.add(0, this.userList.get(i));
                notifyItemInserted(0);
                this.userListFilter.add(0, this.userList.get(i));
                this.userList.remove(i);
                notifyItemRemoved(i);
                this.userListFilter.remove(i);
                break;
            }
        }
    }

    public void clean() {
        this.userList.clear();
        notifyItemRangeRemoved(0, getItemCount());
    }

    private final List<User> filteredList = new ArrayList<>();

    public void filterSearch(@NonNull String text) {
        this.userList.clear();
        if (text.isEmpty())
            this.userList.addAll(userListFilter);
        else if (!userListFilter.isEmpty()) {
            text = text.toLowerCase();
            filteredList.clear();
            for (User user : userListFilter) {
                if (user.login.toLowerCase().startsWith(text) || user.displayName.toLowerCase().startsWith(text))
                    filteredList.add(user);
            }
            this.userList.addAll(filteredList);
        }
        notifyDataSetChanged();
    }

    public void filterListStatus(Boolean status) {
        this.userList.clear();
        if (status == null)
            this.userList.addAll(userListFilter);
        else if (status) {
            for (User user : userListFilter) {
                if (user.isCheckIn())
                    userList.add(user);
            }
        } else {
            for (User user : userListFilter) {
                if (!user.isCheckIn())
                    userList.add(user);
            }
        }
        notifyDataSetChanged();
    }

//    public String containsUser(long userId) {
//        for (User user : getUserList()) {
//            if (user.uid == userId) {
//                return Objects.requireNonNullElse(user.getUrlImageUser(), "");
//            }
//        }
//        return null;
//    }
//
//    public String[] containsUserFaceID(long userId) {
//        for (User user : getUserList()) {
//            if (user.uid == userId) {
//                return new String[]{user.displayName, Objects.requireNonNullElse(user.getUrlImageUser(), "")};
//            }
//        }
//        return new String[]{"", null};
//    }

    @NonNull
    @Override
    public AttendanceListAdapter.AttendanceListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecyclerviewAttendanceListBinding binding = ItemRecyclerviewAttendanceListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AttendanceListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceListAdapter.AttendanceListViewHolder holder, int position) {
        String imageUrl;
        int redColor = ContextCompat.getColor(context, R.color.red);
        int greenColor = ContextCompat.getColor(context, R.color.green);
        User user = userList.get(position);
        imageUrl = user.getUrlImageUser();
        holder.binding.textViewLogin.setText(user.login);
        holder.binding.textViewName.setText(user.displayName);
        Boolean isCheckIn = user.isCheckIn();
        if (isCheckIn != null && isCheckIn) {
            holder.binding.textViewSubscriptionCheckIn.setTextColor(greenColor);
            holder.binding.textViewSubscriptionCheckIn.setText(context.getString(R.string.text_present));
        } else if (isCheckIn != null) {
            holder.binding.textViewSubscriptionCheckIn.setTextColor(redColor);
            holder.binding.textViewSubscriptionCheckIn.setText(context.getString(R.string.text_absent));
        }
        Boolean isCheckOut = user.isCheckOut();
        if (isCheckOut != null && isCheckOut) {
            holder.binding.textViewSubscriptionCheckOut.setTextColor(greenColor);
            holder.binding.textViewSubscriptionCheckOut.setText(context.getString(R.string.text_present));
        } else if (isCheckOut != null) {
            holder.binding.textViewSubscriptionCheckOut.setTextColor(redColor);
            holder.binding.textViewSubscriptionCheckOut.setText(context.getString(R.string.text_absent));
        }
        holder.binding.cardViewRegisteredUser.setOnClickListener(v -> {
            if (user.isCheckIn() != null)
                Util.showModalUserDetails(context, user.login, user.displayName, imageUrl, holder.binding.textViewSubscriptionCheckIn.getText().toString(), user.isCheckIn());
        });
        Util.setImageUserRegistered(context, imageUrl, holder.binding.imageViewUserRegistered);
    }

    @Override
    public int getItemCount() {
        return this.userList.size();
    }

    public List<User> getUserList() {
        return this.userList;
    }

    public int[] getNumberUser() {
        int size0 = 0;
        int size1 = 0;
        int size2 = 0;
        int size3 = 0;

        for (User user : getUserList()) {
            if (user.isCheckIn() != null && user.isCheckIn()) {
                size0 += 1;
                Boolean isCheckOut = user.isCheckOut();
                if (isCheckOut != null && isCheckOut) {
                    size2 += 1;
                } else {
                    size3 += 1;
                }
            } else {
                size1 += 1;
            }
        }
        return new int[]{size0, size1, size2, size3};
    }

    public static class AttendanceListViewHolder extends RecyclerView.ViewHolder {
        ItemRecyclerviewAttendanceListBinding binding;

        public AttendanceListViewHolder(@NonNull ItemRecyclerviewAttendanceListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}