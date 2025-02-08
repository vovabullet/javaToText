package main.java;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileSaver extends SimpleFileVisitor<Path> {

    private Path outputFile;

    public FileSaver(Path outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        // Обрабатываем только файлы с расширением .java
        if (file.toString().endsWith(".java")) {
            String content = Files.readString(file);
            Files.writeString(outputFile, content + "\n\n", StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        return FileVisitResult.CONTINUE;
    }
}
