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
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.dao.daofarebase.DaoSusbscriptionFirebase;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ScanOptions;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import java.io.File;
import java.util.Arrays;
import java.util.List;
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
    private Integer cameraId;
    private Activity activity;
    private String colorCoalition;
    private View inflatedViewStub;
    private BeepManager beepManager;
    private ScanOptions scanOptions;
    private MenuProvider menuProvider;
    private UserViewModel userViewModel;
    private LayoutInflater layoutInflater;
    private SharedViewModel sharedViewModel;
    private FirebaseDatabase firebaseDatabase;
    private ProgressBar progressBarSubscription;
    private FragmentSubscriptionListBinding binding;
    private DecoratedBarcodeView decoratedBarcodeView;
    private SubscriptionListAdapter subscriptionListAdapter;

    private int numberUserSubscription = 0;
    private int numberUserUnsubscription = 0;

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
                        // String urlImageUser = subscriptionListAdapter.containsUser(Long.parseLong(partsQrCode[0]));
                        // if (urlImageUser != null) {
                        Util.setVisibleProgressBar(progressBarSubscription, sharedViewModel);
                        DaoSusbscriptionFirebase.subscription(
                                firebaseDatabase,
                                null,
                                String.valueOf(meal.getId()),
                                null,
                                partsQrCode[0], /* id */
                                partsQrCode[1], /* login */
                                partsQrCode[2], /* displayName */
                                String.valueOf(cursusId),
                                partsQrCode[4], /* campusId */
                                partsQrCode[5], /* urlImageUser */
                                context,
                                layoutInflater,
                                progressBarSubscription,
                                sharedViewModel,
                                () -> decoratedBarcodeView.resume()
                        );
                        // } else
                        //    Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), partsQrCode[2] + "\n" + getString(R.string.msg_user_not_fount_list), "#FDD835", null, () -> decoratedBarcodeView.resume());
                    } else
                        Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), getString(R.string.msg_qr_code_invalid), "#FDD835", null, () -> decoratedBarcodeView.resume());
                } else
                    Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), getString(R.string.msg_qr_code_invalid), "#FDD835", null, () -> decoratedBarcodeView.resume());
            }
        }
    };

    public void nfcResult(@NonNull String QRCode) {
        beepManager.playBeepSoundAndVibrate();
        if (QRCode.isEmpty()) {
            Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), getString(R.string.not_found_text_pass), "#FDD835", null,
                    () -> NFCUtils.startReaderNFC(nfcAdapter, activity, pendingIntent, intentFiltersArray, techListsArray)
            );
        } else {
            String result = AESUtil.decrypt(QRCode);
            if (result != null && result.startsWith("cc42user")) {
                String resultQrCode = result.replace("cc42user", "");
                String[] partsQrCode = resultQrCode.split("#", 6);
                if (partsQrCode.length == 6) {
                    Util.setVisibleProgressBar(progressBarSubscription, sharedViewModel);
                    DaoSusbscriptionFirebase.subscription(
                            firebaseDatabase,
                            null,
                            String.valueOf(meal.getId()),
                            null,
                            partsQrCode[0], /* id */
                            partsQrCode[1], /* login */
                            partsQrCode[2], /* displayName */
                            String.valueOf(cursusId),
                            partsQrCode[4], /* campusId */
                            partsQrCode[5], /* urlImageUser */
                            context,
                            layoutInflater,
                            progressBarSubscription,
                            sharedViewModel,
                            () -> NFCUtils.startReaderNFC(nfcAdapter, activity, pendingIntent, intentFiltersArray, techListsArray)

                    );
                } else
                    Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), getString(R.string.not_found_text_pass), "#FDD835", null,
                            () -> NFCUtils.startReaderNFC(nfcAdapter, activity, pendingIntent, intentFiltersArray, techListsArray)
                    );
            } else
                Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), getString(R.string.not_found_text_pass), "#FDD835", null,
                        () -> NFCUtils.startReaderNFC(nfcAdapter, activity, pendingIntent, intentFiltersArray, techListsArray)
                );
        }
    }

    private void activityResultContractsViewer(Boolean result) {
        if (result) {
            List<User> userList = subscriptionListAdapter.getUserList();
            if (userList.isEmpty())
                Util.showAlertDialogBuild(getString(R.string.list_print), getString(R.string.msg_subscription_list_empty), context, null);
            else
                printAndShareSubscriptionsList(userList, true, R.string.list_print);
        } else
            Util.showAlertDialogBuild(getString(R.string.permission), getString(R.string.whithout_permission_cannot_print), context, null);
    }

    private void activityResultContractsSharer(Boolean result) {
        if (result) {
            List<User> userList = subscriptionListAdapter.getUserList();
            if (userList.isEmpty())
                Util.showAlertDialogBuild(getString(R.string.list_share), getString(R.string.msg_subscription_list_empty), context, null);
            else
                printAndShareSubscriptionsList(userList, false, R.string.list_share);
        } else
            Util.showAlertDialogBuild(getString(R.string.permission), getString(R.string.whithout_permission_cannot_share), context, null);
    }

    private final ActivityResultLauncher<Intent> requestIntentPermissionLauncherViewer = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> activityResultContractsViewer(result.getResultCode() == Activity.RESULT_OK));

    private final ActivityResultLauncher<String> requestPermissionLauncherViewer = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), this::activityResultContractsViewer);

    private final ActivityResultLauncher<Intent> requestIntentPermissionLauncherSharer = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> activityResultContractsSharer(result.getResultCode() == Activity.RESULT_OK));

    private final ActivityResultLauncher<String> requestPermissionLauncherSharer = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), this::activityResultContractsSharer);

    private List<String> userIds;

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
        l = new Loading();
        context = requireContext();
        user = new User(context);
        activity = requireActivity();
        scanOptions = new ScanOptions();
        layoutInflater = getLayoutInflater();
        beepManager = new BeepManager(activity);
        colorCoalition = new Coalition(context).getColor();
        subscriptionListAdapter = new SubscriptionListAdapter();
        firebaseDatabase = FirebaseDataBaseInstance.getInstance().database;
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        UserRepository userRepository = new UserRepository(context);
        UserViewModelFactory userViewModelFactory = new UserViewModelFactory(userRepository);
        userViewModel = new ViewModelProvider(this, userViewModelFactory).get(UserViewModel.class);
        nfcAdapter = NfcAdapter.getDefaultAdapter(context); // Obter o adaptador NFC padrão
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* habilitado por padrão */) {
            @Override
            public void handleOnBackPressed() {

                if (inflatedViewStub != null && inflatedViewStub.getVisibility() == View.VISIBLE) {
                    closeCamera();
                    return;
                }

                if (isEnabled()) { // Verifica se ainda está habilitado
                    setEnabled(false); // Desabilita este callback para evitar um loop se chamado recursivamente
                    requireActivity().getOnBackPressedDispatcher().onBackPressed(); // Propaga o evento
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
                Object[] objects = NFCUtils.startNFC(
                        nfcAdapter,
                        activity
                );
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
            if (subscriptionListAdapter.isMarkAttendance || subscriptionListAdapter.getItemCount() < 0) {
                setupVisibility(binding, View.GONE, true, View.GONE, View.VISIBLE);
                l.currentPage = 1;
                activeScrollListener();
                subscriptionListAdapter.clean();
                userViewModel.getUserIdsSubscriptionList(firebaseDatabase, String.valueOf(user.getCampusId()), String.valueOf(cursusId), String.valueOf(meal.getId()), context, layoutInflater);
            } else
                binding.swipeRefreshLayout.setRefreshing(false);
        });

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

        progressBarSubscription = binding.progressBarSubscription;
        if (colorCoalition != null) {
            ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(colorCoalition));
            binding.fabOpenReaderNFC.setBackgroundTintList(colorStateList);
            binding.fabOpenCameraScannerQrCodeBack.setBackgroundTintList(colorStateList);
            binding.fabOpenCameraScannerQrCodeFront.setBackgroundTintList(colorStateList);
            binding.progressindicator.setIndicatorColor(Color.parseColor(colorCoalition));
            progressBarSubscription.setIndeterminateTintList(ColorStateList.valueOf(Color.parseColor(colorCoalition)));
        }

        binding.fabOpenReaderNFC.setOnClickListener(v -> NFCUtils.startReaderNFC(
                nfcAdapter,
                activity,
                context,
                pendingIntent,
                intentFiltersArray,
                techListsArray
        ));

        binding.fabOpenCameraScannerQrCodeBack.setOnClickListener(v -> openCameraScannerQrCodeSubscriptio(0));
        binding.fabOpenCameraScannerQrCodeFront.setOnClickListener(v -> openCameraScannerQrCodeSubscriptio(1));
        binding.fabOpenCameraScannerQrCodeClose.setOnClickListener(v -> closeCamera());

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

        binding.progressBarSubscription.setVisibility(View.VISIBLE);
        userViewModel.getUserIdsSubscriptionList(firebaseDatabase, String.valueOf(user.getCampusId()), String.valueOf(cursusId), String.valueOf(meal.getId()), context, layoutInflater);
        userViewModel.getUserIdsList().observe(getViewLifecycleOwner(), userIds -> {
            this.userIds = userIds;
            userViewModel.getUsersSubscription(cursusId, l, context);
        });

        userViewModel.getUsersSubscriptionLiveData().observe(getViewLifecycleOwner(), users -> {
            if (!users.isEmpty() && users.get(0) != null) {
                subscriptionListAdapter.updateUserList(users, context);
                binding.recyclerviewSubscriptionList.setAdapter(subscriptionListAdapter);
                if (userIds != null && !userIds.isEmpty() && userIds.get(0) != null) {
                    subscriptionListAdapter.updateSubscriptionUser(userIds);
                    setNumberUserChip();
                }
                subscriptionListAdapter.isMarkAttendance = false;
            } else
                setupVisibility(binding, View.GONE, false, View.VISIBLE, View.GONE);
        });

        sharedViewModel.getUserIdLiveData().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                Long userId = event.getContentIfNotHandled();
                subscriptionListAdapter.updateSubscriptionUserSingle(userId);
                binding.chipSubscription.setText(String.valueOf(++numberUserSubscription));
                binding.chipUnsubscription.setText(String.valueOf(Math.max(--numberUserUnsubscription, 0)));
            }
        });

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
                        userViewModel.getUsersSubscription(cursusId, l, context);
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
                        userViewModel.getUsersSubscription(cursusId, l, context);
                    });
            }
        });

        sharedViewModel.getUserFaceIdLiveData().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                String userId = event.getContentIfNotHandled();
                if (userId != null) {
                    beepManager.playBeepSoundAndVibrate();
                    String[] userData = subscriptionListAdapter.containsUserFaceID(Long.parseLong(userId));
                    String displayName = userData[0];
                    String urlImageUser = userData[1];
                    if (urlImageUser != null && !displayName.isEmpty()) {
                        Util.setVisibleProgressBar(progressBarSubscription, sharedViewModel);
                        DaoSusbscriptionFirebase.subscription(
                                firebaseDatabase,
                                null,
                                String.valueOf(meal.getId()),
                                null,
                                userId, /* id */
                                "", /* login */
                                displayName, /* displayName */
                                String.valueOf(cursusId), /* cursusId */
                                String.valueOf(campusId), /* campusId */
                                urlImageUser,
                                context,
                                layoutInflater,
                                progressBarSubscription,
                                sharedViewModel,
                                () -> sharedViewModel.setUserFaceIdContinueCaptureLiveData(true)
                        );
                    } else
                        Util.showAlertDialogMessage(context, getLayoutInflater(), context.getString(R.string.warning), displayName + "\n" + getString(R.string.msg_user_not_fount_list), "#FDD835", urlImageUser, () -> sharedViewModel.setUserFaceIdContinueCaptureLiveData(true));
                }
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

                MenuItem menuItemSubscription = menu.findItem(R.id.action_one_list);
                menuItemSubscription.setTitle(context.getString(R.string.with_signs));
                MenuItem menuItemUnSubscription = menu.findItem(R.id.action_two_list);
                menuItemUnSubscription.setTitle(context.getString(R.string.without_signs));
                MenuItem menuItemAll = menu.findItem(R.id.action_three_list);
                menuItemAll.setTitle(context.getString(R.string.all));
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
                else if (itemId == R.id.action_register_face_id_camera_front) {
                    SubscriptionListFragmentDirections.ActionSubscriptionListFragmentToFaceRecognitionFragment actionSubscriptionListFragmentToFaceRecognitionFragment
                            = SubscriptionListFragmentDirections.actionSubscriptionListFragmentToFaceRecognitionFragment(false, 1, String.valueOf(user.getCampusId()), String.valueOf(cursusId));
                    Navigation.findNavController(view).navigate(actionSubscriptionListFragmentToFaceRecognitionFragment);
                } else if (itemId == R.id.action_register_face_id_camera_back) {
                    SubscriptionListFragmentDirections.ActionSubscriptionListFragmentToFaceRecognitionFragment actionSubscriptionListFragmentToFaceRecognitionFragment
                            = SubscriptionListFragmentDirections.actionSubscriptionListFragmentToFaceRecognitionFragment(false, 0, String.valueOf(user.getCampusId()), String.valueOf(cursusId));
                    Navigation.findNavController(view).navigate(actionSubscriptionListFragmentToFaceRecognitionFragment);
                } else if (itemId == R.id.action_list_print) {
                    boolean isExternalStorageManager = Util.launchPermissionDocument(
                            context,
                            requestIntentPermissionLauncherViewer,
                            requestPermissionLauncherViewer,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (isExternalStorageManager) {
                        List<User> userList = subscriptionListAdapter.getUserList();
                        if (userList.isEmpty())
                            Util.showAlertDialogBuild(getString(R.string.list_print), getString(R.string.msg_subscription_list_empty), context, null);
                        else
                            printAndShareSubscriptionsList(userList, true, R.string.list_print);
                    }
                } else if (itemId == R.id.action_list_share) {
                    boolean isExternalStorageManager = Util.launchPermissionDocument(
                            context,
                            requestIntentPermissionLauncherSharer,
                            requestPermissionLauncherSharer,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (isExternalStorageManager) {
                        List<User> userList = subscriptionListAdapter.getUserList();
                        if (userList.isEmpty())
                            Util.showAlertDialogBuild(getString(R.string.list_share), getString(R.string.msg_subscription_list_empty), context, null);
                        else
                            printAndShareSubscriptionsList(userList, false, R.string.list_share);
                    }
                }
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        };
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner());
//        sharedViewModel.disabledRecyclerView().observe(getViewLifecycleOwner(), disabled -> {
//            binding.fabOpenCameraScannerQrCodeBack.setVisibility(disabled ? View.INVISIBLE : View.VISIBLE);
//            binding.fabOpenCameraScannerQrCodeFront.setVisibility(disabled ? View.INVISIBLE : View.VISIBLE);
//            binding.recyclerviewSubscriptionList.setOnTouchListener((v, event) -> disabled);
//        });
    }

    private void printAndShareSubscriptionsList(List<User> userList, boolean isPrint, int title) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setItems(R.array.array_subscriptions_list_qr_code_options, (dialog, selected) -> {
                    if (selected == 0) {
                        binding.progressindicator.setVisibility(View.VISIBLE);
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            File filePdf = PdfCreator.createPdfSubscriptionList(context, requireActivity(), meal, numberUserUnsubscription, numberUserSubscription, subscriptionListAdapter.getUserList(), binding.progressindicator, binding.textViewTotal);
                            if (filePdf != null) {
                                if (isPrint)
                                    PdfViewer.openPdf(context, filePdf, "application/pdf", getString(R.string.msg_no_pdf_viewing_applications_were_found));
                                else
                                    PdfSharer.sharePdf(context, filePdf, "application/pdf", context.getString(R.string.list_share));
                            } else
                                activity.runOnUiThread(() -> Util.showAlertDialogBuild(context.getString(R.string.err), context.getString(R.string.pdf_not_created), context, null));
                            requireActivity().runOnUiThread(() -> {
                                binding.textViewTotal.setText("");
                                binding.progressindicator.setProgress(0);
                                binding.progressindicator.setVisibility(View.GONE);
                            });
                        });
                    } else {
                        binding.progressindicator.setVisibility(View.VISIBLE);
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
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
                            requireActivity().runOnUiThread(() -> {
                                binding.textViewTotal.setText("");
                                binding.progressindicator.setProgress(0);
                                binding.progressindicator.setVisibility(View.GONE);
                            });
                        });
                    }
                }).setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss())
                .show();
    }

    RecyclerView.OnScrollListener onScrollListener = new EndlessScrollListener() {
        @Override
        public void onLoadMore() {
            if (!l.isLoading && l.hasNextPage) {
//                Toast.makeText(context, R.string.msg_loading_more_data, Toast.LENGTH_LONG).show();
                userViewModel.getUsersSubscription(cursusId, l, context);
            } else {
//                Toast.makeText(context, R.string.synchronization, Toast.LENGTH_LONG).show();
                setupVisibility(binding, View.GONE, false, View.GONE, View.VISIBLE);
                desactiveScrollListener();
            }
        }
    };

    private void setNumberUserChip() {
        int[] numberUser = subscriptionListAdapter.getNumberUser();
        this.numberUserSubscription = numberUser[0];
        this.numberUserUnsubscription = numberUser[1];
        binding.chipSubscription.setText(String.valueOf(numberUserSubscription));
        binding.chipUnsubscription.setText(String.valueOf(numberUserUnsubscription));
    }

    private void activeScrollListener() {
        //sharedViewModel.setDisabledRecyclerView(true);
        binding.recyclerviewSubscriptionList.addOnScrollListener(onScrollListener);
    }

    private void desactiveScrollListener() {
        //sharedViewModel.setDisabledRecyclerView(false);
        binding.recyclerviewSubscriptionList.removeOnScrollListener(onScrollListener);
    }

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
        binding.fabOpenReaderNFC.setVisibility(View.GONE);
        binding.fabOpenCameraScannerQrCodeBack.setVisibility(View.GONE);
        binding.fabOpenCameraScannerQrCodeFront.setVisibility(View.GONE);
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
        binding.fabOpenReaderNFC.setVisibility(View.VISIBLE);
        binding.fabOpenCameraScannerQrCodeClose.setVisibility(View.GONE);
        binding.fabOpenCameraScannerQrCodeBack.setVisibility(View.VISIBLE);
        binding.fabOpenCameraScannerQrCodeFront.setVisibility(View.VISIBLE);
    }

    private void setupVisibility(@NonNull FragmentSubscriptionListBinding binding, int viewP,
                                 boolean refreshing, int viewT, int viewR) {
        binding.progressBarSubscription.setVisibility(viewP);
        binding.swipeRefreshLayout.setRefreshing(refreshing);
        binding.textViewEmptyData.setVisibility(viewT);
        binding.recyclerviewSubscriptionList.setVisibility(viewR);
    }

    // Método para processar um Intent NFC recebido
    public void resolveIntent(@NonNull Intent intent) {
        // Verifica se a ação do Intent corresponde a uma descoberta de tag NFC (NDEF, TECH ou TAG)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {

            // Obtém as mensagens NDEF brutas do Intent
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            // Verifica se existem mensagens NDEF
            if (rawMsgs != null) {
                // Cria um StringBuilder para acumular o texto dos registros NDEF
                StringBuilder resultado = new StringBuilder();

                // Itera sobre cada mensagem NDEF bruta
                for (Parcelable rawMsg : rawMsgs) {
                    // Converte a mensagem bruta para um objeto NdefMessage
                    NdefMessage msg = (NdefMessage) rawMsg;
                    // Itera sobre cada registro NDEF dentro da mensagem
                    for (NdefRecord record : msg.getRecords()) {
                        // Verifica se o registro é do tipo TNF_WELL_KNOWN e se o tipo é RTD_TEXT (texto)
                        if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                                Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {

                            try {
                                // Obtém o payload (dados) do registro
                                final String texto = NFCUtils.getString(record);

                                // Adiciona o texto extraído ao resultado, seguido por uma nova linha
                                resultado.append(texto);

                            } catch (Exception e) {
                                NFCUtils.showAlertDialogBuild(context.getString(R.string.err), getString(R.string.erro_process_text_pass), context, () ->
                                        NFCUtils.startReaderNFC(nfcAdapter, activity, pendingIntent, intentFiltersArray, techListsArray)
                                );
                                return;
                            }
                        }
                    }
                }

                // Verifica se algum texto foi extraído
                if (resultado.length() > 0) {
                    nfcResult(resultado.toString().trim());
                } else {
                    NFCUtils.showAlertDialogBuild(getString(R.string.without_text), getString(R.string.not_found_text_pass), context, () ->
                            NFCUtils.startReaderNFC(nfcAdapter, activity, pendingIntent, intentFiltersArray, techListsArray)
                    );
                }
            } else {
                NFCUtils.showAlertDialogBuild(getString(R.string.without_data), getString(R.string.without_message_NDEF), context, () ->
                        NFCUtils.startReaderNFC(nfcAdapter, activity, pendingIntent, intentFiltersArray, techListsArray)
                );
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (decoratedBarcodeView.isShown())
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
        requireActivity().removeMenuProvider(menuProvider);
    }
}