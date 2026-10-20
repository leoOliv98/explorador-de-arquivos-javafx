package explorador.arquivos.demo.model;

import java.io.File;

public class Pasta extends SistemaArquivos {
    private Pasta pastaPai;
    private ListaDuplamenteEncadeada<SistemaArquivos> itens;

    public Pasta(String nome, Pasta pastaPai) {
        super(nome);
        this.pastaPai = pastaPai;
        this.itens = new ListaDuplamenteEncadeada<>();
    }

    public Pasta getPastaPai() {
        return pastaPai;
    }

    public ListaDuplamenteEncadeada<SistemaArquivos> getItens() {
        return itens;
    }

    public void adicionarItem(SistemaArquivos item) {
        itens.adicionar(item);
    }

    public void removerItem(SistemaArquivos item) {
        itens.remover(item);
    }

    @Override
    public double getTamanho() {
        double total = 0;
        No<SistemaArquivos> atual = itens.getInicio();
        while (atual != null) {
            total += atual.getDado().getTamanho();
            atual = atual.getProximo();
        }
        return total;
    }

    public static String gerarArvoreDiscoRecursiva(File diretorio, String indentacao) {
        StringBuilder sb = new StringBuilder();
        if (diretorio == null || !diretorio.exists()) return "";

        sb.append(indentacao).append("📁 ").append(diretorio.getName().isEmpty() ? diretorio.getPath() : diretorio.getName()).append("\n");

        File[] arquivos = diretorio.listFiles();
        if (arquivos != null) {
            for (File f : arquivos) {
                if (f.isDirectory()) {
                    // CHAMADA RECURSIVA para subdiretórios
                    sb.append(gerarArvoreDiscoRecursiva(f, indentacao + "   │  "));
                } else if (f.isFile()) {
                    sb.append(indentacao).append("   ├── 📄 ").append(f.getName()).append("\n");
                }
            }
        }
        return sb.toString();
    }

    public static class Estatisticas {
        public int quantidadePastas = 0;
        public int quantidadeArquivos = 0;
        public double tamanhoTotalBytes = 0;

        public String formatarTamanho() {
            double kb = tamanhoTotalBytes / 1024.0;
            if (kb < 1024) {
                return String.format("%.2f KB", kb);
            } else {
                return String.format("%.2f MB", kb / 1024.0);
            }
        }
    }

    public static Estatisticas calcularEstatisticasRecursivas(File diretorio) {
        Estatisticas stats = new Estatisticas();
        if (diretorio == null || !diretorio.exists()) return stats;

        File[] arquivos = diretorio.listFiles();
        if (arquivos != null) {
            for (File f : arquivos) {
                if (f.isDirectory()) {
                    stats.quantidadePastas++;
                    // chamada recursiva para subpastas
                    Estatisticas subStats = calcularEstatisticasRecursivas(f);
                    stats.quantidadePastas += subStats.quantidadePastas;
                    stats.quantidadeArquivos += subStats.quantidadeArquivos;
                    stats.tamanhoTotalBytes += subStats.tamanhoTotalBytes;
                } else if (f.isFile()) {
                    stats.quantidadeArquivos++;
                    stats.tamanhoTotalBytes += f.length();
                }
            }
        }
        return stats;
    }
}