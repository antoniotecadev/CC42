package com.antonioteca.cc42.utility;

import android.content.Context;
import android.os.Environment;

import com.antonioteca.cc42.R;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.io.FileOutputStream;

public class PdfCreator {

    private static File createFolder(Context context, String folderName) {
        File folder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), folderName);
        if (!folder.exists()) {
            boolean wasCreated = folder.mkdirs();
            if (!wasCreated) {
                Util.showAlertDialogBuild(context.getString(R.string.err), context.getString(R.string.msg_folder_not_created) + folder.getAbsolutePath(), context, null);
                return null;
            }
        }
        return folder;
    }

    public static File createPdfAttendanceList(Context context, String fileName) {
        File folder = createFolder(context, "AttendanceList");
        if (folder == null)
            return null;
//        Caminho do arquivo PDF
        File file = new File(folder, fileName);
        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.add(new Paragraph("Ola!, este é um exemplo de PDF!"));
            document.add(new Paragraph("Gerado com  iText 7."));
            document.close();
            return file;
        } catch (Exception e) {
            Util.showAlertDialogBuild("PDF", context.getString(R.string.pdf_not_created) + e.getMessage(), context, null);
            return null;
        }
    }
}
