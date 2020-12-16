import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleApp {

    // TODO bash commands verification (how each command behave in bash)
    // TODO JUnit 5 test suite in a separate file
    // TODO java.nio version

    private static File currPath = new File("/");

    public static final String CD = "cd";
    public static final String DESTR = "destr"; // removes non-empty folders
    public static final String EXIT = "exit";
    public static final String LS = "ls";
    public static final String MKDIR = "mkdir";
    public static final String MV = "mv"; // method doesn't work: requires java.nio
    public static final String RM = "rm";
    public static final String TOUCH = "touch";

    public static void main(String[] args) throws IOException {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);

        while (true) {
            displayMenu();

            String[] input = br.readLine().trim().split(" ");
            if (input.length == 0) {
                continue;
            }

            if (input.length == 1) {
                switch (input[0].toLowerCase()) {
                    case LS:
                        list();
                        break;
                    case EXIT:
                        System.out.println("Good Bye :-)");
                        return;
                    case CD:
                    case MKDIR:
                    // case MV:
                    case RM:
                    case TOUCH:
                        System.out.println("Wrong command format. Try again");
                        break;
                    default:
                        System.out.println("Unknown command. Try again");
                }
            } else {
                switch (input[0].toLowerCase()) {
                    case CD:
                        if (input[1].equals("..")) {
                            File parent = currPath.getParentFile();
                            currPath = parent != null ? parent : currPath;
                        } else {
                            changeDir(formDirectoryName(input));
                        }
                        break;
                    case TOUCH:
                        createFile(input[1]);
                        break;
                    case MKDIR:
                        createDir(input[1]);
                        break;
                    // case MV:
                        // moveFileDir(input);
                        // break;
                    case RM:
                        remove(input[1]);
                        break;
                    case DESTR:
                        destroy(input[1]);
                        break;
                    default:
                        System.out.println("Unknown command. Try again");
                }
            }
        }
    }

    public static void displayMenu() {
        System.out.println("\t ~C~O~N~S~O~L~E~~~A~P~P~~~2020-12-16~");
        System.out.println("\t\t ls - display list of files and sub-folders of current folder");
        System.out.println("\t\t cd [dir] - change folder");
        System.out.println("\t\t touch [file] - create a new file");
        System.out.println("\t\t mkdir [dir] - create a new sub-folder");
        // System.out.println("\t\t mv [file/dir] > [file/dir] - move or rename file or directory");
        System.out.println("\t\t rm [file/dir] - remove file or empty sub-folder");
        System.out.println("\t\t destr [file/dir] - remove* file or sub-folder");
        System.out.println("\t\t exit - exit the program");
        System.out.println("\t *folder will be removed regardless if it is empty or not");
        System.out.println("\t ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.print(currPath.getAbsolutePath() + "> ");
    }

    public static void list() {
        String[] objects = currPath.list();
        if (objects == null || objects.length == 0) {
            System.out.println("The folder is empty or access-restricted");
            return;
        }
        for (String o : objects) {
            System.out.println(o);
        }
    }

    public static void changeDir(String childDir) {
        for (char ch : childDir.toCharArray()) { // workaround for "external" bug (in Windows): // cd ..... => C:\.....
            if (ch != '.') {
                break;
            } else {
                return;
            }
        }

        File newPath = new File(currPath, childDir);
        if (newPath.exists() && newPath.isDirectory()) {
            currPath = newPath;
        } else {
            System.out.println("Specified folder does not exist in current folder");
        }
    }

    public static void createFile(String fileName) {
        File newFile = new File(currPath, fileName);
        if (newFile.exists()) {
            System.out.println("Failed to create " + fileName + ": file already exists. " +
                    "Choose different file name");
            return;
        }

        try {
            if (newFile.createNewFile()) {
                System.out.println("Empty file " + fileName + " created successfully");
            } else {
                System.out.println("Failed to create " + fileName);
            }
        } catch (IOException ex) {
            System.out.println("Failed to create " + fileName + ". Possible write protection");
        }
    }

    public static void createDir(String dirName) {
        File newDir = new File(currPath, dirName);
        if (newDir.exists()) {
            System.out.println("Failed to create " + dirName + ": folder already exists " +
                    "(or syntax error). Choose different folder name");
            return;
        }

        if (newDir.mkdir()) {
            System.out.println("Empty folder " + dirName + " created successfully");
        } else {
            System.out.println("Failed to create " + dirName + ". Possible write protection (or syntax error)");
        }
        // TODO prevent user entering the access-restricted directories
    }

    public static void remove(String name) {
        File obj = removeIfEmpty(name);
        if (obj == null) {
            return;
        }

        if (obj.isDirectory() && obj.list() != null) {
            System.out.println("Failed to remove " + name + ". Folder is not empty (or syntax error)");
        } else {
            System.out.println("Failed to remove " + name + ". Possible write protection");
        }
    }

    public static void destroy(String name) {
        File obj = removeIfEmpty(name);
        if (obj == null) {
            return;
        }

        if (obj.isDirectory() && obj.listFiles() != null) {
            for (File o : obj.listFiles()) {
                if (!o.delete()) {
                    System.out.println("Failed to remove file or folder. Possible write protection");
                    return;
                }
            }
            removeIfEmpty(name);
        }
        if (obj.exists()) {
            System.out.println("Failed to remove " + name + ". Possible write protection");
        }
    }

    private static File removeIfEmpty(String name) {
        if (name.equals(".")) { // workaround for unwanted removal of current directory in Windows
            return null;
        }
        File obj = new File(currPath, name);
        if (!obj.exists()) {
            System.out.println(name + " was not found in current folder");
            return null;
        }

        // TODO workaround: following code removes access-restricted files (not directories) in Windows
        if (obj.delete()) {
            System.out.println(name + " removed successfully");
            return null;
        }
        return obj;
    }

    private static String formDirectoryName(String[] input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < input.length; i++) {
            sb.append(input[i]).append(" ");
        }
        return sb.toString().trim();
    }

    private static void moveFileDir(String[] input) {
        // TODO requires java.nio to work properly: https://stackoverflow.com/questions/1000183/reliable-file-renameto-alternative-on-windows/5451161#5451161
        /*
        if (input.length < 4 || input[1].equals(">")) {
            System.out.println("Wrong command format. Try again");
            return;
        }

        StringBuilder nameCurr = new StringBuilder();
        for (int i = 1; i < input.length - 1; i++) {
            if (input[i].equals(">")) {
                break;
            }
            if (i == input.length - 1) {
                System.out.println("Wrong command format. Try again");
                return;
            }
            nameCurr.append(input[i]);
        }

        StringBuilder nameTarget = new StringBuilder();
        for (int i = 3; i < input.length; i++) {
            nameTarget.append(input[i]);
        }

        //remove this debugging
        System.out.println(nameCurr.toString() + " > " + nameTarget.toString());

        try {
            File currObject = new File(nameCurr.toString());
            File targetObject = new File(nameTarget.toString());
            if (targetObject.exists()) {
                System.out.println("File or directory already exists");
                return;
            }
            boolean success = false;
            for (int i = 0; i < 2000; i++) {
                if (currObject.renameTo(targetObject)) {
                    success = true;
                    break;
                }
                System.gc();
                Thread.yield();
            }
            if (!success) {
                System.out.println("An error occurred while performing the operation");
            }
        } catch (Exception e) {
            System.out.println("An error occurred. " +
                    "Possibly, wrong command format or naming issue. Try again");
        }
    }
         */
    }
}
