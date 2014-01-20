package itu.abc4gsd.rcp.client_v6.draw2d.notification.notifier;

//Adapted from http://hexapixel.com/
//http://hexapixel.com/2009/06/30/creating-a-notification-popup-widget

import org.eclipse.swt.graphics.Image;
import itu.abc4gsd.rcp.client_v6.draw2d.notification.cache.ImageCache;

public enum NotificationType {
    WARN(ImageCache.getImage("notification_warn.png")),
    WARN2(ImageCache.getImage("notification_warn2.png"));
//    ERROR(ImageCache.getImage("error.png")),
//    DELETE(ImageCache.getImage("delete.png")),
//    SUCCESS(ImageCache.getImage("ok.png")),
//    INFO(ImageCache.getImage("info.png")),
//    LIBRARY(ImageCache.getImage("library.png")),
//    HINT(ImageCache.getImage("hint.png")),
//    PRINTED(ImageCache.getImage("printer.png")),
//    CONNECTION_TERMINATED(ImageCache.getImage("terminated.png")),
//    CONNECTION_FAILED(ImageCache.getImage("connecting.png")),
//    CONNECTED(ImageCache.getImage("connected.png")),
//    DISCONNECTED(ImageCache.getImage("disconnected.png")),
//    TRANSACTION_OK(ImageCache.getImage("ok.png")),
//    TRANSACTION_FAIL(ImageCache.getImage("error.png"));

    private Image _image;

    private NotificationType(Image img) {
        _image = img;
    }

    public Image getImage() {
        return _image;
    }
}
