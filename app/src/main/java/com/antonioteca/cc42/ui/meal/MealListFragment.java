package com.antonioteca.cc42.ui.meal;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.dao.daofarebase.DaoSusbscriptionFirebase;
import com.antonioteca.cc42.databinding.FragmentMealBinding;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Cursu;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
import com.antonioteca.cc42.utility.AESUtil;
import com.antonioteca.cc42.utility.Loading;
import com.antonioteca.cc42.utility.MealsUtils;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.MealViewModel;
import com.antonioteca.cc42.viewmodel.SharedViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ScanOptions;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import java.util.List;

public class MealListFragment extends Fragment {


    private User user;
    private int cursusId;
    private Context context;
    private Loading loading;
    private Integer cameraId;
    private View inflatedViewStub;
    private BeepManager beepManager;
    private ScanOptions scanOptions;
    private MealAdapter mealAdapter;
    private FragmentActivity activity;
    private DatabaseReference mealsRef;
    private FragmentMealBinding binding;
    private MealViewModel mealViewModel;
    private SharedViewModel sharedViewModel;
    private FirebaseDatabase firebaseDatabase;
    private DecoratedBarcodeView decoratedBarcodeView;

    final long DOUBLE_CLICK_TIME_DELTA = 300; // Tempo máximo entre cliques (em milisegundos)
    final long[] lastClickTime = {0};
    final boolean[] isFlashLightOn = {false};

    private Toolbar toolbar;
    private AppCompatActivity activityApp;

    private void toggleToolbarVisibity() {
        if (toolbar.getVisibility() == View.VISIBLE) {
            Util.hideToolbar(toolbar);
        } else {
            Util.showToolbar(toolbar);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getActivity() != null) {
            activityApp = (AppCompatActivity) getActivity();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loading = new Loading();
        context = requireContext();
        user = new User(context);
        activity = requireActivity();
        scanOptions = new ScanOptions();
        user.coalition = new Coalition(context);
        beepManager = new BeepManager(activity);
        firebaseDatabase = FirebaseDataBaseInstance.getInstance().database;
        mealViewModel = new ViewModelProvider(this).get(MealViewModel.class);
        sharedViewModel = new ViewModelProvider(activity).get(SharedViewModel.class);

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if (inflatedViewStub != null && inflatedViewStub.getVisibility() == View.VISIBLE) {
                    closeCamera();
                    return;
                }

                if (isEnabled()) { // Verifica se ainda está habilitado
                    setEnabled(false); // Importante para não criar um loop infinito
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };

        // Adicione o callback ao dispatcher
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMealBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MealListFragmentArgs args = MealListFragmentArgs.fromBundle(getArguments());
        Cursu cursu = args.getCursu();
        int campusId = user.getCampusId();
        long userId = user.getUid();
        this.cursusId = cursu.getId();
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(String.valueOf(cursu.getName()));
        }

        toolbar = activityApp.findViewById(R.id.toolbar);

        scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        scanOptions.setPrompt(getString(R.string.align_camera_qr_code));
        scanOptions.setOrientationLocked(false);
        scanOptions.setCameraId(0);
        scanOptions.setBeepEnabled(true);
        scanOptions.setBarcodeImageEnabled(false); // Não capturar e retornar a imagem do código scaneado

        inflatedViewStub = binding.viewStub.inflate();
        inflatedViewStub.setVisibility(View.GONE);

        decoratedBarcodeView = inflatedViewStub.findViewById(R.id.decoratedBarcodeView);

        decoratedBarcodeView.initializeFromIntent(scanOptions.createScanIntent(context));
        decoratedBarcodeView.decodeContinuous(callback);

        inflatedViewStub.setOnLongClickListener(v -> {
            if (isFlashLightOn[0]) {
                isFlashLightOn[0] = false;
                decoratedBarcodeView.setTorchOff();
            } else {
                isFlashLightOn[0] = true;
                decoratedBarcodeView.setTorchOn();
            }
            return true;
        });

        inflatedViewStub.setOnClickListener(v -> {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime[0] < DOUBLE_CLICK_TIME_DELTA) {
                closeCamera();
            }
            lastClickTime[0] = clickTime;
        });

