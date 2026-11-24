import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * One file that runs the whole Banking System.
 *
 * How to use (jGRASP): open this file, press Compile, then press Run.
 * It finds every .java file in this folder and its sub-folders (in any layout, even all mixed
 * together in one folder), files each one under its "package" name, compiles them together
 * and starts the JavaFX GUI. It uses only standard Java, so it compiles with no JavaFX settings.
 *
 * It looks for the JavaFX SDK in this order: the first program argument, the JAVAFX_HOME
 * environment variable, DEFAULT_JAVAFX below, then any "javafx-sdk-..." folder in your
 * Documents, Downloads or home folder.
 */
public class RunBankingSystem {

    /** Change this if your JavaFX SDK folder (the one that contains "lib") is somewhere else. */
    private static final String DEFAULT_JAVAFX = "C:\\Users\\Tarren$ting\\Documents\\javafx-sdk-25.0.2";

    public static void main(String[] args) throws Exception {
        Path launcherFolder = findBaseFolder();
        Path base = null;
        Map<String, Path> sources = new LinkedHashMap<>();
        // Look in this folder first, then one and two levels up, so the launcher can sit in
        // bank/model while the other packages live in bank/exception, bank/security and bank/view.
        Path candidate = launcherFolder;
        for (int level = 0; level < 3 && candidate != null; level++) {
            Map<String, Path> found = collectSources(candidate);
            if (found.containsKey("bank.view.MainApp")) {
                base = candidate;
                sources = found;
                break;
            }
            candidate = candidate.getParent();
        }
        if (base == null) {
            fail("Cannot find MainApp.java (the GUI) near " + launcherFolder + ".\n"
                    + "Keep RunBankingSystem.java inside the project (for example in bank/model) with the\n"
                    + "exception, security and view folders next to it.");
            return;
        }
        Path root = launcherFolder;
        Path javafxLib = findJavaFx(args);
        if (javafxLib == null) {
            fail("Cannot find the JavaFX SDK. Set DEFAULT_JAVAFX at the top of RunBankingSystem.java\n"
                    + "to the folder that contains 'lib' (for example C:\\Users\\you\\Documents\\javafx-sdk-21.0.5).");
            return;
        }

        System.out.println("Java " + Runtime.version() + ", JavaFX lib: " + javafxLib);
        if (Runtime.version().feature() < 17) {
            fail("This program needs JDK 17 or newer. You are running Java " + Runtime.version().feature() + ".");
            return;
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            fail("No Java compiler found. Run this with a JDK, not a JRE.");
            return;
        }

        Path staged = root.resolve("compiled-src");
        Path out = root.resolve("compiled");
        deleteTree(staged);
        deleteTree(out);
        Files.createDirectories(out);
        List<String> compileArgs = new ArrayList<>(List.of(
                "-d", out.toString(),
                "--module-path", javafxLib.toString(),
                "--add-modules", "javafx.controls"));
        for (Map.Entry<String, Path> entry : sources.entrySet()) {
            Path target = staged.resolve(entry.getKey().replace('.', File.separatorChar) + ".java");
            Files.createDirectories(target.getParent());
            Files.copy(entry.getValue(), target, StandardCopyOption.REPLACE_EXISTING);
            compileArgs.add(target.toString());
        }
        System.out.println("Found " + sources.size() + " source files under " + base);

        System.out.println("Compiling all classes together...");
        if (compiler.run(null, null, null, compileArgs.toArray(new String[0])) != 0) {
            fail("Compile failed. Copy the first error above and send it to your helper.\n"
                    + "If it says the JavaFX version is too new, use JavaFX SDK 21 with JDK 17 to 22.");
            return;
        }

        String java = Paths.get(System.getProperty("java.home"), "bin",
                System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java").toString();
        System.out.println("Starting the Banking System...");
        // MySQL driver: every .jar in a "lib" folder goes on the class path. The folder is looked for
        // next to the launcher and in the folders above it, so the project's top-level lib folder is found.
        StringBuilder classPath = new StringBuilder(out.toString());
        List<Path> searched = new ArrayList<>();
        Path configFile = null;
        boolean driverFound = false;
        Path folder = launcherFolder;
        for (int level = 0; level < 4 && folder != null; level++, folder = folder.getParent()) {
            Path lib = folder.resolve("lib");
            searched.add(lib);
            if (Files.isDirectory(lib)) {
                try (Stream<Path> jars = Files.list(lib)) {
                    for (Path jar : jars.filter(j -> j.toString().endsWith(".jar")).collect(Collectors.toList())) {
                        classPath.append(File.pathSeparator).append(jar);
                        System.out.println("MySQL driver: " + jar);
                        driverFound = true;
                    }
                }
            }
            if (configFile == null && Files.isRegularFile(folder.resolve("db.properties"))) {
                configFile = folder.resolve("db.properties");
            }
        }
        if (!driverFound) {
            System.out.println("NOTE: no .jar found in any of these lib folders: " + searched);
            System.out.println("      The program will run with sample data in memory.");
        }
        List<String> command = new ArrayList<>(List.of(java,
                "--module-path", javafxLib.toString(),
                "--add-modules", "javafx.controls",
                "-cp", classPath.toString()));
        if (configFile != null) {
            System.out.println("Database settings: " + configFile);
            command.add("-Dbank.config=" + configFile);
        } else {
            System.out.println("NOTE: db.properties not found; using root with no password.");
        }
        command.add("bank.view.MainApp");
        Process process = new ProcessBuilder(command)
                .inheritIO()
                .start();
        System.exit(process.waitFor());
    }

    /** The folder this launcher lives in (jGRASP compiles the class next to the source file). */
    private static Path findBaseFolder() {
        try {
            Path location = Paths.get(RunBankingSystem.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            Path dir = Files.isDirectory(location) ? location : location.getParent();
            if (dir != null && Files.exists(dir.resolve("RunBankingSystem.java"))) {
                return dir;
            }
        } catch (Exception ignored) {
            // use the working directory
        }
        return Paths.get("").toAbsolutePath();
    }

    /**
     * Finds every .java file under base that declares a package, whatever folder it sits in,
     * and returns them keyed by fully qualified class name. If the same class appears twice,
     * the most recently changed file wins. Test files and this launcher are skipped.
     */
    private static Map<String, Path> collectSources(Path base) throws IOException {
        Pattern pkg = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;", Pattern.MULTILINE);
        Map<String, Path> found = new LinkedHashMap<>();
        List<Path> files;
        try (Stream<Path> walk = Files.walk(base, 8)) {
            files = walk.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
        }
        for (Path file : files) {
            String name = file.getFileName().toString();
            boolean skip = name.equals("RunBankingSystem.java");
            for (Path part : base.relativize(file)) {
                String seg = part.toString().toLowerCase();
                if (seg.equals("compiled") || seg.equals("compiled-src") || seg.equals("tests")
                        || seg.equals("test") || seg.equals("out") || seg.equals("target")) {
                    skip = true;
                }
            }
            if (skip) {
                continue;
            }
            String text = new String(Files.readAllBytes(file), java.nio.charset.StandardCharsets.UTF_8);
            Matcher m = pkg.matcher(text);
            if (!m.find() || text.contains("org.junit")) {
                continue;
            }
            String key = m.group(1) + "." + name.substring(0, name.length() - ".java".length());
            Path old = found.get(key);
            if (old == null || Files.getLastModifiedTime(file).compareTo(Files.getLastModifiedTime(old)) > 0) {
                found.put(key, file);
            }
        }
        return found;
    }

    private static void deleteTree(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            for (Path p : walk.sorted(java.util.Comparator.reverseOrder()).collect(Collectors.toList())) {
                Files.delete(p);
            }
        }
    }

    private static Path findJavaFx(String[] args) throws IOException {
        List<String> choices = new ArrayList<>();
        if (args.length > 0) {
            choices.add(args[0]);
        }
        String env = System.getenv("JAVAFX_HOME");
        if (env != null) {
            choices.add(env);
        }
        choices.add(DEFAULT_JAVAFX);
        for (String choice : choices) {
            Path lib = libOf(Paths.get(choice));
            if (lib != null) {
                return lib;
            }
        }
        String home = System.getProperty("user.home");
        for (String sub : new String[]{"Documents", "Downloads", "Desktop", ""}) {
            Path dir = sub.isEmpty() ? Paths.get(home) : Paths.get(home, sub);
            if (!Files.isDirectory(dir)) {
                continue;
            }
            try (Stream<Path> kids = Files.list(dir)) {
                for (Path kid : kids.collect(Collectors.toList())) {
                    if (kid.getFileName().toString().toLowerCase().startsWith("javafx-sdk")) {
                        Path lib = libOf(kid);
                        if (lib != null) {
                            return lib;
                        }
                    }
                }
            }
        }
        return null;
    }

    /** Accepts the SDK folder or its lib folder; returns the lib folder if it holds JavaFX. */
    private static Path libOf(Path dir) {
        for (Path lib : new Path[]{dir.resolve("lib"), dir}) {
            if (Files.exists(lib.resolve("javafx.controls.jar"))) {
                return lib;
            }
        }
        return null;
    }

    private static void fail(String message) {
        System.err.println();
        System.err.println("ERROR: " + message);
        System.exit(1);
    }
}
