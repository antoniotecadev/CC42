package com.antonioteca.cc42;

import static com.antonioteca.cc42.utility.Util.setColorCoalition;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.antonioteca.cc42.databinding.ActivityNavigationDrawerBinding;
import com.antonioteca.cc42.model.Coalition;
import com.antonioteca.cc42.model.Token;
import com.antonioteca.cc42.model.User;
import com.antonioteca.cc42.utility.GlideApp;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

public class NavigationDrawerActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityNavigationDrawerBinding binding = ActivityNavigationDrawerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarNavigationDrawer.toolbar);
        binding.appBarNavigationDrawer.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation_drawer);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        // Obter o NavigationView
        // Obter o header view dentro do NavigationView
        View headerView = navigationView.getHeaderView(0);
        User user = new User(this);
        user.coalition = new Coalition(this);

        Toolbar toolbar = binding.appBarNavigationDrawer.toolbar;
        String colorCoalition = user.coalition.getColor();
        setColorCoalition(toolbar, colorCoalition);
        if (colorCoalition != null) {
            ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(colorCoalition));
            binding.appBarNavigationDrawer.fab.setBackgroundTintList(colorStateList);
            navigationView.setItemTextColor(colorStateList);
            navigationView.setItemIconTintList(colorStateList);
        }
        LinearLayout linearLayout = headerView.findViewById(R.id.linearLayoutNavHeaderNavigationDrawer);
        String imageUrlCoalition = user.coalition.getImageUrl();
        if (imageUrlCoalition != null) {
            Glide.with(this)
                    .load(imageUrlCoalition)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            linearLayout.setBackground(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // Definir background placeholder caso a imagem não carregue
                        }
                    });
        }
        ImageView imageViewUser = headerView.findViewById(R.id.imageViewUser);
        TextView textViewFullNameUser = headerView.findViewById(R.id.fullNameUser);
        TextView textViewEmailUser = headerView.findViewById(R.id.emailUser);
        textViewFullNameUser.setText(user.getDisplayName());
        textViewEmailUser.setText(user.getEmail());

        String imageUrl = user.getImage();
        GlideApp.with(this)
                .load(imageUrl)
                .circleCrop() // Recorta a imagem para ser circular
                .placeholder(R.drawable.logo_42) // Imagem de substituição enquanto a imagem carrega
                .error(R.drawable.logo_42) // Imagem a ser mostrada caso ocorra um erro
                .into(imageViewUser);

        this.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_settings_general, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                Activity context = NavigationDrawerActivity.this;
                NavController navController = Navigation.findNavController(context, R.id.nav_host_fragment_content_navigation_drawer);
                if (menuItem.getItemId() == R.id.action_logout)
                    logout(context);
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        }, this);
    }

    private void logout(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(getString(R.string.action_logout))
                .setMessage(getString(R.string.msg_logout))
                .setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss())
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> removeSessionUser(context))
                .show();
    }

    private void removeSessionUser(Context context) {
        new User(context).clear();
        new Token(context).clear();
        redirectToLogin(context);
    }

    private void redirectToLogin(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("fragment_id", R.id.loginFragment_nav);
        startActivity(intent);
        finish();
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings_general, menu);
        return true;
    }*/

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation_drawer);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}