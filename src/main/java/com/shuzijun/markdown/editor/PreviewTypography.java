package com.shuzijun.markdown.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import org.intellij.plugins.markdown.settings.MarkdownPreviewSettings;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reuse the bundled preview's prose font without maintaining a second font stack. */
final class PreviewTypography {
    private static final Logger LOG = Logger.getInstance(PreviewTypography.class);
    private static final String FONT_FAMILY = loadFontFamily();

    private PreviewTypography() {}

    static String css() {
        MarkdownPreviewSettings settings = ApplicationManager.getApplication().getService(MarkdownPreviewSettings.class);
        return ":root{--markdown-preview-font-family:" + FONT_FAMILY
                + ";--markdown-preview-font-size:" + settings.getState().getFontSize() + "px;}";
    }

    private static String loadFontFamily() {
        String resource = "/org/intellij/plugins/markdown/extensions/common/baseStyles/default.css";
        try (InputStream stream = MarkdownPreviewSettings.class.getResourceAsStream(resource)) {
            if (stream != null) {
                return fontFamily(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
            }
        } catch (IOException | IllegalArgumentException e) {
            LOG.warn("Unable to read Markdown preview typography; using system font", e);
            return "system-ui, sans-serif";
        }
        LOG.warn("Markdown preview stylesheet missing; using system font");
        return "system-ui, sans-serif";
    }

    static String fontFamily(String stylesheet) {
        Matcher body = Pattern.compile("(?s)(?:^|\\})\\s*body\\s*\\{([^}]+)\\}").matcher(stylesheet);
        if (body.find()) {
            Matcher family = Pattern.compile("(?:^|;)\\s*font-family\\s*:\\s*([^;]+)").matcher(body.group(1));
            if (family.find()) return family.group(1).trim();
        }
        throw new IllegalArgumentException("Markdown preview stylesheet has no body font-family");
    }
}
