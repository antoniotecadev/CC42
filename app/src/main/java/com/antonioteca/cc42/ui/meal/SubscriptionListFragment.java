package com.antonioteca.cc42.ui.meal;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.dao.daofarebase.DaoSusbscriptionFirebase;
import com.antonioteca.cc42.databinding.DialogFilterBinding;
import com.antonioteca.cc42.databinding.FragmentSubscriptionListBinding;
import com.antonioteca.cc42.factory.UserViewModelFactory;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.repository.UserRepository;
import com.antonioteca.cc42.utility.AESUtil;
import com.antonioteca.cc42.utility.EndlessScrollListener;
import com.antonioteca.cc42.utility.Loading;
import com.antonioteca.cc42.utility.NFCUtils;
import com.antonioteca.cc42.utility.PdfCreator;
import com.antonioteca.cc42.utility.PdfSharer;
import com.antonioteca.cc42.utility.PdfViewer;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.SharedViewModel;
import com.antonioteca.cc42.viewmodel.UserViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubscriptionListFragment extends Fragment {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private User user;
    private Meal meal;
    private Loading l;
    private Context context;
    private Integer campusId;
    private Integer cursusId;
    private Integer cameraId = 0; // 0 = Traseira, 1 = Frontal
    private Activity activity;
    private String colorCoalition;
    private View inflatedViewStub;
    private MenuProvider menuProvider;
    private UserViewModel userViewModel;
    private LayoutInflater layoutInflater;
    private SharedViewModel sharedViewModel;
    private FirebaseDatabase firebaseDatabase;
    private Set<Long> allBlockedUsersListId;
    private ProgressBar progressBarSubscription;
    private FragmentSubscriptionListBinding binding;
    private SubscriptionListAdapter subscriptionListAdapter;

    // Componentes do CameraX e ML Kit
    private PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private BarcodeScanner barcodeScanner;
    private ExecutorService cameraExecutor;
    private boolean isProcessingBarcode = false;

    private String rangeParam = null;
    private Boolean activeParam = true;

    private int numberMealReceived = 0;
    private int numberUserSubscription = 0;
    private int numberUserUnsubscription = 0;
    private int numberUserSubscriptionSecondPortion = 0;
    private int numberUserNotSubscriptionSecondPortion = 0;
    final boolean[] isFlashLightOn = {false};

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result)
                    openCamera(this.cameraId);
                else
                    Util.showAlertDialogBuild(getString(R.string.err), getString(R.string.msg_permis_camera_denied), context, null);
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        l = new Loading();
        context = requireContext();
        user = new User(context);
        activity = requireActivity();
        layoutInflater = getLayoutInflater();
        allBlockedUsersListId = new HashSet<>();
        colorCoalition = new Coalition(context).getColor();
        subscriptionListAdapter = new SubscriptionListAdapter();
        firebaseDatabase = FirebaseDataBaseInstance.getInstance().database;
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        UserRepository userRepository = new UserRepository(context);
        UserViewModelFactory userViewModelFactory = new UserViewModelFactory(userRepository);
        userViewModel = new ViewModelProvider(this, userViewModelFactory).get(UserViewModel.class);
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);

        cameraExecutor = Executors.newSingleThreadExecutor();

        // Configura o scanner do ML Kit estritamente para QR Codes
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (previewView.isShown()) {
                    closeCamera();
                    return;
                }
                if (isEnabled()) {
                    setEnabled(false);
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSubscriptionListBinding.inflate(inflater, container, false);
        if (!user.isStaff()) {
            binding.fabOpenReaderNFC.setVisibility(View.GONE);
            binding.fabOpenCameraScannerQrCodeBack.setVisibility(View.GONE);
            binding.fabOpenCameraScannerQrCodeFront.setVisibility(View.GONE);
        }
        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled())
                Util.showAlertDialogBuild(context.getString(R.string.err), context.getString(R.string.msg_nfc_not_enabled), context, null);
            else {
                Object[] objects = NFCUtils.startNFC(nfcAdapter, activity);
                nfcAdapter = (NfcAdapter) objects[0];
                pendingIntent = (PendingIntent) objects[1];
                intentFiltersArray = (IntentFilter[]) objects[2];
                techListsArray = (String[][]) objects[3];
            }
        } else
            binding.fabOpenReaderNFC.setVisibility(View.GONE);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activeScrollListener();
        SubscriptionListFragmentArgs args = SubscriptionListFragmentArgs.fromBundle(requireArguments());
        meal = args.getMeal();
        campusId = user.getCampusId();
        cursusId = args.getCursusId();

        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(String.valueOf(meal.getName()));
        }
        toolbar = activityApp.findViewById(R.id.toolbar);
        binding.recyclerviewSubscriptionList.setHasFixedSize(true);
        binding.recyclerviewSubscriptionList.setLayoutManager(new LinearLayoutManager(context));

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            Util.showAlertDialogBuild(getString(R.string.subscriptions_list), meal.getName(), context, () -> {
                subscriptionListAdapter.isFilterSecondPortion = false;
                setupVisibility(binding, View.GONE, true, View.GONE, View.VISIBLE);
                l.currentPage = 1;
                activeScrollListener();
                subscriptionListAdapter.clean();
                userViewModel.getUserIdsSubscriptionList(firebaseDatabase, String.valueOf(user.getCampusId()), String.valueOf(cursusId), String.valueOf(meal.getId()), context, layoutInflater);
            });
            binding.swipeRefreshLayout.setRefreshing(false);
        });

        // Infla e mapeia o PreviewView do CameraX vindo do ViewStub alterado
        inflatedViewStub = binding.viewStub.inflate();
        inflatedViewStub.setVisibility(View.GONE);
        previewView = inflatedViewStub.findViewById(R.id.previewView);

        progressBarSubscription = binding.progressBarSubscription;
        if (colorCoalition != null) {
            ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(colorCoalition));
            binding.layoutQuantity.buttonDecrement.setStrokeColor(colorStateList);
            binding.layoutQuantity.buttonIncrement.setStrokeColor(colorStateList);
            binding.fabOpenReaderNFC.setBackgroundTintList(colorStateList);
            binding.fabOpenCameraScannerQrCodeBack.setBackgroundTintList(colorStateList);
            binding.fabOpenCameraScannerQrCodeFront.setBackgroundTintList(colorStateList);
            binding.progressindicator.setIndicatorColor(Color.parseColor(colorCoalition));
            progressBarSubscription.setIndeterminateTintList(ColorStateList.valueOf(Color.parseColor(colorCoalition)));
        }

        binding.fabOpenReaderNFC.setOnClickListener(v -> NFCUtils.startReaderNFC(
                nfcAdapter, activity, context, pendingIntent, intentFiltersArray, techListsArray,
                null, binding.radioGroupPortion, binding.layoutQuantity
        ));

        binding.fabOpenCameraScannerQrCodeBack.setOnClickListener(v -> openCameraScannerQrCodeSubscriptio(0));
        binding.fabOpenCameraScannerQrCodeFront.setOnClickListener(v -> openCameraScannerQrCodeSubscriptio(1));
        binding.fabOpenCameraScannerQrCodeClose.setOnClickListener(v -> closeCamera());

        // Lógica de activação dinâmica da Lanterna (Torch)
        inflatedViewStub.setOnLongClickListener(v -> {
            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                isFlashLightOn[0] = !isFlashLightOn[0];
                camera.getCameraControl().enableTorch(isFlashLightOn[0]);
                Snackbar.make(requireView(), isFlashLightOn[0] ? R.string.on_flashlight : R.string.off_flashlight, Snackbar.LENGTH_SHORT).show();
            }
            return true;
        });

        binding.radioGroupPortion.radioGroupMealPortion.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.radioButtonFirstPortion) {
                binding.radioGroupPortion.checkBoxSecondPortion.setVisibility(View.GONE);
            } else {
                binding.radioGroupPortion.checkBoxSecondPortion.setVisibility(View.VISIBLE);
            }
        });

        binding.layoutQuantity.buttonDecrement.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(binding.layoutQuantity.textViewQuantityValue.getText().toString());
            if (currentQuantity > 1) {
                binding.layoutQuantity.textViewQuantityValue.setText(String.valueOf(currentQuantity - 1));
            }
        });

        binding.layoutQuantity.buttonIncrement.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(binding.layoutQuantity.textViewQuantityValue.getText().toString());
            if (currentQuantity < 9)
                binding.layoutQuantity.textViewQuantityValue.setText(String.valueOf(currentQuantity + 1));
        });

        userViewModel.fetchAllBlockedUsers(firebaseDatabase, String.valueOf(campusId), String.valueOf(cursusId), allBlockedUsersListId, null, () -> {
        });

        binding.progressBarSubscription.setVisibility(View.VISIBLE);
        userViewModel.getUserIdsSubscriptionList(firebaseDatabase, String.valueOf(user.getCampusId()), String.valueOf(cursusId), String.valueOf(meal.getId()), context, layoutInflater);
        userViewModel.getUserIdsAndQuantityList().observe(getViewLifecycleOwner(), userIdsAndQuantity -> {
            this.userIds = userIdsAndQuantity.getKey();
            this.numberMealReceived = userIdsAndQuantity.getValue();
            userViewModel.getUsersSubscription(cursusId, l, context, activeParam, rangeParam);
        });

        userViewModel.getUsersSubscriptionLiveData().observe(getViewLifecycleOwner(), users -> {
            var adapter = binding.recyclerviewSubscriptionList.getAdapter();
            if (!users.isEmpty() && users.get(0) != null) {
                subscriptionListAdapter.updateUserList(users, context);
                binding.recyclerviewSubscriptionList.setAdapter(subscriptionListAdapter);
                if (userIds != null && !userIds.isEmpty()) {
                    subscriptionListAdapter.updateSubscriptionUser(userIds, allBlockedUsersListId);
                    setNumberUserChip();
                }
            } else if (adapter != null && adapter.getItemCount() > 0)
                setupVisibility(binding, View.GONE, false, View.GONE, View.VISIBLE);
            else
                setupVisibility(binding, View.GONE, false, View.VISIBLE, View.GONE);
        });

        sharedViewModel.getUserIdLiveData().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                Long userId = event.getContentIfNotHandled();
                if (userId == null) return;
                int quantity = Integer.parseInt(binding.layoutQuantity.textViewQuantityValue.getText().toString());
                if (getPortionSelected() == null) {
                    subscriptionListAdapter.updateSubscriptionUserSingle(userId, true);
                    binding.chipSubscription.setText(String.valueOf(++numberUserSubscription));
                    binding.chipUnsubscription.setText(String.valueOf(Math.max(--numberUserUnsubscription, 0)));
                } else {
                    subscriptionListAdapter.updateSubscriptionUserSingle(userId, false);
                    binding.chipNumberSubscribedSecondPortion.setText(String.valueOf(++numberUserSubscriptionSecondPortion));
                    numberUserNotSubscriptionSecondPortion = Math.max(--numberUserNotSubscriptionSecondPortion, 0);
                }
                numberMealReceived += quantity;
                meal.setQuantityReceived(numberMealReceived);
                binding.chipNumberMealReceived.setText(String.valueOf(numberMealReceived));
                meal.setQuantityNotReceived(meal.getQuantityNotReceived() - quantity);
                binding.chipNumberMealNotReceived.setText(String.valueOf(Math.max(meal.getQuantityNotReceived(), 0)));
            }
        });

        // Os demais observers de erros e menus permanecem inalterados
        setupMenuAndErrorObservers();
    }

    private void openCameraScannerQrCodeSubscriptio(int cameraId) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            this.cameraId = cameraId;
            activityResultLauncher.launch(Manifest.permission.CAMERA);
        } else {
            if (previewView.isShown())
                closeCamera();
            else
                openCamera(cameraId);
        }
    }

    private void openCamera(int cameraId) {
        this.cameraId = cameraId;
        toggleToolbarVisibity();

        inflatedViewStub.setVisibility(View.VISIBLE);
        binding.fabOpenReaderNFC.setVisibility(View.GONE);
        binding.fabOpenCameraScannerQrCodeBack.setVisibility(View.GONE);
        binding.fabOpenCameraScannerQrCodeFront.setVisibility(View.GONE);
        binding.fabOpenCameraScannerQrCodeClose.setVisibility(View.VISIBLE);
        binding.layoutQuantity.liniearLayoutQuantity.setVisibility(View.VISIBLE);
        binding.radioGroupPortion.radioGroupMealPortion.setVisibility(View.VISIBLE);

        if (binding.radioGroupPortion.radioButtonSecondPortion.isChecked())
            binding.radioGroupPortion.checkBoxSecondPortion.setVisibility(View.VISIBLE);

        isProcessingBarcode = false;
        startCameraX(cameraId);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void startCameraX(int targetCameraId) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();

                // Define preview de imagem na tela
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Configurações de resolução (se não houver 1280x720 ele escolhe a maos próxima)
                ResolutionStrategy resolutionStrategy = new ResolutionStrategy(new Size(1280, 720),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                );

                // Configura o seletor de resolução
                ResolutionSelector resolutionSelector = new ResolutionSelector.Builder()
                        .setResolutionStrategy(resolutionStrategy)
                        .build();

                // Configura o fluxo contínuo para análise de frames em tempo real
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setResolutionSelector(resolutionSelector)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                // Instancia o analisador de código de barras
                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImageFrame);

                CameraSelector cameraSelector = targetCameraId == 1 ?
                        CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;

                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(context, "Erro ao iniciar a câmera.", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    // Método que intercepta cada frame e processa via Google ML Kit
    @androidx.camera.core.ExperimentalGetImage
    private void analyzeImageFrame(@NonNull ImageProxy imageProxy) {
        if (imageProxy.getImage() == null || isProcessingBarcode) {
            imageProxy.close();
            return;
        }

        com.google.mlkit.vision.common.InputImage inputImage =
                com.google.mlkit.vision.common.InputImage.fromMediaImage(
                        imageProxy.getImage(),
                        imageProxy.getImageInfo().getRotationDegrees()
                );

        barcodeScanner.process(inputImage)
                .addOnSuccessListener(barcodes -> {
                    if (!barcodes.isEmpty() && !isProcessingBarcode) {
                        Barcode barcode = barcodes.get(0);
                        String rawValue = barcode.getRawValue();
                        if (rawValue != null) {
                            isProcessingBarcode = true; // Trava para evitar leituras duplicadas simultâneas
                            activity.runOnUiThread(() -> processQrCodeResult(rawValue));
                        }
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    // Guarda o ID do último usuário que foi escaneado com sucesso
    private static String lastScannedQrText = "";
    // Guarda o timestamp (em milissegundos) da última leitura com sucesso
    private static long lastScanTime = 0;
    // Tempo em milissegundos para permitir ler o MESMO QR Code novamente (2000ms = 2 segundos)
    private static final long SCAN_DEBOUNCE_DELAY = 2000;

    private void processQrCodeResult(@NonNull String qrCodeText) {
        // TRAVA DE DUPLICIDADE: Verifica se é o mesmo QR Code lido nos últimos 2 segundos
        long currentTime = System.currentTimeMillis();
        if (qrCodeText.equals(lastScannedQrText) && (currentTime - lastScanTime) < SCAN_DEBOUNCE_DELAY) {
            // Ignora silenciosamente, libera a câmera para o próximo frame e sai do método
            isProcessingBarcode = false;
            return;
        }
        // Se for um QR Code diferente ou se passou o tempo, actualiza o histórico
        lastScannedQrText = qrCodeText;
        lastScanTime = currentTime;
        // Só toca o Beep e processa se passou na validação acima
        playBeep();
        if (qrCodeText.isEmpty()) {
            Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), getString(R.string.msg_qr_code_invalid), "#FDD835", null, () -> isProcessingBarcode = false);
        } else {
            String result = AESUtil.decrypt(qrCodeText);
            if (result != null && result.startsWith("cc42user")) {
                String resultQrCode = result.replace("cc42user", "");
                String[] partsQrCode = resultQrCode.split("#", 6);
                if (partsQrCode.length == 6) {
                    progressBarSubscription.setVisibility(View.VISIBLE);
                    if (binding.radioGroupPortion.checkBoxBlocked.isChecked() && allBlockedUsersListId.contains(Long.valueOf(partsQrCode[0]))) {
                        progressBarSubscription.setVisibility(View.INVISIBLE);
                        Util.showAlertDialogMessage(context, layoutInflater, getString(R.string.blocked), getString(R.string.msg_user_blocked_subscription), "#E53935", partsQrCode[5], () -> isProcessingBarcode = false);
                        return;
                    }
                    boolean checkSubscription = binding.radioGroupPortion.checkBoxSecondPortion.isChecked();
                    DaoSusbscriptionFirebase.subscription(
                            firebaseDatabase,
                            Integer.parseInt(binding.layoutQuantity.textViewQuantityValue.getText().toString()),
                            checkSubscription,
                            getPortionSelected(),
                            String.valueOf(meal.getId()),
                            null,
                            partsQrCode[0], partsQrCode[1], partsQrCode[2],
                            String.valueOf(cursusId), partsQrCode[4], partsQrCode[5],
                            context, layoutInflater, progressBarSubscription, sharedViewModel,
                            () -> isProcessingBarcode = false
                    );
                } else
                    Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), getString(R.string.msg_qr_code_invalid), "#FDD835", null, () -> isProcessingBarcode = false);
            } else
                Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), getString(R.string.msg_qr_code_invalid), "#FDD835", null, () -> isProcessingBarcode = false);
        }
    }

    private static MediaPlayer mediaPlayer;

    private void playBeep() {
        stopMedia();
        mediaPlayer = MediaPlayer.create(context, R.raw.beep);
        if (mediaPlayer != null) {
            Util.startVibration(context);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> stopMedia());
        }
    }

    private void stopMedia() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (IllegalStateException ignored) {
            }
            mediaPlayer.release(); // libera os recursos
            mediaPlayer = null;
        }
    }

    private void closeCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        isFlashLightOn[0] = false;
        toggleToolbarVisibity();

        inflatedViewStub.setVisibility(View.GONE);
        binding.fabOpenCameraScannerQrCodeClose.setVisibility(View.GONE);
        binding.fabOpenCameraScannerQrCodeBack.setVisibility(View.VISIBLE);
        binding.fabOpenCameraScannerQrCodeFront.setVisibility(View.VISIBLE);
        binding.layoutQuantity.liniearLayoutQuantity.setVisibility(View.GONE);
        binding.radioGroupPortion.radioGroupMealPortion.setVisibility(View.GONE);
        binding.radioGroupPortion.checkBoxBlocked.setVisibility(View.GONE);
        binding.radioGroupPortion.checkBoxSecondPortion.setVisibility(View.GONE);
        binding.fabOpenReaderNFC.setVisibility(nfcAdapter != null ? View.VISIBLE : View.GONE);
    }

    // Métodos utilitários de tratamento de erro e inicialização de layouts originais replicados abaixo...
    private Set<String> userIds;
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

    private void setupMenuAndErrorObservers() {
        userViewModel.getHttpSatusEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                desactiveScrollListener();
                int count = subscriptionListAdapter.getItemCount();
                if (count > 0) setNumberUserChip();
                String message = (count > 0 ? getString(R.string.msg_users_not_reload) : "");
                setupVisibility(binding, View.GONE, false, count > 0 ? View.GONE : View.VISIBLE, count > 0 ? View.VISIBLE : View.GONE);
                HttpStatus httpStatus = event.getContentIfNotHandled();
                if (httpStatus != null)
                    Util.showAlertDialogBuild(String.valueOf(httpStatus.getCode()), httpStatus.getDescription() + message, context, () -> {
                        setupVisibility(binding, View.VISIBLE, false, View.GONE, View.VISIBLE);
                        l.currentPage = 1;
                        activeScrollListener();
                        subscriptionListAdapter.clean();
                        userViewModel.getUsersSubscription(cursusId, l, context, activeParam, rangeParam);
                    });
            }
        });

        userViewModel.getHttpExceptionEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                desactiveScrollListener();
                int count = subscriptionListAdapter.getItemCount();
                if (count > 0) setNumberUserChip();
                String message = (count > 0 ? getString(R.string.msg_users_not_reload) : "");
                setupVisibility(binding, View.GONE, false, count > 0 ? View.GONE : View.VISIBLE, count > 0 ? View.VISIBLE : View.GONE);
                HttpException httpException = event.getContentIfNotHandled();
                if (httpException != null)
                    Util.showAlertDialogBuild(String.valueOf(httpException.getCode()), httpException.getDescription() + message, context, () -> {
                        setupVisibility(binding, View.VISIBLE, false, View.GONE, View.VISIBLE);
                        l.currentPage = 1;
                        activeScrollListener();
                        subscriptionListAdapter.clean();
                        userViewModel.getUsersSubscription(cursusId, l, context, activeParam, rangeParam);
                    });
            }
        });

        menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_attendance_list, menu);
                menu.findItem(R.id.action_list_reload).setVisible(false);
                menu.findItem(R.id.action_list_export_csv).setVisible(false);
                MenuItem menuItem = menu.findItem(R.id.action_search);
                SearchView searchView = (SearchView) menuItem.getActionView();
                if (searchView != null) {
                    searchView.setQueryHint(context.getString(R.string.name_login));
                    searchView.onActionViewExpanded();
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            subscriptionListAdapter.filterSearch(query);
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            subscriptionListAdapter.filterSearch(newText);
                            return false;
                        }
                    });
                }
                menu.findItem(R.id.action_one_list).setTitle(context.getString(R.string.with_signs));
                menu.findItem(R.id.action_two_list).setTitle(context.getString(R.string.without_signs));
                menu.findItem(R.id.action_three_list).setTitle(context.getString(R.string.all));
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment_content_navigation_drawer);
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_one_list)
                    subscriptionListAdapter.filterListStatus(true);
                else if (itemId == R.id.action_two_list)
                    subscriptionListAdapter.filterListStatus(false);
                else if (itemId == R.id.action_three_list)
                    subscriptionListAdapter.filterListStatus(null);
                else if (itemId == R.id.action_list_subscripted_second_portion) {
                    if (userIds != null && !userIds.isEmpty()) {
                        subscriptionListAdapter.filterUsersSubscriptedSecondPortion(userIds);
                    } else
                        Toast.makeText(context, getString(R.string.msg_error_get_ids_user_local), Toast.LENGTH_LONG).show();
                } else if (itemId == R.id.action_list_transcenders) {
                    subscriptionListAdapter.filterTranscenders();
                } else if (itemId == R.id.action_filter_advanced) {
                    showFilterDialog();
                } else if (itemId == R.id.action_list_print || itemId == R.id.action_list_share) {
                    boolean isShare = itemId == R.id.action_list_share;
                    ActivityResultLauncher<String> launcher = isShare ? requestPermissionLauncherSharer : requestPermissionLauncherViewer;
                    ActivityResultLauncher<Intent> intentLauncher = isShare ? requestIntentPermissionLauncherSharer : requestIntentPermissionLauncherViewer;
                    boolean hasPerm = Util.launchPermissionDocument(context, intentLauncher, launcher, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (hasPerm) {
                        List<User> userList = subscriptionListAdapter.getUserList();
                        if (userList.isEmpty())
                            Util.showAlertDialogBuild(getString(isShare ? R.string.list_share : R.string.list_print), getString(R.string.msg_subscription_list_empty), context, null);
                        else
                            printAndShareSubscriptionsList(userList, !isShare, isShare ? R.string.list_share : R.string.list_print);
                    }
                }
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        };
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner());
    }

    private void showFilterDialog() {
        DialogFilterBinding dialogFilterBinding = DialogFilterBinding.inflate(getLayoutInflater());
        dialogFilterBinding.editDays.setText(R.string._15);
        new AlertDialog.Builder(context)
                .setTitle("Filtrar Subscritos")
                .setView(dialogFilterBinding.getRoot())
                .setPositiveButton("Consultar", (dialog, which) -> {
                    String daysInput = dialogFilterBinding.editDays.getText().toString();
                    boolean onlyActive = dialogFilterBinding.checkActive.isChecked();
                    rangeParam = null;
                    activeParam = null;
                    if (!daysInput.isEmpty()) {
                        int days = Integer.parseInt(daysInput);
                        Instant startDate = Instant.now().minus(days, ChronoUnit.DAYS);
                        rangeParam = startDate.toString() + "," + Instant.now().toString();
                    }
                    if (onlyActive) activeParam = true;
                    subscriptionListAdapter.isFilterSecondPortion = false;
                    setupVisibility(binding, View.VISIBLE, false, View.GONE, View.VISIBLE);
                    l.currentPage = 1;
                    activeScrollListener();
                    subscriptionListAdapter.clean();
                    userViewModel.getUserIdsSubscriptionList(firebaseDatabase, String.valueOf(user.getCampusId()), String.valueOf(cursusId), String.valueOf(meal.getId()), context, layoutInflater);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void printAndShareSubscriptionsList(List<User> userList, boolean isPrint, int title) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setItems(R.array.array_subscriptions_list_qr_code_options, (dialog, selected) -> {
                    binding.progressindicator.setVisibility(View.VISIBLE);
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        if (selected == 0) {
                            File filePdf = PdfCreator.createPdfSubscriptionList(context, requireActivity(), meal, numberUserUnsubscription, numberUserSubscription, numberUserSubscriptionSecondPortion, numberUserNotSubscriptionSecondPortion, subscriptionListAdapter.getUserList(), binding.progressindicator, binding.textViewTotal);
                            if (filePdf != null) {
                                if (isPrint)
                                    PdfViewer.openPdf(context, filePdf, "application/pdf", getString(R.string.msg_no_pdf_viewing_applications_were_found));
                                else
                                    PdfSharer.sharePdf(context, filePdf, "application/pdf", context.getString(R.string.list_share));
                            } else
                                activity.runOnUiThread(() -> Util.showAlertDialogBuild(context.getString(R.string.err), context.getString(R.string.pdf_not_created), context, null));
                        } else {
                            List<File> filePdf = PdfCreator.createMultiplePdfQrCodes(requireActivity(), userList, campusId, cursusId, binding.progressindicator, binding.textViewTotal);
                            if (!filePdf.isEmpty()) {
                                File fileMergePdf = PdfCreator.mergePdfs(context, filePdf);
                                if (fileMergePdf != null) {
                                    if (isPrint)
                                        PdfViewer.openPdf(context, fileMergePdf, "application/pdf", getString(R.string.msg_no_pdf_viewing_applications_were_found));
                                    else
                                        PdfSharer.sharePdf(context, fileMergePdf, "application/pdf", context.getString(R.string.list_share));
                                } else
                                    activity.runOnUiThread(() -> Util.showAlertDialogBuild(context.getString(R.string.err), context.getString(R.string.pdf_not_created), context, null));
                            } else
                                activity.runOnUiThread(() -> Util.showAlertDialogBuild(context.getString(R.string.err), context.getString(R.string.pdf_not_created), context, null));
                        }
                        requireActivity().runOnUiThread(() -> {
                            binding.textViewTotal.setText("");
                            binding.progressindicator.setProgress(0);
                            binding.progressindicator.setVisibility(View.GONE);
                        });
                    });
                }).setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void activityResultContractsViewer(@NonNull Boolean result) {
        if (result) {
            List<User> userList = subscriptionListAdapter.getUserList();
            if (userList.isEmpty())
                Util.showAlertDialogBuild(getString(R.string.list_print), getString(R.string.msg_subscription_list_empty), context, null);
            else printAndShareSubscriptionsList(userList, true, R.string.list_print);
        } else
            Util.showAlertDialogBuild(getString(R.string.permission), getString(R.string.whithout_permission_cannot_print), context, null);
    }

    private void activityResultContractsSharer(@NonNull Boolean result) {
        if (result) {
            List<User> userList = subscriptionListAdapter.getUserList();
            if (userList.isEmpty())
                Util.showAlertDialogBuild(getString(R.string.list_share), getString(R.string.msg_subscription_list_empty), context, null);
            else printAndShareSubscriptionsList(userList, false, R.string.list_share);
        } else
            Util.showAlertDialogBuild(getString(R.string.permission), getString(R.string.whithout_permission_cannot_share), context, null);
    }

    private final ActivityResultLauncher<Intent> requestIntentPermissionLauncherViewer = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> activityResultContractsViewer(result.getResultCode() == Activity.RESULT_OK));
    private final ActivityResultLauncher<String> requestPermissionLauncherViewer = registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::activityResultContractsViewer);
    private final ActivityResultLauncher<Intent> requestIntentPermissionLauncherSharer = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> activityResultContractsSharer(result.getResultCode() == Activity.RESULT_OK));
    private final ActivityResultLauncher<String> requestPermissionLauncherSharer = registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::activityResultContractsSharer);

    RecyclerView.OnScrollListener onScrollListener = new EndlessScrollListener() {
        @Override
        public void onLoadMore() {
            if (!l.isLoading && l.hasNextPage) {
//                Toast.makeText(context, R.string.msg_loading_more_data, Toast.LENGTH_LONG).show();
                userViewModel.getUsersSubscription(cursusId, l, context, activeParam, rangeParam);
            } else {
//                Toast.makeText(context, R.string.synchronization, Toast.LENGTH_LONG).show();
                setupVisibility(binding, View.GONE, false, View.GONE, View.VISIBLE);
                desactiveScrollListener();
            }
        }
    };

    @Nullable
    private String getPortionSelected() {
        return binding.radioGroupPortion.radioGroupMealPortion.getCheckedRadioButtonId() == R.id.radioButtonFirstPortion ? null : "-";
    }

    private void setNumberUserChip() {
        int[] numberUser = subscriptionListAdapter.getNumberMealsReceivedUser();
        this.numberUserSubscription = numberUser[0];
        this.numberUserUnsubscription = numberUser[1];
        this.numberUserSubscriptionSecondPortion = numberUser[2];
        this.numberUserNotSubscriptionSecondPortion = numberUser[3];
        binding.chipSubscription.setText(String.valueOf(numberUserSubscription));
        binding.chipNumberMealReceived.setText(String.valueOf(numberMealReceived));
        binding.chipUnsubscription.setText(String.valueOf(numberUserUnsubscription));
        binding.chipNumberMealNotReceived.setText(String.valueOf(meal.getQuantityNotReceived()));
        binding.chipNumberSubscribedSecondPortion.setText(String.valueOf(numberUserSubscriptionSecondPortion));
    }

    private void activeScrollListener() {
        binding.recyclerviewSubscriptionList.addOnScrollListener(onScrollListener);
    }

    private void desactiveScrollListener() {
        binding.recyclerviewSubscriptionList.removeOnScrollListener(onScrollListener);
    }

    private void setupVisibility(@NonNull FragmentSubscriptionListBinding binding, int viewP, boolean refreshing, int viewT, int viewR) {
        binding.progressBarSubscription.setVisibility(viewP);
        binding.swipeRefreshLayout.setRefreshing(refreshing);
        binding.textViewEmptyData.setVisibility(viewT);
        binding.recyclerviewSubscriptionList.setVisibility(viewR);
    }

    public void nfcResult(@NonNull String QRCode) {
        // TRAVA DE DUPLICIDADE: Verifica se é o mesmo QR Code lido nos últimos 3.5 segundos
        long currentTime = System.currentTimeMillis();
        if (QRCode.equals(lastScannedQrText) && (currentTime - lastScanTime) < SCAN_DEBOUNCE_DELAY) {
            // Ignora silenciosamente, libera a câmera para o próximo frame e sai do método
            isProcessingBarcode = false;
            return;
        }
        // Se for um QR Code diferente ou se passou o tempo, actualiza o histórico
        lastScannedQrText = QRCode;
        lastScanTime = currentTime;
        // Só toca o Beep e processa se passou na validação acima
        playBeep();
        if (QRCode.isEmpty()) {
            Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), getString(R.string.not_found_text_pass), "#FDD835", null, () -> NFCUtils.startReaderNFC(nfcAdapter, activity, pendingIntent, intentFiltersArray, techListsArray));
        } else {
            String result = AESUtil.decrypt(QRCode);
            if (result != null && result.startsWith("cc42user")) {
                String resultQrCode = result.replace("cc42user", "");
                String[] partsQrCode = resultQrCode.split("#", 6);
                if (partsQrCode.length == 6) {
                    progressBarSubscription.setVisibility(View.VISIBLE);
                    if (binding.radioGroupPortion.checkBoxBlocked.isChecked() && allBlockedUsersListId.contains(Long.valueOf(partsQrCode[0]))) {
                        progressBarSubscription.setVisibility(View.INVISIBLE);
                        Util.showAlertDialogMessage(context, layoutInflater, getString(R.string.blocked), getString(R.string.msg_user_blocked_subscription), "#E53935", partsQrCode[5], () -> isProcessingBarcode = false);
                        return;
                    }
                    boolean checkSubscription = binding.radioGroupPortion.checkBoxSecondPortion.isChecked();
                    DaoSusbscriptionFirebase.subscription(
                            firebaseDatabase, Integer.parseInt(binding.layoutQuantity.textViewQuantityValue.getText().toString()),
                            checkSubscription, getPortionSelected(), String.valueOf(meal.getId()), null,
                            partsQrCode[0], partsQrCode[1], partsQrCode[2], String.valueOf(cursusId), partsQrCode[4], partsQrCode[5],
                            context, layoutInflater, progressBarSubscription, sharedViewModel,
                            () -> NFCUtils.startReaderNFC(nfcAdapter, activity, pendingIntent, intentFiltersArray, techListsArray)
                    );
                } else
                    Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), getString(R.string.not_found_text_pass), "#FDD835", null, () -> NFCUtils.startReaderNFC(nfcAdapter, activity, pendingIntent, intentFiltersArray, techListsArray));
            } else
                Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), getString(R.string.not_found_text_pass), "#FDD835", null, () -> NFCUtils.startReaderNFC(nfcAdapter, activity, pendingIntent, intentFiltersArray, techListsArray));
        }
    }

    public void resolveIntent(@NonNull Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                StringBuilder resultado = new StringBuilder();
                for (Parcelable rawMsg : rawMsgs) {
                    NdefMessage msg = (NdefMessage) rawMsg;
                    for (NdefRecord record : msg.getRecords()) {
                        if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
                            try {
                                resultado.append(NFCUtils.getString(record));
                            } catch (Exception e) {
                                NFCUtils.showAlertDialogBuild(context.getString(R.string.err), getString(R.string.erro_process_text_pass), context, () -> NFCUtils.startReaderNFC(nfcAdapter, activity, pendingIntent, intentFiltersArray, techListsArray));
                                return;
                            }
                        }
                    }
                }
                if (resultado.length() > 0)
                    nfcResult(resultado.toString().trim());
                else
                    NFCUtils.showAlertDialogBuild(getString(R.string.without_text), getString(R.string.not_found_text_pass), context, () -> NFCUtils.startReaderNFC(nfcAdapter, activity, pendingIntent, intentFiltersArray, techListsArray));
            } else
                NFCUtils.showAlertDialogBuild(getString(R.string.without_data), getString(R.string.without_message_NDEF), context, () -> NFCUtils.startReaderNFC(nfcAdapter, activity, pendingIntent, intentFiltersArray, techListsArray));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (previewView.isShown()) {
            startCameraX(this.cameraId);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopMedia();
        cameraExecutor.shutdown();
        binding = null;
        requireActivity().removeMenuProvider(menuProvider);
    }
}