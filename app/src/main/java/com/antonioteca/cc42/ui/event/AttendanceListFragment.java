package com.antonioteca.cc42.ui.event;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.dao.daofarebase.DaoEventFirebase;
import com.antonioteca.cc42.databinding.FragmentAttendanceListBinding;
import com.antonioteca.cc42.factory.UserViewModelFactory;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.network.FirebaseDataBaseInstance;
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.repository.UserRepository;
import com.antonioteca.cc42.utility.AttendanceListAdapter;
import com.antonioteca.cc42.utility.EndlessScrollListener;
import com.antonioteca.cc42.utility.Loading;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.SharedViewModel;
import com.antonioteca.cc42.viewmodel.UserViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ScanOptions;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import java.util.List;

public class AttendanceListFragment extends Fragment {

    private Loading l;
    private Long eventId;
    private Context context;
    private Integer cameraId;
    private Activity activity;
    private String resultQrCode;
    private String colorCoalition;
    private View inflatedViewStub;
    private BeepManager beepManager;
    private ScanOptions scanOptions;
    private UserViewModel userViewModel;
    private SharedViewModel sharedViewModel;
    private FirebaseDatabase firebaseDatabase;
    private ProgressBar progressBarMarkAttendance;
    private FragmentAttendanceListBinding binding;
    private DecoratedBarcodeView decoratedBarcodeView;
    private AttendanceListAdapter attendanceListAdapter;

