public static File mergePdfs(Context context, List<File> pdfFiles, String outputFileName) throws IOException {
    File mergedFile = new File(context.getExternalFilesDir(null), outputFileName);
    PdfDocument pdfDocument = new PdfDocument(new PdfWriter(mergedFile));
    PdfMerger merger = new PdfMerger(pdfDocument);

    for (File file : pdfFiles) {
        PdfDocument srcDoc = new PdfDocument(new PdfReader(file));
        merger.merge(srcDoc, 1, srcDoc.getNumberOfPages());
        srcDoc.close(); // Libera a memória
    }

    pdfDocument.close(); // Salva o documento final
    return mergedFile;
}