package com.shuzijun.markdown.editor;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** Watch just the visible file, and coalesce document events before notifying JCEF. */
final class ExternalFileSync {
    ExternalFileSync(VirtualFile file, Document document, MarkdownHtmlPanel panel,
                     JComponent component, Disposable parent) {
        Timer delivery = new Timer(75, event -> {
            if (!panel.isDisposed()) {
                panel.getCefBrowser().executeJavaScript(
                        "if (typeof editFocus === 'function' && typeof vditor !== 'undefined') editFocus();",
                        panel.getCefBrowser().getURL(), 0);
            }
        });
        delivery.setRepeats(false);
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                SwingUtilities.invokeLater(() -> {
                    if (!panel.isDisposed()) delivery.restart();
                });
            }
        }, parent);
        // IntelliJ's global refresh can wait for application activation. Refresh only
        // this file asynchronously, never scan the project or perform EDT disk I/O.
        Timer watcher = new Timer(1000, event -> {
            if (!panel.isDisposed() && component.isShowing() && file.isValid()) {
                file.refresh(true, false);
            }
        });
        watcher.start();
        Disposer.register(parent, () -> {
            watcher.stop();
            delivery.stop();
        });
    }
}
