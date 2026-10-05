package com.sumire.celeritasextra.client.gui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class TranslationIntegrityTest {
    private static final Path LANG = Path.of("src/main/resources/assets/celeritasextra/lang");
    private static final Pattern FORMAT = Pattern.compile("%(?:[0-9]+\\$)?[-#+ 0,(]*[0-9]*(?:\\.[0-9]+)?[a-zA-Z%]");

    private static Map<String, String> readLanguage(Path file) throws Exception {
        Map<String, String> entries = new LinkedHashMap<>();
        for (String line : Files.readAllLines(file)) {
            if (line.isBlank() || line.startsWith("#")) continue;
            int equals = line.indexOf('=');
            assertTrue(equals > 0, file + ": invalid language line: " + line);
            String key = line.substring(0, equals);
            assertNull(entries.put(key, line.substring(equals + 1)), file + ": duplicate " + key);
        }
        return entries;
    }

    @Test void everyLanguageHasTheSameKeysAndCompatibleFormatArguments() throws Exception {
        Map<String, String> english = readLanguage(LANG.resolve("en_us.lang"));
        try (var files = Files.list(LANG)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".lang")).toList()) {
                Map<String, String> translated = readLanguage(file);
                assertEquals(english.keySet(), translated.keySet(), file.toString());
                var minecraftLocale = new net.minecraft.client.resources.Locale();
                var load = net.minecraft.client.resources.Locale.class.getDeclaredMethod("loadLocaleData", InputStream.class);
                load.setAccessible(true);
                try (var input = Files.newInputStream(file)) { load.invoke(minecraftLocale, input); }
                for (String key : english.keySet()) {
                    assertEquals(arguments(english.get(key)), arguments(translated.get(key)), file + ": " + key);
                    int argumentCount = arguments(translated.get(key));
                    // The real 1.12 parser normalizes numeric placeholders (%d/%f) to %s.
                    String formatted = minecraftLocale.formatMessage(key, Collections.nCopies(argumentCount, "sample").toArray());
                    assertFalse(formatted.startsWith("Format error:"), file + ": " + key + ": " + formatted);
                }
            }
        }
    }

    @Test void allJavaReferencesAndDerivedTooltipsResolve() throws Exception {
        Set<String> defined = new HashSet<>(readLanguage(LANG.resolve("en_us.lang")).keySet());
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("assets/minecraft/lang/en_us.lang")) {
            assertNotNull(stream, "Minecraft's real English language resources must be on the test classpath");
            for (String line : new String(stream.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
                int equals = line.indexOf('=');
                if (equals > 0 && !line.startsWith("#")) defined.add(line.substring(0, equals));
            }
        }
        Pattern keys = Pattern.compile("\"((?:sodium-extra|celeritasextra|moreculling|sodium\\.extras|rso|options|tile|entity|subtitles|soundCategory|gui|generator)\\.[^\"]+)\"");
        Pattern controls = Pattern.compile("(?:booleanOption|sliderOption)\\(\"([^\"]+)\"(?:,\\s*\"([^\"]+)\")?");
        try (var paths = Files.walk(Path.of("src/main/java"))) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
                String source = Files.readString(path);
                var matcher = keys.matcher(source);
                while (matcher.find()) assertTrue(defined.contains(matcher.group(1)), path + ": missing " + matcher.group(1));
                matcher = controls.matcher(source);
                while (matcher.find()) {
                    String tooltip = matcher.group(2) != null ? matcher.group(2) : Translations.tooltipKey(matcher.group(1));
                    assertTrue(defined.contains(tooltip), path + ": missing derived tooltip " + tooltip);
                }
            }
        }
    }

    @Test void provenanceMapCoversMigratedKeysAndExplicitTooltipMappings() throws Exception {
        JsonObject manifest = new JsonParser().parse(Files.readString(Path.of("translations/upstream-keys.json"))).getAsJsonObject();
        Set<String> defined = readLanguage(LANG.resolve("en_us.lang")).keySet();
        Set<String> builtin = new HashSet<>();
        manifest.getAsJsonArray("builtinKeys").forEach(key -> builtin.add(key.getAsString()));
        for (var entry : manifest.getAsJsonArray("mappings")) {
            var row = entry.getAsJsonObject();
            if (row.get("key").isJsonNull()) continue;
            String key = row.get("key").getAsString();
            assertTrue(defined.contains(key) || builtin.contains(key), "Unresolved migration: " + key);
        }
        manifest.getAsJsonObject("tooltips").entrySet().forEach(entry ->
                assertEquals(entry.getValue().getAsString(), Translations.tooltipKey(entry.getKey())));
        assertEquals("sodium-extra.option.item_frames.tooltip", Translations.tooltipKey("item.frame.name"));
        assertEquals("First\nSecond", Translations.decodeNewlines("First\\nSecond"));
    }

    private static int arguments(String value) {
        return (int) FORMAT.matcher(value).results().filter(match -> !match.group().equals("%%")).count();
    }
}