        decoratedBarcodeView.setTorchListener(new DecoratedBarcodeView.TorchListener() {
            @Override
            public void onTorchOn() {
                Snackbar.make(requireView(), R.string.on_flashlight, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onTorchOff() {
                Snackbar.make(requireView(), R.string.off_flashlight, Snackbar.LENGTH_LONG).show();
            }
        });

        mealsRef = firebaseDatabase.getReference("campus").child(String.valueOf(campusId))
                .child("cursus")
                .child(String.valueOf(cursu.getId()))
                .child("meals");

        binding.recyclerViewMeal.setHasFixedSize(true);
        binding.recyclerViewMeal.setLayoutManager(new LinearLayoutManager(context));

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            MealsUtils.setupVisibility(binding, View.INVISIBLE, true, View.INVISIBLE, View.VISIBLE);
            mealAdapter.idMealQrCode.clear();
            mealAdapter.listMealQrCode.clear();
            mealAdapter.mealList.clear();
            mealViewModel.mealList.clear();
            mealAdapter.notifyDataSetChanged();
            mealViewModel.loadMeals(context, binding, mealsRef, null, userId);
        });

        String colorCoalition = user.coalition.getColor();
        if (colorCoalition != null) {
//            int color = Color.parseColor(colorCoalition);
            ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(colorCoalition));
            binding.progressBarMeal.setIndeterminateTintList(colorStateList);
        }

        binding.fabOpenCameraScannerQrCodeClose.setOnClickListener(v -> closeCamera());

        mealAdapter = new MealAdapter(context,
                loading,
                binding,
                mealsRef,
                mealViewModel,
                firebaseDatabase,
                getLayoutInflater(),
                user.getUid(),
                campusId,
                cursu.getId());
        binding.recyclerViewMeal.setAdapter(mealAdapter);

        mealViewModel.getMealList(context, binding, mealsRef, null, userId).observe(getViewLifecycleOwner(), meals -> {
            if (!meals.isEmpty() && meals.get(0) != null) {
                mealAdapter.updateMealList(meals, meals.get(meals.size() - 1).getId());
                mealViewModel.mealList.addAll(meals);
            } else
                loading.isLoading = false;
        });

        sharedViewModel.getNewMealLiveData().observe(getViewLifecycleOwner(), event -> {
            Meal newMeal = event.getContentIfNotHandled();
            if (newMeal != null) {
                mealAdapter.addMeal(newMeal);
                binding.recyclerViewMeal.scrollToPosition(0);
            }
        });

        sharedViewModel.getUpdatedMealLiveData().observe(getViewLifecycleOwner(), event -> {
            Meal updatedMeal = event.getContentIfNotHandled();
            if (updatedMeal != null)
                mealAdapter.updateMeal(updatedMeal);
        });

        mealViewModel.getDeleteMealLiveData().observe(getViewLifecycleOwner(), event -> {
            Meal deleteMeal = event.getContentIfNotHandled();
            if (deleteMeal != null)
                mealAdapter.deleteMeal(deleteMeal);
        });

        sharedViewModel.getPathImageLiveData().observe(getViewLifecycleOwner(), event -> {
            List<String> list = event.getContentIfNotHandled();
            if (list != null) {
                String idMeal = list.get(0);
                String pathImage = list.get(1);
                mealAdapter.updatePathImage(idMeal, pathImage);
            }
        });

        MenuProvider menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_meal, menu);
                menu.findItem(R.id.action_view_qr_code_meals).setOnMenuItemClickListener(item -> {
                    if (!mealAdapter.listMealQrCode.isEmpty()) {
                        Util.showModalQrCode(context, mealAdapter.listMealQrCode, 0);
                    } else
                        Snackbar.make(view, R.string.meals_not_found, Snackbar.LENGTH_LONG).show();
                    return true;
                });
                menu.findItem(R.id.action_view_qr_code_meals_scanner).setOnMenuItemClickListener(item -> {
                    if (!mealAdapter.listMealQrCode.isEmpty()) {
                        if (decoratedBarcodeView.isShown())
                            closeCamera();
                        else
                            new AlertDialog.Builder(context)
                                    .setTitle(R.string.scanner)
                                    .setItems(R.array.array_scanner_qr_code_options, (dialog, cameraId) -> openCameraScannerQrCodeSubscriptio(cameraId)).setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                                    .show();
                    } else
                        Snackbar.make(view, R.string.meals_not_found, Snackbar.LENGTH_LONG).show();
                    return true;
                });
                menu.findItem(R.id.action_view_create_meal).setOnMenuItemClickListener(item -> {
                    MealListFragmentDirections.ActionNavMealToDialogFragmentCreateMeal actionNavMealToDialogFragmentCreateMeal =
                            MealListFragmentDirections.actionNavMealToDialogFragmentCreateMeal(true, cursu.getId());
                    Navigation.findNavController(view).navigate(actionNavMealToDialogFragmentCreateMeal);
                    return true;
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        };
        if (user.isStaff())
            activity.addMenuProvider(menuProvider, getViewLifecycleOwner());
        else
            activity.removeMenuProvider(menuProvider);
    }

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result)
                    openCamera(this.cameraId);
                else
                    Util.showAlertDialogBuild(getString(R.string.err), getString(R.string.msg_permis_camera_denied), context, null);
            });

    private void openCameraScannerQrCodeSubscriptio(int cameraId) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            this.cameraId = cameraId;
            activityResultLauncher.launch(Manifest.permission.CAMERA);
        } else {
            if (decoratedBarcodeView.isShown())
                closeCamera();
            else
                openCamera(cameraId);
        }
    }

    private void openCamera(int cameraId) {
        toggleToolbarVisibity();
        decoratedBarcodeView.pause();
        decoratedBarcodeView.getBarcodeView().setCameraSettings(createCameraSettings(cameraId));
        decoratedBarcodeView.resume();
        inflatedViewStub.setVisibility(View.VISIBLE);
        binding.fabOpenCameraScannerQrCodeClose.setVisibility(View.VISIBLE);
    }

    @NonNull
    private CameraSettings createCameraSettings(int cameraId) {
        CameraSettings cameraSettings = new CameraSettings();
        cameraSettings.setRequestedCameraId(cameraId);
        return cameraSettings;
    }

    private void closeCamera() {
        if (decoratedBarcodeView.isShown()) {
            if (isFlashLightOn[0]) {
                isFlashLightOn[0] = false;
                decoratedBarcodeView.setTorchOff();
            }
            decoratedBarcodeView.pause();
            toggleToolbarVisibity();
        }
        inflatedViewStub.setVisibility(View.GONE);
        binding.fabOpenCameraScannerQrCodeClose.setVisibility(View.GONE);
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(@NonNull BarcodeResult barcodeResult) {
            decoratedBarcodeView.pause();
            beepManager.playBeepSoundAndVibrate();
            if (barcodeResult.getText().isEmpty()) {
                Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), getString(R.string.msg_qr_code_invalid), "#FDD835", null, () -> decoratedBarcodeView.resume());
            } else {
                String result = AESUtil.decrypt(barcodeResult.getText());
                if (result != null && result.startsWith("cc42user")) {
                    String resultQrCode = result.replace("cc42user", "");
                    String[] partsQrCode = resultQrCode.split("#", 6);
                    if (partsQrCode.length == 6) {
                        if (!mealAdapter.listMealQrCode.isEmpty()) {
                            binding.progressBarMeal.setVisibility(View.VISIBLE);
                            DaoSusbscriptionFirebase.subscription(
                                    firebaseDatabase,
                                    mealAdapter.listMealQrCode,
                                    null,
                                    null,
                                    partsQrCode[0], /* userId */
                                    partsQrCode[1], /* login */
                                    partsQrCode[2], /* displayName */
                                    String.valueOf(cursusId),
                                    partsQrCode[4], /* campusId */
                                    partsQrCode[5], /* UrlImageUser */
                                    context,
                                    getLayoutInflater(),
                                    binding.progressBarMeal,
                                    sharedViewModel,
                                    () -> decoratedBarcodeView.resume()
                            );
                        } else {
                            Snackbar.make(requireView(), R.string.meals_not_found, Snackbar.LENGTH_LONG).show();
                            decoratedBarcodeView.resume();
                        }
                    } else
                        Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), getString(R.string.msg_qr_code_invalid), "#FDD835", null, () -> decoratedBarcodeView.resume());
                } else
                    Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), getString(R.string.msg_qr_code_invalid), "#FDD835", null, () -> decoratedBarcodeView.resume());
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        decoratedBarcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        decoratedBarcodeView.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        closeCamera();
        binding = null;
    }
}