package explorador.arquivos.demo.model;

import javafx.concurrent.Task;
import java.io.File;

public class ArvoreDiscoTask extends Task<String> {

    private final File diretorioRaiz;

    public ArvoreDiscoTask(File diretorioRaiz) {
        this.diretorioRaiz = diretorioRaiz;
    }

    @Override
    protected String call() throws Exception {
        if (diretorioRaiz == null || !diretorioRaiz.exists()) {
            return "";
        }
        return gerarArvoreDiscoRecursivaComProgresso(diretorioRaiz, "");
    }

    private String gerarArvoreDiscoRecursivaComProgresso(File diretorio, String indentacao) {
        if (isCancelled()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        if (diretorio == null || !diretorio.exists()) return "";

        // Atualiza a mensagem na UI em tempo real com a pasta atual a ser varrida
        updateMessage("A mapear: " + (diretorio.getName().isEmpty() ? diretorio.getPath() : diretorio.getName()));

        sb.append(indentacao).append("📁 ").append(diretorio.getName().isEmpty() ? diretorio.getPath() : diretorio.getName()).append("\n");

        File[] arquivos = diretorio.listFiles();
        if (arquivos != null) {
            for (File f : arquivos) {
                if (isCancelled()) {
                    break;
                }

                try {
                    if (f.isDirectory()) {
                        // chamada recursiva para subdiretórios
                        sb.append(gerarArvoreDiscoRecursivaComProgresso(f, indentacao + "   │  "));
                    } else if (f.isFile()) {
                        sb.append(indentacao).append("   ├── 📄 ").append(f.getName()).append("\n");
                    }
                } catch (RuntimeException e) {
                    // Ignora pastas/ficheiros sem permissão de leitura
                }
            }
        }
        return sb.toString();
    }
}