    final long DOUBLE_CLICK_TIME_DELTA = 300; // Tempo máximo entre cliques (em milisegundos)
    final long[] lastClickTime = {0};
    final boolean[] isFlashLightOn = {false};

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result)
                    openCamera(this.cameraId);
                else
                    Util.showAlertDialogBuild(getString(R.string.err), getString(R.string.msg_permis_camera_denied), context, null);
            });

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText().equals(resultQrCode)) {
                resultQrCode = null;
            } else {
                Util.startVibration(context);
                beepManager.playBeepSoundAndVibrate();
                if (result.getText().startsWith("cc42user")) {
                    resultQrCode = result.getText();
                    Util.setVisibleProgressBar(progressBarMarkAttendance, binding.fabOpenCameraScannerQrCodeBack, sharedViewModel);
                    resultQrCode = result.getText();
                    resultQrCode = resultQrCode.replace("cc42user", "");
                    String[] parts = resultQrCode.split("#", 5);
                    if (parts.length == 5) {
                        DaoEventFirebase.markAttendance(
                                firebaseDatabase,
                                String.valueOf(eventId),
                                parts[0],
                                parts[1],
                                parts[2],
                                parts[3],
                                parts[4],
                                context,
                                getLayoutInflater(),
                                progressBarMarkAttendance,
                                binding.fabOpenCameraScannerQrCodeBack,
                                sharedViewModel
                        );
                    } else
                        Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), getString(R.string.msg_qr_code_invalid), "#FDD835");
                } else
                    Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), getString(R.string.msg_qr_code_invalid), "#FDD835");
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        l = new Loading();
        context = requireContext();
        activity = requireActivity();
        scanOptions = new ScanOptions();
        beepManager = new BeepManager(activity);
        colorCoalition = new Coalition(context).getColor();
        attendanceListAdapter = new AttendanceListAdapter(colorCoalition);
        firebaseDatabase = FirebaseDataBaseInstance.getInstance().database;
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        UserRepository userRepository = new UserRepository(context);
        UserViewModelFactory userViewModelFactory = new UserViewModelFactory(userRepository);
        userViewModel = new ViewModelProvider(this, userViewModelFactory).get(UserViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAttendanceListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventId = AttendanceListFragmentArgs.fromBundle(requireArguments()).getEventId();
        binding.recyclerviewAttendanceList.setHasFixedSize(true);
        binding.recyclerviewAttendanceList.setLayoutManager(new LinearLayoutManager(context));

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            setupVisibility(binding, View.GONE, true, View.GONE, View.VISIBLE);
            userViewModel.getUsersEvent(eventId, l, context);
        });

        scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        scanOptions.setPrompt(getString(R.string.align_camera_qr_code));
        scanOptions.setOrientationLocked(false);
        scanOptions.setCameraId(0);
        scanOptions.setBeepEnabled(true);

        inflatedViewStub = binding.viewStub.inflate();
        inflatedViewStub.setVisibility(View.GONE);

        decoratedBarcodeView = inflatedViewStub.findViewById(R.id.decoratedBarcodeView);

        decoratedBarcodeView.initializeFromIntent(scanOptions.createScanIntent(context));
        decoratedBarcodeView.decodeContinuous(callback);

        progressBarMarkAttendance = binding.progressBarMarkAttendance;
        if (colorCoalition != null)
            progressBarMarkAttendance.setIndeterminateTintList(ColorStateList.valueOf(Color.parseColor(colorCoalition)));

        binding.fabOpenCameraScannerQrCodeBack.setOnClickListener(v -> openCameraScannerQrCodeEvent(0));
        binding.fabOpenCameraScannerQrCodeFront.setOnClickListener(v -> openCameraScannerQrCodeEvent(1));

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

        userViewModel.getUsersEventLiveData(eventId, l, progressBarMarkAttendance, context).observe(getViewLifecycleOwner(), users -> {
            if (users.get(0) != null) {
                setupVisibility(binding, View.GONE, false, View.GONE, View.VISIBLE);
                attendanceListAdapter.updateUserList(users, context);
                binding.recyclerviewAttendanceList.setAdapter(attendanceListAdapter);
            } else
                setupVisibility(binding, View.GONE, false, View.VISIBLE, View.GONE);
        });

        userViewModel.getHttpSatusEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                HttpStatus httpStatus = event.getContentIfNotHandled();
                setupVisibility(binding, View.GONE, false, View.VISIBLE, View.GONE);
                Util.showAlertDialogBuild(String.valueOf(httpStatus.getCode()), httpStatus.getDescription(), context, () -> {
                    setupVisibility(binding, View.VISIBLE, false, View.GONE, View.GONE);
                    userViewModel.getUsersEvent(eventId, l, context);
                });
            }
        });

        userViewModel.getHttpExceptionEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                HttpException httpException = event.getContentIfNotHandled();
                setupVisibility(binding, View.GONE, false, View.VISIBLE, View.GONE);
                Util.showAlertDialogBuild(String.valueOf(httpException.getCode()), httpException.getDescription(), context, () -> {
                    setupVisibility(binding, View.VISIBLE, false, View.GONE, View.GONE);
                    userViewModel.getUsersEvent(eventId, l, context);
                });
            }
        });

        // ScrollListener para detectar quando carregar mais dados
        binding.recyclerviewAttendanceList.addOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore() {
                if (!l.isLoading && l.hasNextPage) {
                    Toast.makeText(context, "Carregando mais dados...", Toast.LENGTH_LONG).show();
                    userViewModel.getUsersEvent(eventId, l, context);  // Carregar mais usuários
                }
            }
        });
        sharedViewModel.disabledRecyclerView().observe(getViewLifecycleOwner(), disabled -> binding.recyclerviewAttendanceList.setOnTouchListener((v, event) -> disabled));
    }

    private void openCameraScannerQrCodeEvent(int cameraId) {
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
        decoratedBarcodeView.pause();
        decoratedBarcodeView.getBarcodeView().setCameraSettings(createCameraSettings(cameraId));
        decoratedBarcodeView.resume();
        inflatedViewStub.setVisibility(View.VISIBLE);
    }

    private CameraSettings createCameraSettings(int cameraId) {
        CameraSettings cameraSettings = new CameraSettings();
        cameraSettings.setRequestedCameraId(cameraId);
        return cameraSettings;
    }

    private void closeCamera() {
        inflatedViewStub.setVisibility(View.GONE);
        if (decoratedBarcodeView.isShown()) {
            if (isFlashLightOn[0]) {
                isFlashLightOn[0] = false;
                decoratedBarcodeView.setTorchOff();
            }
            decoratedBarcodeView.pause();
        }
    }

    private void setupVisibility(FragmentAttendanceListBinding binding, int viewP, boolean refreshing, int viewT, int viewR) {
        binding.progressBarMarkAttendance.setVisibility(viewP);
        binding.swipeRefreshLayout.setRefreshing(refreshing);
        binding.textViewEmptyData.setVisibility(viewT);
        binding.recyclerviewAttendanceList.setVisibility(viewR);
    }

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