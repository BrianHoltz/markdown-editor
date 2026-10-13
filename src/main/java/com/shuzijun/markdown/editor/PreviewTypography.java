package com.shuzijun.markdown.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import org.intellij.plugins.markdown.settings.MarkdownPreviewSettings;
import com.intellij.ui.jcef.JBCefApp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Project the bundled preview's typography into Vditor without copying its stylesheet. */
final class PreviewTypography {
    private static final Logger LOG = Logger.getInstance(PreviewTypography.class);
    private static final String STYLESHEET = loadStylesheet();
    private static final String FONT_FAMILY = fontFamily(STYLESHEET);
    private static final String TYPOGRAPHY = typography(STYLESHEET);

    private PreviewTypography() {}

    static String css() {
        MarkdownPreviewSettings settings = ApplicationManager.getApplication().getService(MarkdownPreviewSettings.class);
        return ":root{--markdown-preview-font-family:" + FONT_FAMILY
                + ";--markdown-preview-font-size:" + JBCefApp.normalizeScaledSize(settings.getState().getFontSize()) + "px;}" + TYPOGRAPHY;
    }

    private static String loadStylesheet() {
        String resource = "/org/intellij/plugins/markdown/extensions/common/baseStyles/default.css";
        try (InputStream stream = MarkdownPreviewSettings.class.getResourceAsStream(resource)) {
            if (stream != null) {
                String stylesheet = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                fontFamily(stylesheet);
                return stylesheet;
            }
        } catch (IOException | IllegalArgumentException e) {
            LOG.warn("Unable to read Markdown preview typography; using system font", e);
            return "body { font-family: system-ui, sans-serif; line-height: 1.6; }";
        }
        LOG.warn("Markdown preview stylesheet missing; using system font");
        return "body { font-family: system-ui, sans-serif; line-height: 1.6; }";
    }

    private static String typography(String stylesheet) {
        StringBuilder css = new StringBuilder();
        Matcher rules = Pattern.compile("([^{}]+)\\{([^{}]*)\\}").matcher(stylesheet);
        while (rules.find()) {
            for (String selector : rules.group(1).split(",")) {
                selector = selector.trim();
                boolean body = selector.equals("body");
                if (!body && !selector.matches("h[1-6]|strong|code|pre|pre\\s*>\\s*code|kbd")) continue;
                StringBuilder declarations = new StringBuilder();
                for (String declaration : rules.group(2).split(";")) {
                    String[] parts = declaration.trim().split(":", 2);
                    if (parts.length != 2) continue;
                    String property = parts[0].trim();
                    if (!property.matches("font|font-family|font-size|font-weight|font-style|line-height")) continue;
                    if (body && (property.equals("font-size") || property.equals("font-family"))) continue;
                    declarations.append(property).append(':').append(parts[1].trim()).append(" !important;");
                }
                if (!declarations.isEmpty()) {
                    css.append(".vditor .vditor-reset");
                    if (!body) css.append(' ').append(selector);
                    css.append('{').append(declarations).append('}');
                }
            }
        }
        return css.toString();
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
