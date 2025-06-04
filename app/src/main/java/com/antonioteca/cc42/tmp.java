DecoratedBarcodeView barcodeView = findViewById(R.id.decoratedBarcodeView);

final int minHeight = (int) TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics()
);
final int maxHeight = (int) TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, 600, getResources().getDisplayMetrics()
);

barcodeView.setOnTouchListener(new View.OnTouchListener() {
    float startY;
    boolean isDragging = false;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = event.getRawY();
                isDragging = true;
                return true;

            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    float currentY = event.getRawY();
                    int deltaY = (int) (currentY - startY);
                    ViewGroup.LayoutParams params = barcodeView.getLayoutParams();

                    int newHeight = params.height - deltaY;
                    newHeight = Math.max(minHeight, Math.min(maxHeight, newHeight));

                    params.height = newHeight;
                    barcodeView.setLayoutParams(params);
                    startY = currentY;
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;

                // Decide para onde animar: min ou max
                int currentHeight = barcodeView.getLayoutParams().height;
                int targetHeight = (currentHeight < (minHeight + maxHeight) / 2) ? minHeight : maxHeight;

                animateHeight(barcodeView, currentHeight, targetHeight);
                return true;
        }
        return false;
    }
});

// Método para animar a altura
private void animateHeight(final View view, int from, int to) {
    ValueAnimator animator = ValueAnimator.ofInt(from, to);
    animator.setDuration(300); // duração da animação em ms
    animator.addUpdateListener(animation -> {
        int value = (Integer) animation.getAnimatedValue();
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = value;
        view.setLayoutParams(params);
    });
    animator.start();
}