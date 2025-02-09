package com.antonioteca.cc42.ui.meal;


import static com.antonioteca.cc42.dao.daofarebase.DaoMealFirebase.extractPublicIdFromUrl;
import static com.antonioteca.cc42.dao.daofarebase.DaoMealFirebase.updateMealDataInFirebase;

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
import android.view.LayoutInflater;
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
import com.antonioteca.cc42.model.Meal;
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
        addNumberSpinner(binding.spinnerQuantity, context);
        DialogFragmentCreateMealArgs args = DialogFragmentCreateMealArgs.fromBundle(getArguments());
        Meal meal = args.getMeal();
        int cursusId = args.getCursusId();
        boolean isCreate = args.getIsCreate();

        if (!isCreate && meal != null) {
            Uri imageUri = Uri.parse(meal.getPathImage());
            this.imageUri = imageUri;
            loadingImageMeal(imageUri);
            binding.textInputEditTextName.setText(meal.getName());
            binding.textInputEditTextDescription.setText(meal.getDescription());
            binding.spinnerQuantity.setSelection(meal.getQuantity());
            binding.buttonCreateMeal.setText(getText(R.string.ok));
        } else {
            loadingImageMeal(imageUri);
            binding.buttonCreateMeal.setText(getText(R.string.create_meal));
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setIcon(R.drawable.logo_42);
        builder.setView(binding.getRoot());
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        String colorCoalition = user.coalition.getColor();
        if (colorCoalition != null) {
            int color = Color.parseColor(colorCoalition);
            ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(colorCoalition));
            binding.progressBarMeal.setIndeterminateTintList(colorStateList);
            binding.buttonClose.setBackgroundColor(color);
            binding.buttonCreateMeal.setBackgroundColor(color);
        }

        binding.buttonCreateMeal.setOnClickListener(v -> createUpdateMeal(meal, isCreate, cursusId));
        binding.buttonClose.setOnClickListener(v -> dialog.dismiss());
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), imageUri -> {
            // Imagem selecionada da galeria
            if (imageUri != null) {
                this.imageUri = imageUri;
                loadingImageMeal(imageUri);
            }
        });
        // Registrar o contrato para a câmera
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                    if (success && imageUri != null) {
                        loadingImageMeal(imageUri);
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

    private boolean validateData(Meal meal, boolean isCreate, LayoutInflater layoutInflater) {
        String mealName = binding.textInputEditTextName.getText().toString();
        String mealDescription = binding.textInputEditTextDescription.getText().toString();
        int mealsQauntity = 0;
        int selectedPosition = binding.spinnerQuantity.getSelectedItemPosition();
        if (selectedPosition != AdapterView.INVALID_POSITION) {
            mealsQauntity = (int) binding.spinnerQuantity.getItemAtPosition(selectedPosition);
        }
        if (isEmptyField(mealName)) {
            binding.textInputEditTextName.requestFocus();
            binding.textInputEditTextName.setError(getString(R.string.invalid_name_meal));
            return false;
        }
        if (!isCreate && meal != null
                && this.imageUri.equals(Uri.parse(meal.getPathImage())) && mealName.equals(meal.getName())
                && mealDescription.equals(meal.getDescription()) && mealsQauntity == meal.getQuantity()) {
            String message = context.getString(R.string.nothing_edit);
            Util.showAlertDialogMessage(context, layoutInflater, context.getString(R.string.warning), message, "#FDD835", null);
            return false;
        }
        return true;
    }

    private void createUpdateMeal(Meal meal, boolean isCreate, int cursusId) {
        if (validateData(meal, isCreate, getLayoutInflater())) {
            binding.buttonClose.setEnabled(false);
            binding.buttonCreateMeal.setEnabled(false);
            binding.progressBarMeal.setVisibility(View.VISIBLE);
            if (isCreate && meal == null) {
                if (imageUri != null) {
                    DaoMealFirebase.uploadImageToCloudinary(
                            firebaseDatabase,
                            getLayoutInflater(),
                            binding,
                            context,
                            String.valueOf(user.getCampusId()),
                            String.valueOf(cursusId),
                            imageUri
                    );
                } else {
                    DaoMealFirebase.saveMealToFirebase(
                            firebaseDatabase,
                            getLayoutInflater(),
                            binding,
                            context,
                            String.valueOf(user.getCampusId()),
                            String.valueOf(cursusId),
                            ""
                    );
                }
            } else {
                if (!imageUri.equals(Uri.parse(meal.getPathImage()))) {
                    int mealsQauntity = 0;
                    int selectedPosition = binding.spinnerQuantity.getSelectedItemPosition();
                    if (selectedPosition != AdapterView.INVALID_POSITION) {
                        mealsQauntity = (int) binding.spinnerQuantity.getItemAtPosition(selectedPosition);
                    }
                    DaoMealFirebase.uploadNewImage(
                            firebaseDatabase,
                            getLayoutInflater(),
                            binding,
                            context,
                            String.valueOf(user.getCampusId()),
                            String.valueOf(cursusId),
                            meal.getId(),
                            imageUri,
                            extractPublicIdFromUrl(meal.getPathImage()),
                            binding.textInputEditTextName.getText().toString().equals(meal.getName())
                                    && binding.textInputEditTextDescription.getText().toString().equals(meal.getDescription())
                                    && mealsQauntity == meal.getQuantity());
                } else {
                    updateMealDataInFirebase(
                            firebaseDatabase,
                            getLayoutInflater(),
                            binding,
                            context,
                            String.valueOf(user.getCampusId()),
                            String.valueOf(cursusId),
                            meal.getId(),
                            null); // Actualizar todos os dados no Firebase
                }
            }
        }
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

    private void loadingImageMeal(Uri imageUri) {
        Glide.with(this)
                .load(imageUri)
                .circleCrop()
                .apply(new RequestOptions().placeholder(R.drawable.ic_baseline_restaurant_menu_60))
                .into(binding.imageViewMeal);
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

