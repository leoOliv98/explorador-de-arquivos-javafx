package explorador.arquivos.demo.model;

import javafx.concurrent.Task;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class EstatisticasDiscoTask extends Task<EstatisticasDiscoTask.EstatisticasResultado> {

    private final File diretorioRaiz;

    public static class EstatisticasResultado {
        public long totalFicheiros = 0;
        public long totalPastas = 0;
        public long tamanhoTotalBytes = 0;

        public String formatarTamanho() {
            double mb = tamanhoTotalBytes / (1024.0 * 1024.0);
            if (mb < 1024) {
                return String.format("%.2f MB", mb);
            } else {
                return String.format("%.2f GB", mb / 1024.0);
            }
        }
    }

    public EstatisticasDiscoTask(File diretorioRaiz) {
        this.diretorioRaiz = diretorioRaiz;
    }

    @Override
    protected EstatisticasResultado call() throws Exception {
        EstatisticasResultado stats = new EstatisticasResultado();

        if (diretorioRaiz == null || !diretorioRaiz.exists()) {
            return stats;
        }

        try {
            Files.walkFileTree(diretorioRaiz.toPath(), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (isCancelled()) {
                        return FileVisitResult.TERMINATE;
                    }
                    stats.totalPastas++;
                    try {
                        updateMessage("A analisar: " + dir.getFileName());
                    } catch (Exception ignored) {}

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (isCancelled()) {
                        return FileVisitResult.TERMINATE;
                    }
                    stats.totalFicheiros++;
                    stats.tamanhoTotalBytes += attrs.size();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    // Ignora ficheiros/pastas do sistema bloqueados (ex: System Volume Information)
                    // e continua a varredura sem quebrar a task.
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            updateMessage("Aviso: Algumas pastas foram ignoradas devido a permissões.");
        }

        return stats;
    }
}