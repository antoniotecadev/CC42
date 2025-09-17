package com.antonioteca.cc42.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentSendMessageBinding;
import com.antonioteca.cc42.model.Message;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
import com.antonioteca.cc42.network.NotificationFirebase.Notification;
import com.antonioteca.cc42.utility.Util;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SendMessageFragment extends Fragment {

    private FragmentSendMessageBinding binding;
    private FirebaseDatabase database;
    private User user;

    public SendMessageFragment() {
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
        binding = FragmentSendMessageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int campusId = user.getCampusId();
        long userId = user.getUid();
        String campusName = user.getCampusName();
        binding.buttonSendMessage.setOnClickListener(v -> sendMessage(campusId, userId, campusName));
    }

    private void sendMessage(int campusId, long userId, String campusName) {

        String cursu = binding.spinnerCursus.getSelectedItem().toString();
        String cursusId;
        if (cursu.equals(getString(R.string.Cursus42)))
            cursusId = "21";
        else if (cursu.equals(getString(R.string.CPiscine)))
            cursusId = "9";
        else if (cursu.equals(getString(R.string.CPiscineReloaded)))
            cursusId = "66";
        else {
            cursusId = "";
        }

        String title = "";
        if (binding.editTextTitle.getText() != null) {
            title = binding.editTextTitle.getText().toString().trim();
        }

        String messageText = "";
        if (binding.editTextMessage.getText() != null) {
            messageText = binding.editTextMessage.getText().toString().trim();
        }

        if (title.isEmpty()) {
            binding.textFieldTitleLayout.setError(getString(R.string.title_is_not_empty));
            return;
        } else {
            binding.textFieldTitleLayout.setError(null); // Limpar erro
        }

        if (messageText.isEmpty()) {
            binding.textFieldMessageLayout.setError(getString(R.string.message_is_not_empty));
            return;
        } else {
            binding.textFieldMessageLayout.setError(null); // Limpar erro
        }

        DatabaseReference messagesRef = database.getReference("campus")
                .child(String.valueOf(campusId))
                .child("cursus")
                .child(cursusId)
                .child("messages");

        Context context = getContext();

        // Criar um ID único para a mensagem
        String messageId = messagesRef.push().getKey();

        Message message = new Message(title, messageText, System.currentTimeMillis(), userId);

        if (messageId != null) {
            binding.buttonSendMessage.setEnabled(false);
            binding.buttonSendMessage.setText(R.string.sending);
            messagesRef.child(messageId).setValue(message) // Você pode passar o objeto Message diretamente
                    .addOnSuccessListener(aVoid -> { // Java 8 lambda
                        // Limpar campos após o envio
                        if (binding.editTextTitle.getText() != null)
                            binding.editTextTitle.getText().clear();
                        if (binding.editTextMessage.getText() != null)
                            binding.editTextMessage.getText().clear();
                        binding.editTextTitle.requestFocus(); // Opcional: focar no primeiro campo novamente
                        binding.buttonSendMessage.setEnabled(true);
                        binding.buttonSendMessage.setText(R.string.send_message);
                        Util.showAlertDialogBuild(getString(R.string.sucess), getString(R.string.message_send_success), context, null);
                        String topicStudent = "meals_" + campusId + "_" + cursusId;
                        String topicStaff = "meals_" + campusId + "_" + campusName;
                        List<String> topics = Arrays.asList(topicStudent, topicStaff);
                        String condition = topics.stream().map(topic -> "'" + topic + "' in topics").collect(Collectors.joining(" || "));
                        try {
                            Notification.sendFCMNotification(context, getLayoutInflater(), null, message, String.valueOf(campusId), cursusId, null, condition);
                        } catch (IOException e) {
                            Toast.makeText(context, R.string.error_send_notification, Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> { // Java 8 lambda
                        binding.buttonSendMessage.setEnabled(true);
                        binding.buttonSendMessage.setText(R.string.send_message);
                        Util.showAlertDialogBuild(getString(R.string.err), getString(R.string.error_send_message) + e.getMessage(), context, null);
                    });
        } else {
            Util.showAlertDialogBuild(getString(R.string.err), getString(R.string.error_generate_id_message), getContext(), null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}