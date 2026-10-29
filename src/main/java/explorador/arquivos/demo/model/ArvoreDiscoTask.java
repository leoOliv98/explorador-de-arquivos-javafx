package explorador.arquivos.demo.model;

import javafx.concurrent.Task;
import java.io.File;

public class ArvoreDiscoTask extends Task<String> {

    private final File diretorioRaiz;
    private final int profundidadeMaxima;

    // Construtor com limite de profundidade personalizado
    public ArvoreDiscoTask(File diretorioRaiz, int profundidadeMaxima) {
        this.diretorioRaiz = diretorioRaiz;
        this.profundidadeMaxima = profundidadeMaxima;
    }

    // Construtor padrão (agora configurado para percorrer TUDO, sem limite de profundidade)
    public ArvoreDiscoTask(File diretorioRaiz) {
        this(diretorioRaiz, Integer.MAX_VALUE);
    }

    @Override
    protected String call() throws Exception {
        updateMessage("Iniciando varredura...");
        StringBuilder sb = new StringBuilder();

        // Nível 0 = a pasta selecionada / disco
        sb.append("📁 ").append(diretorioRaiz.getAbsolutePath()).append("\n");

        construirArvore(diretorioRaiz, sb, "", 1);

        updateMessage("Concluído!");
        return sb.toString();
    }

    private void construirArvore(File pastaAtual, StringBuilder sb, String prefixo, int nivelAtual) {
        if (isCancelled()) {
            return;
        }

        updateMessage("Lendo: " + pastaAtual.getName());

        File[] arquivos = pastaAtual.listFiles();
        if (arquivos == null) {
            return; // Pastas de sistema sem permissão ou vazias
        }

        // Primeiro lista as pastas, depois os arquivos
        for (int i = 0; i < arquivos.length; i++) {
            File f = arquivos[i];

            // CORRIGIDO O SINAL DE MENOS QUE ESTAVA AQUI:
            boolean ehUltimo = (i == arquivos.length - 1);

            String marcador = ehUltimo ? "└── " : "├── ";
            String novoPrefixo = prefixo + (ehUltimo ? "    " : "│   ");

            if (f.isDirectory()) {
                sb.append(prefixo).append(marcador).append("📁 ").append(f.getName()).append("\n");

                // SÓ DESCE NA RECURSÃO SE AINDA NÃO ATINGIU O LIMITE DE NÍVEIS
                if (nivelAtual < profundidadeMaxima) {
                    construirArvore(f, sb, novoPrefixo, nivelAtual + 1);
                } else {
                    // Indica visualmente que a pasta tem conteúdo que não foi aberto pelo limite
                    File[] subArquivos = f.listFiles();
                    if (subArquivos != null && subArquivos.length > 0) {
                        sb.append(novoPrefixo).append("└── [ ... ]\n");
                    }
                }
            } else {
                sb.append(prefixo).append(marcador).append("📄 ").append(f.getName()).append("\n");
            }
        }
    }
}