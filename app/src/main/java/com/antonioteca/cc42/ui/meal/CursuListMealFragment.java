package com.antonioteca.cc42.ui.meal;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.databinding.FragmentCursuListMealBinding;
import com.antonioteca.cc42.factory.CursuViewModelFactory;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.network.HttpException;
import com.antonioteca.cc42.network.HttpStatus;
import com.antonioteca.cc42.repository.CursuRepository;
import com.antonioteca.cc42.utility.RateLimiter;
import com.antonioteca.cc42.utility.Util;
import com.antonioteca.cc42.viewmodel.CursuViewModel;

public class CursuListMealFragment extends Fragment {

    private int color;
    private User user;
    private int cursusId;
    private boolean isStaff;
    private Context context;
    private MenuProvider menuProvider;
    private CursuAdapter cursuAdapter;
    private String cursusIdStudentOrStaff;
    private CursuViewModel cursuViewModel;
    private FragmentCursuListMealBinding binding;
    private RateLimiter refreshRateLimiter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        CursuRepository cursuRepository = new CursuRepository(context);
        CursuViewModelFactory cursuViewModelFactory = new CursuViewModelFactory(cursuRepository);
        cursuViewModel = new ViewModelProvider(this, cursuViewModelFactory).get(CursuViewModel.class);
        user = new User(context);
        isStaff = user.isStaff();
        cursusId = user.getCursusId();
        user.coalition = new Coalition(context);
        cursusIdStudentOrStaff = isStaff ? "21, 66, 9, 3" : String.valueOf(cursusId);
        refreshRateLimiter = new RateLimiter(10000);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCursuListMealBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String colorCoalition = user.coalition.getColor();
        if (colorCoalition != null) {
            color = Color.parseColor(colorCoalition);
            ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(colorCoalition));
            binding.progressBarCursus.setIndeterminateTintList(colorStateList);
        }

        binding.recyclerviewCursuList.setHasFixedSize(true);
        binding.recyclerviewCursuList.setLayoutManager(new LinearLayoutManager(context));

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            boolean executed = refreshRateLimiter.executeWithLimit(() -> {
                setupVisibility(binding, View.INVISIBLE, true, View.INVISIBLE, View.VISIBLE);
                cursuViewModel.getCursus(context, cursusIdStudentOrStaff);
            }, message -> {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            });
            if (!executed) {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });

        final boolean[] isClick = {false};
        binding.floatingActionButtonSearchCursuIdUserDown.setOnClickListener(v -> {
            if (!isClick[0]) {
                isClick[0] = true;
                binding.floatingActionButtonSearchCursuIdUserDown.setColorFilter(ContextCompat.getColor(context, R.color.green));
                cursuAdapter.filter(String.valueOf(cursusId), isStaff);
            } else {
                isClick[0] = false;
                binding.floatingActionButtonSearchCursuIdUserDown.setColorFilter(ContextCompat.getColor(context, R.color.black));
                cursuAdapter.filter("", isStaff);
            }
        });

        cursuViewModel.getCursustLiveData(context, cursusIdStudentOrStaff, binding.progressBarCursus).observe(getViewLifecycleOwner(), cursus -> {
            if (!cursus.isEmpty() && cursus.get(0) != null) {
                setupVisibility(binding, View.INVISIBLE, false, View.INVISIBLE, View.VISIBLE);
                cursuAdapter = new CursuAdapter(context, cursus, color, cursusId);
                binding.recyclerviewCursuList.setAdapter(cursuAdapter);
                binding.chipNumberCursus.setText(String.valueOf(cursuAdapter.getItemCount()));
            } else
                setupVisibility(binding, View.INVISIBLE, false, View.VISIBLE, View.INVISIBLE);
        });

        cursuViewModel.getHttpSatusCursu().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                HttpStatus httpStatus = event.getContentIfNotHandled();
                setupVisibility(binding, View.INVISIBLE, false, View.VISIBLE, View.INVISIBLE);
                if (httpStatus != null) {
                    Util.showAlertDialogBuild(String.valueOf(httpStatus.getCode()), httpStatus.getDescription(), context, () -> {
                        setupVisibility(binding, View.VISIBLE, false, View.INVISIBLE, View.INVISIBLE);
                        cursuViewModel.getCursus(context, cursusIdStudentOrStaff);
                    });
                }
            }
        });

        cursuViewModel.getHttpExceptionCursu().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                HttpException httpException = event.getContentIfNotHandled();
                setupVisibility(binding, View.INVISIBLE, false, View.VISIBLE, View.INVISIBLE);
                if (httpException != null) {
                    Util.showAlertDialogBuild(String.valueOf(httpException.getCode()), httpException.getDescription(), context, () -> {
                        setupVisibility(binding, View.VISIBLE, false, View.INVISIBLE, View.INVISIBLE);
                        cursuViewModel.getCursus(context, cursusIdStudentOrStaff);
                    });
                }
            }
        });

        menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_cursu_list, menu);
                MenuItem menuItem = menu.findItem(R.id.action_search);
                SearchView searchView = (SearchView) menuItem.getActionView();
                searchView.setQueryHint(context.getString(R.string.cursu));
                searchView.onActionViewExpanded();
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        cursuAdapter.filter(query, false);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        cursuAdapter.filter(newText, false);
                        return false;
                    }
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        };
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner());
    }

    private void setupVisibility(FragmentCursuListMealBinding binding, int viewP, boolean refreshing, int viewT, int viewR) {
        binding.progressBarCursus.setVisibility(viewP);
        binding.swipeRefreshLayout.setRefreshing(refreshing);
        binding.textViewNotFoundCursus.setVisibility(viewT);
        binding.recyclerviewCursuList.setVisibility(viewR);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        requireActivity().removeMenuProvider(menuProvider);
    }
}