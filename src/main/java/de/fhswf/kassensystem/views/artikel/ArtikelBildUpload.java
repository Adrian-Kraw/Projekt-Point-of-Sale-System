package de.fhswf.kassensystem.views.artikel;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;

import java.util.Base64;

/**
 * Bild-Upload-Komponente mit Vorschau für den Artikel-Dialog.
 * Vaadin 25 kompatibel – nutzt UploadHandler.inMemory() statt MemoryBuffer.
 */
class ArtikelBildUpload extends VerticalLayout {

    private byte[] bildBytes = null;

    ArtikelBildUpload() {
        setPadding(false);
        setSpacing(false);
        getStyle().set("gap", "0.4rem");

        Span label = new Span("ARTIKELBILD (OPTIONAL)");
        label.getStyle()
                .set("font-size", "0.6rem").set("font-weight", "800")
                .set("text-transform", "uppercase").set("letter-spacing", "0.1em")
                .set("color", "#82746d").set("font-family", "'Plus Jakarta Sans', sans-serif");

        Image vorschau = new Image();
        vorschau.getStyle()
                .set("width", "100%").set("max-height", "8rem").set("object-fit", "cover")
                .set("border-radius", "0.75rem").set("display", "none");

        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            bildBytes = data;
            String base64  = Base64.getEncoder().encodeToString(data);
            String dataUrl = "data:" + metadata.contentType() + ";base64," + base64;
            getUI().ifPresent(ui -> ui.access(() -> {
                vorschau.getElement().setAttribute("src", dataUrl);
                vorschau.getStyle().set("display", "block");
                Notification.show("Bild geladen: " + metadata.fileName(),
                        2000, Notification.Position.BOTTOM_START);
            }));
        }));
        upload.setWidthFull();
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024);

        upload.addFileRemovedListener(event -> {
            bildBytes = null;
            vorschau.getElement().setAttribute("src", "");
            vorschau.getStyle().set("display", "none");
        });

        add(label, upload, vorschau);
    }

    byte[] getBildBytes() {
        return bildBytes;
    }

    void setBild(byte[] bild) {
        if (bild == null || bild.length == 0) return;
        bildBytes = bild;
    }
}