package com.antonioteca.cc42.ui.meal;


import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
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
import androidx.lifecycle.ViewModelProvider;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentDialogCreateMealBinding;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.MealViewModel;
import com.antonioteca.cc42.viewmodel.SharedViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DialogFragmentCreateMeal extends DialogFragment {

    private User user;
    private Uri imageUri;
    private Context context;
    private AlertDialog dialog;
    private FragmentDialogCreateMealBinding binding;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private int mealsQuantity = 0;
    private MealViewModel mealViewModel;
    private SharedViewModel sharedViewModel;
    private FirebaseDatabase firebaseDatabase;
    private final List<String> selectedItems = new ArrayList<>();
    private final Map<AppCompatSpinner, Boolean> spinnerInitializedMap = new HashMap<>();

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

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData(); // Imagem selecionada da galeria
                    if (imageUri != null) {
                        this.imageUri = imageUri;
                        // Solicita permissão persistente para o URI
                        context.getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        // Carrega a imagem no ImageView
                        loadingImageMeal(imageUri);
                    }
                }
            }
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        user = new User(context);
        user.coalition = new Coalition(context);
        firebaseDatabase = FirebaseDataBaseInstance.getInstance().database;
        mealViewModel = new ViewModelProvider(this).get(MealViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        binding = FragmentDialogCreateMealBinding.inflate(getLayoutInflater());
        addMealsSpinner(binding.spinnerCarbohydratesMeals, true, R.array.meals_carbohydrates_title);
        addMealsSpinner(binding.spinnerProteinsLegumesVegetablesMeals, false, R.array.meals_proteins_legumes_vegetables_title);
        DialogFragmentCreateMealArgs args = DialogFragmentCreateMealArgs.fromBundle(getArguments());
        Meal meal = args.getMeal();
        int cursusId = args.getCursusId();
        boolean isCreate = args.getIsCreate();

        if (!isCreate && meal != null) {
            Uri imageUri = Uri.parse(meal.getPathImage());
            this.imageUri = imageUri;
            loadingImageMeal(imageUri);
            addChipsFromData(meal.getName());
            binding.quantityEditText.setText(String.valueOf(meal.getQuantity()));
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
        handleSpinnerMeals(binding.spinnerCarbohydratesMeals);
        handleSpinnerMeals(binding.spinnerProteinsLegumesVegetablesMeals);
        mealViewModel.getCreatedMealLiveData().observe(this, newMeal ->
                sharedViewModel.setNewMeal(newMeal)
        );
        mealViewModel.getUpdatedMealLiveData().observe(this, newMeal ->
                sharedViewModel.setUpdatedMeal(newMeal)
        );
        return dialog;
    }

    private void handleSpinnerMeals(@NonNull AppCompatSpinner appCompatSpinner) {
        appCompatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Verifica se o Spinner já foi inicializado
                Boolean isInitialized = spinnerInitializedMap.get(appCompatSpinner);
                if (isInitialized == null || !isInitialized) {
                    spinnerInitializedMap.put(appCompatSpinner, true);
                    return; // Sai do método sem executar o código abaixo
                }
                String selectedItem = parent.getItemAtPosition(position).toString();

                // Verifica se o item já foi adicionado
                if (!selectedItems.contains(selectedItem)) {
                    selectedItems.add(selectedItem); // Adiciona à lista
                    addChip(selectedItem); // Adiciona o Chip ao layout
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nada a fazer aqui
            }
        });
    }

    // Método para adicionar um Chip ao layout
    private void addChip(String mealTrim) {
        final Chip chip = getChip(mealTrim);
        binding.chipContainer.addView(chip); // Adiciona o Chip ao LinearLayout
        updateFinalText(); // Atualiza o texto final
    }

    private void addChipsFromData(@NonNull String data) {
        // Divide a string em um array de strings
        String[] items = data.split(",");

        // Itera sobre o array de strings
        for (String item : items) {
            // Remove espaços em branco extras
            String mealTrim = item.trim();
            // Cria um novo Chip
            final Chip chip = getChip(mealTrim);

            // Adiciona o Chip ao LinearLayout
            selectedItems.add(mealTrim); // Adicionar item  a lista
            binding.chipContainer.addView(chip);
            updateFinalText();
        }
    }

    @NonNull
    private Chip getChip(String mealTrim) {
        Chip chip = new Chip(context);
        chip.setText(mealTrim);
        chip.setCloseIconVisible(true);
        chip.setCloseIconTintResource(R.color.light_blue_900); // Cor do ícone de fechar
        chip.setOnCloseIconClickListener(v -> {
            binding.chipContainer.removeView(chip); // Remove o Chip do layout
            selectedItems.remove(mealTrim); // Remove o item da lista
            updateFinalText(); // Atualiza o texto final
        });
        return chip;
    }

    // Método para atualizar o EditText com os valores selecionados
    private void updateFinalText() {
        StringBuilder finalText = new StringBuilder();
        for (String item : selectedItems) {
            finalText.append(item).append(", ");
        }
        // Remove a última vírgula e espaço
        if (finalText.length() > 0) {
            finalText.setLength(finalText.length() - 2);
        }
        // Define o texto no EditText
        binding.mealsEditText.setText(finalText.toString());
    }

    private boolean validateData(Meal meal, boolean isCreate, LayoutInflater layoutInflater) {
        String mealsName = binding.mealsEditText.getText().toString();
        if (isEmptyField(mealsName)) {
            Toast.makeText(context, R.string.meals_not_found, Toast.LENGTH_LONG).show();
            return false;
        }
        if (!isCreate && meal != null
                && this.imageUri.equals(Uri.parse(meal.getPathImage()))
                && mealsName.equals(meal.getName())
                && getMealsQuantity() == meal.getQuantity()) {
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
                    mealViewModel.uploadImageToCloudinary(
                            firebaseDatabase,
                            getLayoutInflater(),
                            binding,
                            context,
                            user.getDisplayName(),
                            String.valueOf(user.getCampusId()),
                            String.valueOf(cursusId),
                            imageUri,
                            getMealsQuantity()
                    );
                } else {
                    mealViewModel.saveMealToFirebase(
                            firebaseDatabase,
                            getLayoutInflater(),
                            binding,
                            context,
                            user.getDisplayName(),
                            String.valueOf(user.getCampusId()),
                            String.valueOf(cursusId),
                            "",
                            getMealsQuantity()
                    );
                }
            } else {
                if (!imageUri.equals(Uri.parse(meal.getPathImage()))) {
                    mealViewModel.uploadNewImage(
                            firebaseDatabase,
                            getLayoutInflater(),
                            binding,
                            context,
                            meal,
                            user.getDisplayName(),
                            String.valueOf(user.getCampusId()),
                            String.valueOf(cursusId),
                            meal.getId(),
                            imageUri,
                            mealViewModel.extractPublicIdFromUrl(meal.getPathImage()),
                            binding.mealsEditText.getText().toString().equals(meal.getName()) && getMealsQuantity() == meal.getQuantity(),
                            getMealsQuantity());
                } else {
                    mealViewModel.updateMealDataInFirebase(
                            firebaseDatabase,
                            getLayoutInflater(),
                            binding,
                            context,
                            meal,
                            user.getDisplayName(),
                            String.valueOf(user.getCampusId()),
                            String.valueOf(cursusId),
                            meal.getId(),
                            null,
                            getMealsQuantity()); // Actualizar todos os dados no Firebase
                }
            }
        }
    }

    private int getMealsQuantity() {
        String quantityString = Objects.requireNonNull(binding.quantityEditText.getText()).toString();
        if (!TextUtils.isEmpty(quantityString))
            mealsQuantity = Integer.parseInt(quantityString);
        return mealsQuantity;
    }

    private void showImagePickerDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.choose_a_option);
        builder.setItems(new CharSequence[]{getString(R.string.pick_photo), getString(R.string.choose_galery)}, (dialog, which) -> {
            switch (which) {
                case 0:
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                        activityResultLauncherCamera.launch(Manifest.permission.CAMERA);
                    } else {
                        openCamera(context); // Abrir a câmera
                    }
                    break;
                case 1:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // Android 13+ (API 33+)
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_DENIED) {
                            activityResultLauncherPicker.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        } else {
                            openImagePicker(); // Abrir a galeria
                        }
                    } else {
                        // Android 10-12
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                            activityResultLauncherPicker.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        } else {
                            openImagePicker(); // Abrir a galeria
                        }
                    }
                    break;
            }
        });
        builder.show();
    }

    //    Crie um arquivo temporário para salvar a foto tirada
    @NonNull
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
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void openCamera(@NonNull Context context) {
        // Verifica se o dispositivo tem uma câmera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            File photoFile;
            try {
                photoFile = createImageFile(context); // Criar arquivo temporário
            } catch (IOException e) {
                Toast.makeText(context, getString(R.string.error_creating_image_file), Toast.LENGTH_LONG).show();
                return;
            }

            imageUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", photoFile);
            takePictureLauncher.launch(imageUri); // Abrir a câmera
        } else {
            Toast.makeText(context, getString(R.string.device_does_not_have_camera), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isEmptyField(String value) {
        return (TextUtils.isEmpty(value) || value.trim().isEmpty());
    }

    private void loadingImageMeal(Uri imageUri) {
        Glide.with(this)
                .load(imageUri)
                .circleCrop()
                .apply(new RequestOptions().placeholder(R.drawable.ic_baseline_restaurant_60))
                .into(binding.imageViewMeal);
    }

    public void addMealsSpinner(AppCompatSpinner spinner, boolean isCarbohydrates, int arrayString) {
        // Lista de seções
        List<String> sections = Arrays.asList(getResources().getStringArray(arrayString));
        // Itens de cada seção
        Map<String, List<String>> sectionItems = new HashMap<>();
        if (isCarbohydrates) {
            sectionItems.put(sections.get(0), Arrays.asList(getResources().getStringArray(R.array.rice_list)));
            sectionItems.put(sections.get(1), Arrays.asList(getResources().getStringArray(R.array.pasta_list)));
            sectionItems.put(sections.get(2), Arrays.asList(getResources().getStringArray(R.array.funge_list)));
            sectionItems.put(sections.get(3), Arrays.asList(getResources().getStringArray(R.array.potato_list)));
            sectionItems.put(sections.get(4), Arrays.asList(getResources().getStringArray(R.array.breads_list)));
        } else {
            sectionItems.put(sections.get(0), Arrays.asList(getResources().getStringArray(R.array.leguminous_list)));
            sectionItems.put(sections.get(1), Arrays.asList(getResources().getStringArray(R.array.meats_list)));
            sectionItems.put(sections.get(2), Arrays.asList(getResources().getStringArray(R.array.eggs_list)));
            sectionItems.put(sections.get(3), Arrays.asList(getResources().getStringArray(R.array.vegetables_and_salads_list)));
            sectionItems.put(sections.get(4), Arrays.asList(getResources().getStringArray(R.array.sauces_list)));
        }
        SectionedSpinnerAdapter adapter = new SectionedSpinnerAdapter(requireContext(), sections, sectionItems);
        spinner.setAdapter(adapter);
        /*
        Opcional: sem seções
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.meals_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        */
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