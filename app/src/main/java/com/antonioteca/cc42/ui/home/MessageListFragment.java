package com.antonioteca.cc42.ui.home; // Adapte o package conforme sua estrutura

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.model.Message;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
import com.antonioteca.cc42.utility.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageListFragment extends Fragment {

    private User user;
    private ProgressBar progressBar;
    private FirebaseDatabase database;
    private TextView textViewNoMessages;
    private DatabaseReference messagesRef;
    private MessageAdapter messageAdapter;
    private RecyclerView recyclerViewMessages;
    private ValueEventListener messagesListener;

    public MessageListFragment() {
        // Construtor público vazio obrigatório
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = requireContext();
        user = new User(context);
        database = FirebaseDataBaseInstance.getInstance().database;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);

        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);
        progressBar = view.findViewById(R.id.progressBar);
        textViewNoMessages = view.findViewById(R.id.textViewNoMessages);
        setupRecyclerView();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (user.isStaff())
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.title_cursus))
                    .setItems(R.array.cursus_list, (dialog, selected) -> {
                        if (selected == 0) {
                            loadMessages(21);
                        } else if (selected == 1) {
                            loadMessages(9);
                        } else if (selected == 2) {
                            loadMessages(66);
                        }
                    }).setPositiveButton(R.string.cancel, (dialog, which) -> requireActivity().onBackPressed())
                    .setCancelable(false)
                    .show();
        else
            loadMessages(user.getCursusId());
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter();
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void loadMessages(int cursusId) {

        messagesRef = database.getReference("campus")
                .child(String.valueOf(user.getCampusId()))
                .child("cursus")
                .child(String.valueOf(cursusId))
                .child("messages");

        progressBar.setVisibility(View.VISIBLE);
        textViewNoMessages.setVisibility(View.GONE);
        recyclerViewMessages.setVisibility(View.GONE);

        if (messagesListener != null) {
            messagesRef.removeEventListener(messagesListener); // Evitar listeners duplicados
        }

        messagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                List<Message> messages = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) {
                        messages.add(message);
                    }
                }

                // Opcional: Ordenar as mensagens, por exemplo, pela mais recente primeiro
                // Supondo que timestamps maiores são mais recentes
                Collections.sort(messages, (m1, m2) -> {
                    if (m1.getTimestamp() == null && m2.getTimestamp() == null) return 0;
                    if (m1.getTimestamp() == null) return 1; // Coloca nulos no final
                    if (m2.getTimestamp() == null) return -1; // Coloca nulos no final
                    return m2.getTimestamp().compareTo(m1.getTimestamp()); // Decrescente
                });

                if (messages.isEmpty()) {
                    textViewNoMessages.setVisibility(View.VISIBLE);
                    recyclerViewMessages.setVisibility(View.GONE);
                } else {
                    textViewNoMessages.setVisibility(View.GONE);
                    recyclerViewMessages.setVisibility(View.VISIBLE);
                    messageAdapter.setMessages(messages);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                textViewNoMessages.setVisibility(View.GONE); // Pode querer mostrar uma mensagem de erro aqui
                Util.showAlertDialogBuild(getString(R.string.err), getString(R.string.error_load_messages) + databaseError.getMessage(), getContext(), null);
            }
        };
        messagesRef.orderByChild("timestamp").limitToLast(6).addValueEventListener(messagesListener);
        // Se você quiser carregar apenas uma vez e não ouvir por atualizações:
        // messagesRef.addListenerForSingleValueEvent(messagesListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remover o listener para evitar memory leaks e chamadas desnecessárias
        if (messagesListener != null) {
            messagesRef.removeEventListener(messagesListener);
        }
        // Se estiver usando ViewBinding, defina binding = null aqui
    }
}