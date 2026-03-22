import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

public class RefactorTool {

    // Base directory of the project's source code
    private static final String BASE_DIR = "src/main/java/com/metaautomation";
    
    // Map of old root packages to new module names
    private static final Map<String, String> MODULE_MAP = new HashMap<>();
    static {
        MODULE_MAP.put("lead", "modules/crm");
        MODULE_MAP.put("accounting", "modules/finance");
        MODULE_MAP.put("order", "modules/orders");
        MODULE_MAP.put("quotation", "modules/quotation");
        MODULE_MAP.put("inventory", "modules/inventory");
        MODULE_MAP.put("user", "modules/core"); // covers auth, user, company, branch
        MODULE_MAP.put("catalog", "modules/catalog");
        MODULE_MAP.put("report", "modules/report");
        MODULE_MAP.put("master", "modules/master");
    }

    public static void main(String[] args) throws IOException {
        Path basePath = Paths.get(BASE_DIR);
        if (!Files.exists(basePath)) {
            System.err.println("Base path not found: " + BASE_DIR);
            return;
        }

        // 1. Discover all java files
        List<Path> allJavaFiles = Files.walk(basePath)
                .filter(p -> p.toString().endsWith(".java"))
                .collect(Collectors.toList());

        System.out.println("Found " + allJavaFiles.size() + " Java files.");

        // 2. Map old class exact names to new fully qualified class names
        Map<String, String> classFqnMap = new HashMap<>(); // old FQN -> new FQN
        Map<Path, Path> fileMoveMap = new HashMap<>();     // old Path -> new Path

        for (Path file : allJavaFiles) {
            String relativePath = basePath.relativize(file).toString().replace("\\", "/");
            String[] parts = relativePath.split("/");
            if (parts.length < 2) continue; // inside com.mini2more.crm directly?

            String rootPkg = parts[0];
            if (!MODULE_MAP.containsKey(rootPkg)) {
                // skip common, config, security, etc.
                continue;
            }

            String newModule = MODULE_MAP.get(rootPkg);
            String fileName = file.getFileName().toString();
            String className = fileName.replace(".java", "");

            // Determine Layer sub-package (entity, dto, controller, service, repository, etc)
            String layer = "entity"; // default
            if (className.endsWith("Controller") || className.endsWith("Api")) {
                layer = "controller";
            } else if (className.endsWith("Service") || className.endsWith("ServiceImpl") || className.endsWith("Engine")) {
                layer = "service";
            } else if (className.endsWith("Repository") || className.endsWith("Dao")) {
                layer = "repository";
            } else if (className.endsWith("Dto") || className.endsWith("Request") || className.endsWith("Response") || className.endsWith("Payload")) {
                layer = "dto";
            } else if (className.endsWith("Exception")) {
                layer = "exception";
            } else if (className.endsWith("Config")) {
                layer = "config";
            } else if (className.endsWith("Workflow") || className.endsWith("Task")) {
                layer = "workflow";
            } else if (relativePath.contains("/dto/")) {
                layer = "dto";
            } else if (relativePath.contains("/enums/")) {
                layer = "enums";
            }

            // Old Fully Qualified Name
            String oldPackage = "com.mini2more.crm." + rootPkg;
            if (parts.length > 2) { // it was already in a subpackage e.g. com.mini2more.crm.user.dto
                oldPackage = "com.mini2more.crm." + relativePath.substring(0, relativePath.lastIndexOf('/')).replace('/', '.');
            }
            String oldFqn = oldPackage + "." + className;

            // New Fully Qualified Name
            String newPackage = "com.mini2more.crm." + newModule.replace("/", ".") + "." + layer;
            String newFqn = newPackage + "." + className;

            classFqnMap.put(oldFqn, newFqn);

            // New Path
            String newRelativePath = newModule + "/" + layer + "/" + fileName;
            Path newPath = basePath.resolve(newRelativePath);
            fileMoveMap.put(file, newPath);
        }

        System.out.println("Mapped " + fileMoveMap.size() + " files to be moved to module layers.");

        // 3. Process all files (including ones that aren't moving, to update their imports)
        for (Path file : allJavaFiles) {
            String content = new String(Files.readAllBytes(file));
            boolean modified = false;

            // A) Update Package Name if this file is moving
            if (fileMoveMap.containsKey(file)) {
                Path dest = fileMoveMap.get(file);
                String newRelative = basePath.relativize(dest).toString().replace("\\", "/");
                String newPkg = "com.mini2more.crm." + newRelative.substring(0, newRelative.lastIndexOf('/')).replace('/', '.');
                
                content = content.replaceAll("package\\s+com\\.metaautomation\\.[^;]+;", "package " + newPkg + ";");
                modified = true;
            }

            // B) Update Import Statements
            for (Map.Entry<String, String> entry : classFqnMap.entrySet()) {
                String oldFqn = entry.getKey();
                String newFqn = entry.getValue();
                
                // Replace "import com.mini2more.crm.lead.Lead;" with "import com.mini2more.crm.modules.crm.entity.Lead;"
                String oldImport = "import " + oldFqn + ";";
                String newImport = "import " + newFqn + ";";
                
                if (content.contains(oldImport)) {
                    content = content.replace(oldImport, newImport);
                    modified = true;
                }
                
                // Also handle fully qualified references in code (e.g. annotations or throws)
                // Need word boundary to avoid partial matches
                String oldFqnBounded = "\\b" + oldFqn.replace(".", "\\.") + "\\b";
                Pattern pattern = Pattern.compile(oldFqnBounded);
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    content = matcher.replaceAll(newFqn);
                    modified = true;
                }
                
                // C) Inject intra-package missing imports if they were split into different layers
                if (fileMoveMap.containsKey(file)) {
                    String relativePath = basePath.relativize(file).toString().replace("\\", "/");
                    String[] parts = relativePath.split("/");
                    if (parts.length >= 2) {
                        String oldFilePkg = "com.mini2more.crm." + relativePath.substring(0, relativePath.lastIndexOf('/')).replace('/', '.');
                        String oldClassPkg = oldFqn.substring(0, oldFqn.lastIndexOf('.'));
                        String newClassPkg = newFqn.substring(0, newFqn.lastIndexOf('.'));
                        
                        if (oldFilePkg.equals(oldClassPkg)) {
                            Path dest = fileMoveMap.get(file);
                            String newRelative = basePath.relativize(dest).toString().replace("\\", "/");
                            String newFilePkg = "com.mini2more.crm." + newRelative.substring(0, newRelative.lastIndexOf('/')).replace('/', '.');
                            
                            if (!newFilePkg.equals(newClassPkg)) {
                                String className = newFqn.substring(newFqn.lastIndexOf('.') + 1);
                                if (content.matches("(?s).*\\b" + className + "\\b.*") && !content.contains("import " + newFqn + ";")) {
                                    content = content.replaceFirst("(package\\s+[^;]+;)", "$1\nimport " + newFqn + ";");
                                    modified = true;
                                }
                            }
                        }
                    }
                }
            }

            if (modified) {
                Files.write(file, content.getBytes());
            }
        }

        // 4. Move the files physically
        for (Map.Entry<Path, Path> entry : fileMoveMap.entrySet()) {
            Path src = entry.getKey();
            Path dest = entry.getValue();

            Files.createDirectories(dest.getParent());
            Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        System.out.println("Refactoring complete! The packages have been reorganized into the target layout.");
    }
}
