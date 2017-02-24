package com.twosigma.documentation.extensions.include;

import com.twosigma.documentation.codesnippets.CodeSnippetsProps;
import com.twosigma.documentation.core.AuxiliaryFile;
import com.twosigma.documentation.core.ComponentsRegistry;
import com.twosigma.documentation.extensions.ReactComponent;
import com.twosigma.documentation.parser.docelement.DocElementType;
import com.twosigma.utils.StringUtils;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author mykola
 */
public class TextFileIncludePlugin implements IncludePlugin {
    @Override
    public String id() {
        return "text-file";
    }

    @Override
    public void reset(IncludeContext context) {
    }

    @Override
    public IncludePluginResult process(ComponentsRegistry componentsRegistry, Path markupPath, IncludeParams includeParams) {
        String fileName = includeParams.getFreeParam();

        String text = extractText(componentsRegistry.includeResourceResolver().
                textContent(fileName), includeParams.getOpts());

        String providedLang = includeParams.getOpts().getString("lang");
        String langToUse = (providedLang == null) ? langFromFileName(fileName) : providedLang;

        Map<String, Object> props = CodeSnippetsProps.create(componentsRegistry.codeTokenizer(), langToUse, text);
        props.putAll(includeParams.getOpts().toMap());

        return IncludePluginResult.reactComponent(DocElementType.SNIPPET, props);
    }

    @Override
    public Stream<AuxiliaryFile> auxiliaryFiles(ComponentsRegistry componentsRegistry, IncludeParams includeParams) {
        return Stream.of(AuxiliaryFile.builtTime(
                componentsRegistry.includeResourceResolver().fullPath(includeParams.getFreeParam())));
    }

    private String extractText(String text, IncludeParamsOpts opts) {
        if (opts.isEmpty()) {
            return text;
        }

        Number numberOfLines = opts.has("numberOfLines") ? opts.get("numberOfLines") : Integer.MAX_VALUE;
        if (opts.has("startLine")) {
            String startLine = opts.get("startLine").toString();
            return new Text(text).startingWithLineContaining(startLine, numberOfLines).toString();
        }

        return text;
    }

    private static String langFromFileName(String fileName) {
        String ext = extFromFileName(fileName);
        switch (ext) {
            case "js": return "javascript";
            default: return ext;
        }
    }

    private static String extFromFileName(String fileName) {
        int dotLastIdx = fileName.lastIndexOf('.');
        if (dotLastIdx == -1) {
            return "";
        }

        return fileName.substring(dotLastIdx + 1);
    }

    private static class Text {
        private final String text;
        private final String[] lines;

        public Text(String text) {
            this.text = text;
            this.lines = text.split("\n");
        }

        int findLineIdxContaining(String subLine) {
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].contains(subLine)) {
                    return i;
                }
            }

            return -1;
        }

        @Override
        public String toString() {
            return text;
        }

        Text startingWithLineContaining(String subline, Number numberOfLines) {
            int lineIdx = findLineIdxContaining(subline);
            if (lineIdx == -1) {
                throw new IllegalArgumentException("<there is no line containing '" + subline + "'> in:\n" + text);
            }

            return new Text(Arrays.stream(lines).skip(lineIdx).limit(numberOfLines.intValue()).collect(Collectors.joining("\n")));
        }
    }
}
