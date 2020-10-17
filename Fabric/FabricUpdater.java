import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FabricUpdater {

    public static void main(String[] args) throws IOException{
        String versionJson = readStringFromURL("https://meta.fabricmc.net/v1/versions/game");
        String[] versions = parseJsonArray(versionJson)
                .map(FabricUpdater::parseJsonObject)
                .map(o -> o == null ? "\"null\"" : o.get("version"))
                .map(FabricUpdater::parseJsonString)
                .toArray(String[]::new);
        int latestStable = (int)parseJsonArray(versionJson)
                .map(FabricUpdater::parseJsonObject)
                .map(o -> o == null ? "false" : o.get("stable"))
                .takeWhile(s -> !Boolean.parseBoolean(s))
                .count();

        String version = (String)JOptionPane.showInputDialog(null,
                "Choose Version",
                "Fabric Version Updater",
                JOptionPane.PLAIN_MESSAGE,
                null, versions, versions[latestStable]);
        if(version != null){
            Optional<Map<String, String>> loaderversion =
                    parseJsonArray(readStringFromURL("https://meta.fabricmc.net/v1/versions/loader/" + version))
                    .map(FabricUpdater::parseJsonObject)
                    .findFirst();

            if(loaderversion.isPresent()){
                Path filename = Path.of("gradle.properties");
                List<String> config = Files.readAllLines(filename);
                config.replaceAll(line -> {
                    if(line.startsWith("minecraft_version")) {
                        return Optional.ofNullable(loaderversion.get().get("mappings"))
                                .map(FabricUpdater::parseJsonObject)
                                .map(o -> o.get("gameVersion"))
                                .map(FabricUpdater::parseJsonString)
                                .map(s -> "minecraft_version=" + s)
                                .orElseThrow();
                    }
                    if(line.startsWith("yarn_mappings")) {
                        return Optional.ofNullable(loaderversion.get().get("mappings"))
                                .map(FabricUpdater::parseJsonObject)
                                .map(o -> o.get("version"))
                                .map(FabricUpdater::parseJsonString)
                                .map(s -> "yarn_mappings=" + s)
                                .orElseThrow();
                    }
                    if(line.startsWith("loader")) {
                        return Optional.ofNullable(loaderversion.get().get("loader"))
                                .map(FabricUpdater::parseJsonObject)
                                .map(o -> o.get("version"))
                                .map(FabricUpdater::parseJsonString)
                                .map(s -> "loader_version=" + s)
                                .orElseThrow();
                    }
                    return line;
                });
                Files.write(filename, config);
            }
        }


    }

    //https://stackoverflow.com/questions/4328711/read-url-to-string-in-few-lines-of-java-code
    public static String readStringFromURL(String requestURL) throws IOException
    {
        try (Scanner scanner = new Scanner(new URL(requestURL).openStream(),
                StandardCharsets.UTF_8.toString()))
        {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    private static int skipString(int currentIndex, String str){
        //TODO escaped strings?
        if(currentIndex < str.length() && str.charAt(currentIndex) == '"'){
            currentIndex++;
            while(currentIndex < str.length() && str.charAt(currentIndex) != '"')
                currentIndex++;
            currentIndex++;
        }
        return currentIndex;
    }

    private static int skipObject(int currentIndex, String str){
        if(currentIndex < str.length() && str.charAt(currentIndex) == '{'){
            currentIndex++;
            while(currentIndex < str.length() && str.charAt(currentIndex) != '}')
                currentIndex = applySkips(currentIndex + 1, str, FabricUpdater::skipString, FabricUpdater::skipObject);
            currentIndex++;
        }
        return currentIndex;
    }


    private static int applySkips(int currentIndex, String str, BiFunction<Integer, String, Integer>... functions){
        int oldIndex = -1;
        do {
            oldIndex = currentIndex;
            for (BiFunction<Integer, String, Integer> f : functions)
                currentIndex = f.apply(currentIndex, str);
        } while(oldIndex != currentIndex);
        return currentIndex;
    }

    public static Stream<String> parseJsonArray(String json){
        final String str = json.trim();

        Iterator<String> it = new Iterator<String>() {
            int index = 0;
            boolean start = true;

            @Override
            public boolean hasNext() {
                if(index >= str.length())
                    return false;
                if(start)
                    return str.charAt(index) == '[';
                return str.charAt(index) == ',';
            }

            @Override
            public String next() {
                index++;
                int startIndex = index;
                while(index < str.length() && str.charAt(index) != ',' && str.charAt(index) != ']')
                    index = applySkips(index + 1, str, FabricUpdater::skipString, FabricUpdater::skipObject);
                String result = str.substring(startIndex, index).strip();
                start = false;
                return result;
            }

        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.NONNULL), false);
    }

    public static String parseJsonString(String str){
        str = str.strip();
        if(str.charAt(0) != '"' || str.charAt(str.length() - 1) != '"')
            throw new RuntimeException("Could not parse string: " + str);
        return str.substring(1, str.length() - 1);
    }

    public static Map<String, String> parseJsonObject(String json){
        json = json.trim();
        int index = 0;
        if(index < json.length() && json.charAt(index) == '{'){
            int startIndex = -1;
            Map<String, String> result = new HashMap<>();
            while (index < json.length() && json.charAt(index) != '}'){
                index++;
                startIndex = index;
                while(index < json.length() && json.charAt(index) != ':')
                    index = skipString(index + 1, json);
                String key = parseJsonString(json.substring(startIndex, index));
                index++;
                startIndex = index;
                while(index < json.length() && json.charAt(index) != ',' && json.charAt(index) != '}')
                    index = applySkips(index + 1, json, FabricUpdater::skipString, FabricUpdater::skipObject);
                String value = json.substring(startIndex, index).strip();
                result.put(key, value);
            }
            return result;
        }
        return null;
    }

}
