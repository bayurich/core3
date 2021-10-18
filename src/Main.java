import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {
    final static String ROOT_DIR = "D://Games/";
    static StringBuilder log = new StringBuilder();

    public static void  main(String[] args) {

        crDir(ROOT_DIR);

        crDir(ROOT_DIR + "src");
        crDir(ROOT_DIR + "res");
        crDir(ROOT_DIR + "savegames");
        crDir(ROOT_DIR + "temp");

        crDir(ROOT_DIR + "src/main");
        crDir(ROOT_DIR + "src/test");

        crFilePrintResult(ROOT_DIR + "src/main/Main.java");
        crFilePrintResult(ROOT_DIR + "src/main/Utils.java");

        crDir(ROOT_DIR + "res/drawables");
        crDir(ROOT_DIR + "res/vectors");
        crDir(ROOT_DIR + "res/icons");

        // -- task2 --
        GameProgress gameProgress1 = new GameProgress(100, 1, 2, 50);
        GameProgress gameProgress2 = new GameProgress(65, 3, 5, 500);
        GameProgress gameProgress3 = new GameProgress(28, 5, 7, 730.70);

        String path = ROOT_DIR + "savegames";
        saveProgress(gameProgress1, path + "/save1.dat");
        saveProgress(gameProgress2, path + "/save2.dat");
        saveProgress(gameProgress3, path + "/save3.dat");

        List<String> listForZip = new ArrayList<>();
        File saveDir = new File(path);
        if (saveDir.isDirectory() && saveDir.listFiles() != null) {
            for (File saveFile : saveDir.listFiles()) {
                if (!saveFile.isDirectory() && saveFile.getName().matches(".*\\.dat"))
                    listForZip.add(saveFile.getPath());
            }
        }
        zipFiles(path + "/save.zip", listForZip);
        for (String pathForDel : listForZip){
            deleteFile(pathForDel);
        }

        // -- task3 --
        openZip(path + "/save.zip", path);
        Optional<GameProgress> optional = openProgress(path + "/save2.dat");
        if (optional.isPresent()) {
            print("Состояние сохранненой игры: " + optional.get().toString());
        } else print("Состояние сохранненой игры не найдено");

        path = ROOT_DIR + "temp/temp.txt";
        crFilePrintResult(path);
        File file = new File(path);
        if (file.exists()) {
            try (FileWriter fileWriter = new FileWriter(file, true);
                 BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)){

                bufferedWriter.write(log.toString());
                bufferedWriter.flush();
            }
            catch (Exception e) {
                print("Ошибка записи в файл " + path + ": " + e.getMessage());
            }
        } else {
            print("Файл не существует: " + path);
        }
    }

    private static Optional<GameProgress> openProgress(String pathname) {

        GameProgress gameProgress;

        try (FileInputStream fis = new FileInputStream(pathname);
             ObjectInputStream ois = new ObjectInputStream(fis)){

            gameProgress = (GameProgress) ois.readObject();
        }
        catch (Exception e){
            print("Ошибка десериализации объекта из файла " + pathname + ": " + e.getMessage());
            return Optional.empty();
        }

        return Optional.ofNullable(gameProgress);
    }

    private static void openZip(String pathnameZip, String pathnameRes) {
        crDir(pathnameRes);
        try (FileInputStream fis = new FileInputStream(pathnameZip);
             ZipInputStream zis = new ZipInputStream(fis)){

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                FileOutputStream fos = new FileOutputStream(pathnameRes + "/" + entry.getName());
                for (int c = zis.read(); c != -1; c = zis.read()){
                    fos.write(c);
                }
                fos.flush();

                zis.closeEntry();
                fos.close();
                print("OK - извлечен файл " + pathnameRes + "/" + entry.getName());
            }
        }
        catch (Exception e){
            print("Ошибка распаковки архива " + pathnameZip + ": " + e.getMessage());
        }
    }

    private static void deleteFile(String path) {
        File fileForDel = new File(path);
        if (fileForDel.exists()) {
            if (fileForDel.delete()) print("OK - файл удален: " + path);
            else print("Ошибка при удалении файла: " + path);
        } else print("Файл не найден: " + path);
    }

    private static void zipFiles(String pathOutZip, List<String> listForZip) {
        try(FileOutputStream fos = new FileOutputStream(pathOutZip);
            ZipOutputStream zos = new ZipOutputStream(fos)){

            for (String f : listForZip){
                File fileForZip = new File(f);
                try(FileInputStream fis = new FileInputStream(fileForZip);
                    BufferedInputStream bis = new BufferedInputStream(fis)){

                    ZipEntry entry = new ZipEntry(fileForZip.getName());
                    zos.putNextEntry(entry);

                    int c;
                    while ((c = bis.read()) != -1){
                        zos.write(c);
                    }
                    zos.closeEntry();
                    print("Файл " + fileForZip + " добавлен в архив " + pathOutZip);
                }
                catch (Exception e){
                    print("Ошибка чтения файла " + fileForZip + ": " + e.getMessage());
                }
            }
            print("Создан архив " + pathOutZip);
        }
        catch (Exception e){
            print("Ошибка упаковки архива " + pathOutZip + ": " + e.getMessage());
        }
    }

    private static void saveProgress(Object object, String path){
        try(FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos)){

            oos.writeObject(object);
            print("OK - Успешная запись в " + path);
        }
        catch (Exception e){
            print("ERROR - Ошибка записи в файл " + path + ": " + e.getMessage());
        }
    }

    private static void crDir(String path){
        print("Создание директории " + path + "...");

        File file = new File(path);
        if (file.exists()) {
            printRes("Директория уже существует: " + path);
            return;
        }
        String res = file.mkdir()  ? "OK - Создана директория " + path : "ERROR - Ошибка создания директории " + path;
        printRes(res);
    }

    private static boolean crFile(String path) throws Exception{
        File file = new File(path);
        if (file.exists()) {
            printRes("Файл уже существует: " + path);
            return false;
        }
        return file.createNewFile();
    }

    private static void crFilePrintResult(String path){
        print("Создание файла " + path + "...");
        String res = "";
        try {
            res = crFile(path) ? "OK - Создан файл " + path : "ERROR - Ошибка создания файла " + path;
        }
        catch (Exception e){
            res = "ERROR - Ошибка создания файла "+ path + ": " + e.getMessage();
        }
        finally {
            printRes(res);

        }
    }

    private static void print(String str){
        System.out.println(str);
        log.append(str + "\n");
    }

    private static void printRes(String str){
        print("\t" + str);
    }
}
