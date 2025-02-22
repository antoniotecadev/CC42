package com.antonioteca.cc42.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.antonioteca.cc42.R;
import com.antonioteca.cc42.model.Meal;
import com.antonioteca.cc42.model.User;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PdfCreator {

    @Nullable
    private static File createFolder(@NonNull Context context, String folderName) {
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

    @Nullable
    public static File createPdfAttendanceList(Context context, String eventKind, String eventName, String eventDate, int numberUserAbsent, int numberUserPresent, List<User> userList) {
        File folder = createFolder(context, "AttendanceList");
        if (folder == null)
            return null;
//        Caminho do arquivo PDF
        File file = new File(folder, "event_attendance_list.pdf");
        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(20, 20, 20, 20);
            // Evento marca d'agua
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new ImageWatermarkEvent(getImageDataFromDrawable(context, R.drawable.check_cadet_logotipo)));
            // Rodapé
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, event -> {
                PdfDocumentEvent documentEvent = (PdfDocumentEvent) event;
                PdfPage page = documentEvent.getPage();
                PdfCanvas pdfCanvas = new PdfCanvas(page);
                Rectangle pageSize = page.getPageSize(); // Área do rodapé onde o contúdo será renderizado
                Rectangle footerArea = new Rectangle(pageSize.getLeft()/*X inicial*/, pageSize.getBottom()/*Y inicial*/, pageSize.getWidth(), 20);
                Canvas canvas = new Canvas(pdfCanvas, footerArea, true);
                canvas.showTextAligned(new Paragraph(eventName + "\n" + context.getString(R.string.page) + pdf.getPageNumber(page)).setFontSize(6f), pageSize.getWidth() / 2, 10, TextAlignment.CENTER);
                canvas.close();
            });
            Bitmap logoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_42);
            if (logoBitmap != null) { // Converter bitmap para um array de bytes
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                logoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] logoBytes = stream.toByteArray();

                Image image = new Image(ImageDataFactory.create(logoBytes))
                        .setWidth(UnitValue.createPointValue(30)) // Largura da imagem (30%) da página
                        .setHorizontalAlignment(HorizontalAlignment.CENTER); // Centraliza a imagem
                document.add(image);
            } else {
                Util.showAlertDialogBuild("PDF", context.getString(R.string.error_load_logo), context, null);
                return null;
            }
            Color red = new DeviceRgb(244, 67, 54);
            Color green = new DeviceRgb(139, 194, 74);
            Paragraph title = new Paragraph(context.getString(R.string.msg_attendance_list))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setBold();
            document.add(title);
            Paragraph attendanceParagraph = new Paragraph()
                    .add(new Text(context.getString(R.string.text_present) + ": ").setBold())
                    .add(new Text(String.valueOf(numberUserPresent)))
                    .add(new Text(" | "))
                    .add(new Text(context.getString(R.string.text_absent) + ": ").setBold())
                    .add(new Text(String.valueOf(numberUserAbsent)))
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(attendanceParagraph);
            Paragraph dateParagraph = new Paragraph()
                    .add(new Text(context.getString(R.string.date).toUpperCase() + ": ").setBold())
                    .add(new Text(eventDate))
                    .setTextAlignment(TextAlignment.LEFT);
            document.add(dateParagraph);
            Paragraph kindParagraph = new Paragraph()
                    .add(new Text(eventKind.toUpperCase() + ": ").setBold())
                    .add(new Text(eventName))
                    .setTextAlignment(TextAlignment.LEFT);
            document.add(kindParagraph);
            Table table = new Table(UnitValue.createPercentArray(new float[]{10, 50, 25, 15}))
                    .useAllAvailableWidth();
            table.setMarginTop(20);
            table.addHeaderCell(new Paragraph(context.getString(R.string.num)).setBold());
            table.addHeaderCell(new Paragraph(context.getString(R.string.full_name)).setBold());
            table.addHeaderCell(new Paragraph(context.getString(R.string.login)).setBold());
            table.addHeaderCell(new Paragraph(context.getString(R.string.attendance)).setBold());
            for (int i = 0; i < userList.size(); i++) {
                User user = userList.get(i);
                table.addCell(new Paragraph(String.valueOf(i + 1)));
                table.addCell(new Paragraph(user.displayName));
                table.addCell(new Paragraph(user.login));
                if (user.isPresent() != null && user.isPresent()) {
                    table.addCell(new Paragraph(context.getString(R.string.text_present)).setFontColor(green, 100));
                } else if (user.isPresent() != null && !user.isPresent()) {
                    table.addCell(new Paragraph(context.getString(R.string.text_absent)).setFontColor(red, 100));
                }
            }
            document.add(table);
            document.close();
            return file;
        } catch (Exception e) {
            Util.showAlertDialogBuild("PDF", context.getString(R.string.pdf_not_created) + e.getMessage(), context, null);
            return null;
        }
    }

    private static ImageData getImageDataFromDrawable(Context context, int drawebleId) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), drawebleId);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return ImageDataFactory.create(byteArray);
    }

    @Nullable
    public static File createPdfSubscriptionList(Context context, Meal meal, int numberUserUnsubscription, int numberUserSubscription, List<User> userList) {
        File folder = createFolder(context, "MealSubscriptionList");
        if (folder == null)
            return null;
//        Caminho do arquivo PDF
        File file = new File(folder, "meal_subscription_list.pdf");
        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(20, 20, 20, 20);
            // Evento marca d'agua
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new ImageWatermarkEvent(getImageDataFromDrawable(context, R.drawable.check_cadet_logotipo)));
            // Rodapé
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, event -> {
                PdfDocumentEvent documentEvent = (PdfDocumentEvent) event;
                PdfPage page = documentEvent.getPage();
                PdfCanvas pdfCanvas = new PdfCanvas(page);
                Rectangle pageSize = page.getPageSize(); // Área do rodapé onde o contúdo será renderizado
                Rectangle footerArea = new Rectangle(pageSize.getLeft()/*X inicial*/, pageSize.getBottom()/*Y inicial*/, pageSize.getWidth(), 20);
                Canvas canvas = new Canvas(pdfCanvas, footerArea, true);
                canvas.showTextAligned(new Paragraph(meal.getName() + "\n" + context.getString(R.string.page) + pdf.getPageNumber(page)).setFontSize(6f), pageSize.getWidth() / 2, 10, TextAlignment.CENTER);
                canvas.close();
            });
            Bitmap logoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_42);
            if (logoBitmap != null) { // Converter bitmap para um array de bytes
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                logoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] logoBytes = stream.toByteArray();

                Image image = new Image(ImageDataFactory.create(logoBytes))
                        .setWidth(UnitValue.createPointValue(30)) // Largura da imagem (30%) da página
                        .setHorizontalAlignment(HorizontalAlignment.CENTER); // Centraliza a imagem
                document.add(image);
            } else {
                Util.showAlertDialogBuild("PDF", context.getString(R.string.error_load_logo), context, null);
                return null;
            }
            Color red = new DeviceRgb(244, 67, 54);
            Color green = new DeviceRgb(139, 194, 74);
            Paragraph title = new Paragraph(meal.getType().toUpperCase())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setBold();
            document.add(title);
            Paragraph attendanceParagraph = new Paragraph()
                    .add(new Text(context.getString(R.string.text_signed) + ": ").setBold())
                    .add(new Text(String.valueOf(numberUserSubscription)))
                    .add(new Text(" | "))
                    .add(new Text(context.getString(R.string.text_unsigned) + ": ").setBold())
                    .add(new Text(String.valueOf(numberUserUnsubscription)))
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(attendanceParagraph);
            Paragraph dateParagraph = new Paragraph()
                    .add(new Text(context.getString(R.string.date).toUpperCase() + ": ").setBold())
                    .add(new Text(meal.getDate()))
                    .setTextAlignment(TextAlignment.LEFT);
            document.add(dateParagraph);
            Paragraph kindParagraph = new Paragraph()
                    .add(new Text("meal".toUpperCase() + ": ").setBold())
                    .add(new Text(meal.getName()))
                    .setTextAlignment(TextAlignment.LEFT);
            document.add(kindParagraph);
            Table table = new Table(UnitValue.createPercentArray(new float[]{10, 50, 25, 15}))
                    .useAllAvailableWidth();
            table.setMarginTop(20);
            table.addHeaderCell(new Paragraph(context.getString(R.string.num)).setBold());
            table.addHeaderCell(new Paragraph(context.getString(R.string.full_name)).setBold());
            table.addHeaderCell(new Paragraph(context.getString(R.string.login)).setBold());
            table.addHeaderCell(new Paragraph(context.getString(R.string.subscription)).setBold());
            for (int i = 0; i < userList.size(); i++) {
                User user = userList.get(i);
                table.addCell(new Paragraph(String.valueOf(i + 1)));
                table.addCell(new Paragraph(user.displayName));
                table.addCell(new Paragraph(user.login));
                if (user.isSubscription() != null && user.isSubscription()) {
                    table.addCell(new Paragraph(context.getString(R.string.text_signed)).setFontColor(green, 100));
                } else if (user.isSubscription() != null && !user.isSubscription()) {
                    table.addCell(new Paragraph(context.getString(R.string.text_unsigned)).setFontColor(red, 100));
                }
            }
            document.add(table);
            document.close();
            return file;
        } catch (Exception e) {
            Util.showAlertDialogBuild("PDF", context.getString(R.string.pdf_not_created) + e.getMessage(), context, null);
            return null;
        }
    }

    // Evento para aplicar marca d'água em todas as páginas
    private static class TextWatermarkEvent implements IEventHandler {

        @Override
        public void handleEvent(com.itextpdf.kernel.events.Event event) {
            PdfDocumentEvent documentEvent = (PdfDocumentEvent) event;
            PdfPage page = documentEvent.getPage();
            Rectangle pageSize = page.getPageSize();
            PdfCanvas pdfCanvas = new PdfCanvas(page);
            try {
                PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
                pdfCanvas.saveState();
                pdfCanvas.beginText()
                        .setFontAndSize(font, 60)
                        .setColor(ColorConstants.LIGHT_GRAY, true)
                        .moveText(pageSize.getWidth() / 2 - 150, pageSize.getHeight() / 2)
                        .showText("CONFIDENCIAL")
                        .endText();
                pdfCanvas.restoreState();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Evento para aplicar marca d'água em todas as páginas
    private static class ImageWatermarkEvent implements IEventHandler {

        private final ImageData imageData;

        public ImageWatermarkEvent(ImageData imageData) {
            this.imageData = imageData;
        }

        @Override
        public void handleEvent(com.itextpdf.kernel.events.Event event) {
            PdfDocumentEvent documentEvent = (PdfDocumentEvent) event;
            PdfPage page = documentEvent.getPage();
            Rectangle pageSize = page.getPageSize();
            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), page.getDocument()); // desenhar antes do conteúdo principal

            float offsetX = -100f; // Ajuste para mover à esquerda (negativo)
            float offsetY = 0f; // Para deslocar na vertical
            float imageWidth = pageSize.getWidth() / 2;
            float imageHeight = pageSize.getHeight() / 2;

            //Definir transparencia
            pdfCanvas.saveState();
            PdfExtGState extGState = new PdfExtGState();
            extGState.setFillOpacity(0.15f);
            pdfCanvas.setExtGState(extGState);

            pdfCanvas.addImageFittedIntoRectangle(imageData,
                    new Rectangle(
                            ((pageSize.getHeight() - imageWidth) / 2) + offsetX, // Deslocar para esquerda
                            ((pageSize.getHeight() - imageHeight) / 2) + offsetY,
                            imageWidth, imageHeight),
                    false);
            pdfCanvas.restoreState();
        }
    }
}