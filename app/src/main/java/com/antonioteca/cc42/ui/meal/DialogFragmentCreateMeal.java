package com.antonioteca.cc42.ui.meal;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.dao.daofarebase.DaoMealFirebase;
import com.antonioteca.cc42.databinding.FragmentDialogCreateMealBinding;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
import com.antonioteca.cc42.utility.Util;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DialogFragmentCreateMeal extends DialogFragment {

    private User user;
    private Uri imageUri;
    private Context context;
    private AlertDialog dialog;
    private FragmentDialogCreateMealBinding binding;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> imagePickerLauncher;

    private FirebaseDatabase firebaseDatabase;

    private final ActivityResultLauncher<String> activityResultLauncherCamera = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result)
                    openCamera(context); // Abrir a câmera
                else
                    Util.showAlertDialogBuild(getString(R.string.err), getString(R.string.msg_permis_open_camera_denied), getContext(), null);
            });

    private final ActivityResultLauncher<String> activityResultLauncherPicker = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result)
                    openImagePicker(); // Abrir a galeria
                else
                    Util.showAlertDialogBuild(getString(R.string.err), getString(R.string.msg_permis_image_denied), getContext(), null);
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        user = new User(context);
        user.coalition = new Coalition(context);
        firebaseDatabase = FirebaseDataBaseInstance.getInstance().database;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        binding = FragmentDialogCreateMealBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setIcon(R.drawable.logo_42);
        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        addNumberSpinner(binding.spinnerQuantity, context);

        String colorCoalition = user.coalition.getColor();
        if (colorCoalition != null) {
            int color = Color.parseColor(colorCoalition);
            ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(colorCoalition));
            binding.progressBarMeal.setIndeterminateTintList(colorStateList);
            binding.buttonClose.setBackgroundColor(color);
            binding.buttonCreateMeal.setBackgroundColor(color);
        }

        binding.buttonCreateMeal.setOnClickListener(v -> {
            int selectedValue = 0;
            int selectedPosition = binding.spinnerQuantity.getSelectedItemPosition();
            if (selectedPosition != AdapterView.INVALID_POSITION) {
                selectedValue = (int) binding.spinnerQuantity.getItemAtPosition(selectedPosition);
            }
            if (isEmptyField(binding.textInputEditTextName.getText().toString())) {
                binding.textInputEditTextName.requestFocus();
                binding.textInputEditTextName.setError(getString(R.string.invalid_name_meal));
            } else {
                binding.progressBarMeal.setVisibility(View.VISIBLE);
                if (imageUri != null) {
                    DaoMealFirebase.uploadImageToCloudinary(
                            firebaseDatabase,
                            getLayoutInflater(),
                            binding.progressBarMeal,
                            context,
                            String.valueOf(user.getCampusId()),
                            binding.textInputEditTextName.getText().toString(),
                            binding.textInputEditTextDescription.getText().toString(),
                            selectedValue,
                            imageUri
                    );
                } else {
                    DaoMealFirebase.saveMealToFirebase(
                            firebaseDatabase,
                            getLayoutInflater(),
                            binding.progressBarMeal,
                            context,
                            String.valueOf(user.getCampusId()),
                            binding.textInputEditTextName.getText().toString(),
                            binding.textInputEditTextDescription.getText().toString(),
                            selectedValue,
                            ""
                    );
                }
            }
        });
        binding.buttonClose.setOnClickListener(v -> dialog.dismiss());
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), imageUri -> {
            // Imagem selecionada da galeria
            if (imageUri != null) {
                this.imageUri = imageUri;
                Glide.with(this)
                        .load(imageUri)
                        .circleCrop() // Recorta a imagem para ser circular
                        .apply(new RequestOptions().placeholder(R.drawable.ic_baseline_restaurant_menu_60))
                        .into(binding.imageViewMeal);
            }
        });
        // Registrar o contrato para a câmera
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                    if (success && imageUri != null) {
                        // Foto tirada com sucesso, carregar na ImageView
                        Glide.with(this)
                                .load(imageUri)
                                .circleCrop() // Recorta a imagem para ser circular
                                .apply(new RequestOptions().placeholder(R.drawable.ic_baseline_restaurant_menu_60))
                                .into(binding.imageViewMeal);
                    } else {
                        Toast.makeText(context, R.string.cancel, Toast.LENGTH_LONG).show();
                    }
                }
        );
        binding.imageViewMeal.setOnClickListener(v ->
                showImagePickerDialog(v.getContext())
        );
        return dialog;
    }

    private void showImagePickerDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Escolha uma opção");
        builder.setItems(new CharSequence[]{"Tirar Foto", "Escolher da Galeria"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                        activityResultLauncherCamera.launch(Manifest.permission.CAMERA);
                    } else
                        openCamera(context); // Abrir a câmera
                    break;
                case 1:
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        activityResultLauncherPicker.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    } else
                        openImagePicker(); // Abrir a galeria
                    break;
            }
        });
        builder.show();
    }

    //    Crie um arquivo temporário para salvar a foto tirada
    private File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* Prefixo */
                ".jpg",         /* Sufixo */
                storageDir      /* Diretório */
        );
    }

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    private void openCamera(Context context) {
        File photoFile = null;
        try {
            photoFile = createImageFile(context); // Criar arquivo temporário
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (photoFile != null) {
            imageUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", photoFile);
            takePictureLauncher.launch(imageUri); // Abrir a câmera
        }
    }

    private boolean isEmptyField(String value) {
        return (TextUtils.isEmpty(value) || value.trim().isEmpty());
    }

    private static void addNumberSpinner(AppCompatSpinner spinner, Context context) {
        ArrayList<Integer> quantity = new ArrayList<>();
        for (int i = 0; i <= 1000; ++i)
            quantity.add(i);

        ArrayAdapter<Integer> itemAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, quantity);
        spinner.setAdapter(itemAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}

