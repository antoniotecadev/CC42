public static List<File> createMultiplePdfQrCodes(Context context, List<User> userList, int campusId, int cursusId, ProgressBar progressBar) {
    final int BLOCK_SIZE = 60;
    List<File> pdfFiles = new ArrayList<>();

    int totalUsers = userList.size();
    int totalPages = (int) Math.ceil((double) totalUsers / BLOCK_SIZE);

    for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
        int start = pageIndex * BLOCK_SIZE;
        int end = Math.min(start + BLOCK_SIZE, totalUsers);
        List<User> subList = userList.subList(start, end);

        // Gere o PDF para este bloco
        File pdfFile = createSinglePdf(context, subList, campusId, cursusId, pageIndex + 1); // index + 1 para começar do 1
        if (pdfFile != null) {
            pdfFiles.add(pdfFile);
        }
    }

    return pdfFiles;
}
public static File createSinglePdf(Context context, List<User> userList, int campusId, int cursusId, int pageNumber) {
    try {
        File file = new File(context.getExternalFilesDir(null), "qrcode_page_" + pageNumber + ".pdf");
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        Table table = new Table(UnitValue.createPercentArray(3)).useAllAvailableWidth();

        int count = 0;
        for (User user : userList) {
            // mesma lógica atual: gerar QR, criar Image, adicionar em célula etc.
            Cell userCell = new Cell().setTextAlignment(TextAlignment.CENTER);

            Paragraph userName = new Paragraph(user.login)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            userCell.add(userName);

            String content = "user=" + user.uid + "#" + user.login + "#" + user.displayName;
            Bitmap qrBitmap = Util.generateQrCodeWithoutLogo(context, content);

            if (qrBitmap != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
                byte[] qrData = stream.toByteArray();
                qrBitmap.recycle();

                Image qrImage = new Image(ImageDataFactory.create(qrData))
                        .setWidth(UnitValue.createPercentValue(90))
                        .setHorizontalAlignment(HorizontalAlignment.CENTER);
                userCell.add(qrImage);
            } else {
                userCell.add(new Paragraph("QR Indisponível").setTextAlignment(TextAlignment.CENTER));
            }

            table.addCell(userCell);
            count++;
        }

        // Preencher células vazias para completar a última linha
        int remaining = 3 - (count % 3);
        if (remaining < 3) {
            for (int i = 0; i < remaining; i++) {
                table.addCell(new Cell().setBorder(Border.NO_BORDER));
            }
        }

        document.add(table);
        document.close();

        return file;

    } catch (Exception e) {
        Log.e("PDF", "Erro ao criar PDF: " + e.getMessage());
        return null;
    }
}
public static File generateMultiplePdfs(Context context, List<User> userList, ProgressBar progressBar) {
    List<File> pdfFiles = new ArrayList<>();
    int totalUsers = userList.size();
    int pdfCount = (int) Math.ceil(totalUsers / 60.0);

    for (int i = 0; i < pdfCount; i++) {
        int start = i * 60;
        int end = Math.min(start + 60, totalUsers);
        List<User> batch = userList.subList(start, end);

        File pdfFile = createSinglePdf(context, batch, i + 1); // Cria PDF para o lote
        pdfFiles.add(pdfFile);

        // Atualiza barra de progresso
        int progress = (int) (((i + 1) / (float) pdfCount) * 100);
        progressBar.setProgress(progress);

        // Notificação (caso esteja usando NotificationManager)
        showNotification(context, "PDF gerado", "PDF " + (i + 1) + " de " + pdfCount + " criado.");
    }

    // Compactar os PDFs em um único ZIP
    File zipFile = new File(context.getExternalFilesDir(null), "qrcodes.zip");
    zipFiles(pdfFiles, zipFile);

    return zipFile;
}

