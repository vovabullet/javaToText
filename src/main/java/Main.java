package main.java;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.nio.file.Files;
import java.util.Objects;


public class Main extends Application {

    private TextField pathField = new TextField();
    private TextArea consoleOutput = new TextArea(); // Окно для логов
    private Path outputFile;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JavaToText");

        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(10);

        // Инпут
        pathField.setPromptText("ввести...");
        pathField.setStyle("-fx-prompt-text-fill: #666767;");
        pathField.setOnMouseClicked(e -> pathField.setStyle("-fx-border-color: #e1e1e1;"));

        // Картинка
        ImageView imageView = new ImageView(new Image("file:src/main/resources/icon.png"));
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);

        // Кнопка для выбора папки через проводник
        Button browseButton = new Button("выбрать");
        browseButton.setOnAction(e -> chooseDirectory(primaryStage));

        // Кнопка для подтверждения введённого вручную пути
        Button confirmButton = new Button("применить");
        confirmButton.setOnAction(e -> {
            String pathStr = pathField.getText().trim();
            if (!pathStr.isEmpty()) {
                Path manualPath = Paths.get(pathStr);
                if (Files.isDirectory(manualPath)) {
                    processFiles(manualPath);
                } else {
                    System.out.println("указанный путь не является директорией.");
                }
            } else {
                System.out.println("путь не введён.");
            }
        });

        // Кнопка для копирования содержимого файла в буфер обмена
        Button copyButton = new Button("копировать");
        copyButton.setOnAction(e -> copyFileContent());

        // Контейнер для кнопок
        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setSpacing(10);

        // Настройка консоли
        consoleOutput.setEditable(false);
        consoleOutput.setWrapText(true);
        consoleOutput.setPrefHeight(150);

        // Перенаправление System.out в TextArea с корректной кодировкой UTF-8
        OutputStream outputStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                Platform.runLater(() -> consoleOutput.appendText(String.valueOf((char) b)));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                String text = new String(b, off, len, StandardCharsets.UTF_8);
                Platform.runLater(() -> consoleOutput.appendText(text));
            }
        };

        PrintStream printStream = new PrintStream(outputStream, true, StandardCharsets.UTF_8);
        System.setOut(printStream);
        System.setErr(printStream);

        buttonContainer.getChildren().addAll(browseButton, confirmButton, copyButton); // все кнопки в контейнер для кнопок
        root.getChildren().addAll(imageView, pathField, buttonContainer, consoleOutput); // все объекты в основной контейнер
        Scene scene = new Scene(root, 700, 400);

        // Загрузка CSS из classpath
        try {
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        } catch (Exception ex) {
            System.out.println("не удалось загрузить CSS файл: " + ex.getMessage());
        }

        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println("добро пожаловать!");
        System.out.println("кнопка 'выбрать' для указания директории в проводнике.");
        System.out.println("кнопка 'применить' для подтверждения ручного ввода пути.");
        System.out.println("кнопка 'копировать' для копирования содержимого последнего созданного файла.");
    }

    // Метод для выбора директории через DirectoryChooser
    private void chooseDirectory(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("выберите папку");
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            pathField.setText(selectedDirectory.getAbsolutePath());
            processFiles(selectedDirectory.toPath());
        } else {
            System.out.println("директория не выбрана.");
        }
    }

    // Метод для обработки файлов в выбранной директории
    private void processFiles(Path startPath) {
        try {
            String downloadsPath = System.getProperty("user.home") + "\\Downloads\\";
            String formattedDateTime = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy.HH.mm.ss"));
            outputFile = Paths.get(downloadsPath + "JtT_" + formattedDateTime + ".txt");
            Files.walkFileTree(startPath, new FileSaver(outputFile));
            System.out.println("готово! все файлы сохранены в " + outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для копирования содержимого файла в буфер обмена
    private void copyFileContent() {
        if (outputFile != null && Files.exists(outputFile)) {
            try {
                String content = Files.readString(outputFile, StandardCharsets.UTF_8);
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(content), null);
                System.out.println("текст скопирован в буфер обмена!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("файл не найден!");
        }
    }
}