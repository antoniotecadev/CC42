package com.antonioteca.cc42.utility;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Classe para detectar quando rolar até o final
public abstract class EndlessScrollListener extends RecyclerView.OnScrollListener {

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

        // Verifica se chegou ao final da lista
        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
            onLoadMore();
        }
    }

    public abstract void onLoadMore();
}
