package com.antonioteca.cc42.ui.qrcode;

import static com.antonioteca.cc42.utility.Util.generateQrCodeWithLogo;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentQrCodeBinding;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
import com.antonioteca.cc42.utility.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class QrCodeFragment extends Fragment {


    private Context context;
    private boolean firstReader;
    private DatabaseReference ref;
    private FirebaseDatabase firebaseDatabase;
    private FragmentQrCodeBinding binding;
    private ValueEventListener valueEventListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firstReader = true;
        context = requireContext();
        firebaseDatabase = FirebaseDataBaseInstance.getInstance().database;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentQrCodeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        QrCodeFragmentArgs args = QrCodeFragmentArgs.fromBundle(getArguments());
        String content = args.getContent();
        String title = args.getTitle(); /* login user | kind event | meal name*/
        String description = args.getDescription(); /* displayName user | name event | meal description */
        String campusId = String.valueOf(args.getCampusId());
        String cursusId = String.valueOf(args.getCursusId());
        String[] parts = content.split("#");
        String userStaffId = parts[parts.length - 1];
        Bitmap bitmapQrCode = generateQrCodeWithLogo(view.getContext(), content);
        binding.textViewTitle.setText(title);
        binding.textViewDescription.setText(description);
        binding.imageViewQrCode.setImageBitmap(bitmapQrCode);

        ref = firebaseDatabase.getReference("campus")
                .child(campusId)
                .child("cursus")
                .child(cursusId)
                .child("infoTmpUserEventMeal")
                .child(userStaffId);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (firstReader) {
                    firstReader = false;
                    return;
                }
                if (snapshot.exists()) {
                    String displayName = snapshot.child("displayName").getValue(String.class);
                    String urlImageUser = snapshot.child("urlImageUser").getValue(String.class);
                    String message = displayName + "\n" + (content.startsWith("event") ? context.getString(R.string.msg_sucess_mark_attendance_event) : context.getString(R.string.msg_sucess_subscription));
                    Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.sucess), message, "#4CAF50", urlImageUser, null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.err), error.getMessage(), "#E53935", null, null);
            }
        };
        ref.addValueEventListener(valueEventListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (ref != null && valueEventListener != null)
            ref.removeEventListener(valueEventListener);
    }
}