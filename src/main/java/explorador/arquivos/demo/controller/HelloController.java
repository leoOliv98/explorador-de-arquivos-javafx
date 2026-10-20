package explorador.arquivos.demo.controller;

import explorador.arquivos.demo.model.Arquivo;
import explorador.arquivos.demo.model.EstatisticasDiscoTask;
import explorador.arquivos.demo.model.No;
import explorador.arquivos.demo.model.Pasta;
import explorador.arquivos.demo.model.SistemaArquivos;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import explorador.arquivos.demo.model.ArvoreDiscoTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class HelloController {

    @FXML
    private TextField txtCaminhoAtual;

    @FXML
    private VBox conteudoPastas;

    @FXML
    private Button btnNovaPasta;

    @FXML
    private Button btnNovoArquivo;

    @FXML
    private Button btnCopiar;

    @FXML
    private Button btnRecortar;

    @FXML
    private Button btnColar;

    @FXML
    private Button btnRenomear;

    @FXML
    private Button btnDeletar;

    private File diretorioAtualDisk = null; // null = "Este Computador"
    private Pasta pastaAtual;
    private SistemaArquivos itemSelecionado;
    private File itemCopiadoOuRecortadoDisk;
    private boolean eModoRecortar = false;

    @FXML
    public void initialize() {
        carregarArquivosDoDisco();
    }

    private void carregarArquivosDoDisco() {
        if (diretorioAtualDisk == null) {
            pastaAtual = new Pasta("Este Computador", null);
            File[] unidades = File.listRoots();

            if (unidades != null) {
                for (File unidade : unidades) {
                    Pasta pastaUnidade = new Pasta(unidade.getPath(), pastaAtual);
                    pastaAtual.adicionarItem(pastaUnidade);
                }
            }
        } else {
            String nomePasta = diretorioAtualDisk.getName().isEmpty() ? diretorioAtualDisk.getPath() : diretorioAtualDisk.getName();
            pastaAtual = new Pasta(nomePasta, null);

            File[] arquivosNoDisco = diretorioAtualDisk.listFiles();

            if (arquivosNoDisco != null) {
                // PRIMEIRO LAÇO: Carrega apenas as pastas
                for (File f : arquivosNoDisco) {
                    if (f.isDirectory()) {
                        Pasta subPasta = new Pasta(f.getName(), pastaAtual);
                        pastaAtual.adicionarItem(subPasta);
                    }
                }

                // SEGUNDO LAÇO: Carrega apenas os arquivos
                for (File f : arquivosNoDisco) {
                    if (f.isFile()) {
                        String nome = f.getName();
                        String ext = nome.contains(".") ? nome.substring(nome.lastIndexOf(".") + 1) : "";

                        double bytes = f.length();
                        double tamanhoKB = bytes / 1024.0;
                        if (bytes > 0 && tamanhoKB < 0.01) {
                            tamanhoKB = 0.01;
                        }

                        Arquivo arq = new Arquivo(nome, ext, tamanhoKB);
                        pastaAtual.adicionarItem(arq);
                    }
                }
            }
        }
        atualizarInterface();
    }

    private void atualizarInterface() {
        if (diretorioAtualDisk == null) {
            txtCaminhoAtual.setText("Este Computador");
        } else {
            txtCaminhoAtual.setText(diretorioAtualDisk.getAbsolutePath());
        }

        conteudoPastas.getChildren().clear();
        itemSelecionado = null;

        if (diretorioAtualDisk != null) {
            HBox linhaVoltar = criarLinhaVoltar();
            conteudoPastas.getChildren().add(linhaVoltar);
        }

        No<SistemaArquivos> noAtual = pastaAtual.getItens().getInicio();

        while (noAtual != null) {
            SistemaArquivos item = noAtual.getDado();
            HBox linhaItem = criarLinhaItem(item);
            conteudoPastas.getChildren().add(linhaItem);

            noAtual = noAtual.getProximo();
        }
    }

    private HBox criarLinhaItem(SistemaArquivos item) {
        HBox linha = new HBox(10);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.setStyle("-fx-padding: 8; -fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");

        String icone = (diretorioAtualDisk == null) ? "💽 " : ((item instanceof Pasta) ? "📁 " : "📄 ");
        Label lblNome = new Label(icone + item.getNome());
        lblNome.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        HBox.setHgrow(lblNome, Priority.ALWAYS);

        // Adiciona apenas o ícone e o nome à linha, removendo o lblTamanho
        linha.getChildren().add(lblNome);

        linha.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                if (item instanceof Pasta) {
                    if (diretorioAtualDisk == null) {
                        diretorioAtualDisk = new File(item.getNome());
                    } else {
                        diretorioAtualDisk = new File(diretorioAtualDisk, item.getNome());
                    }
                    carregarArquivosDoDisco();
                }
            } else {
                selecionarLinha(linha, item);
            }
        });

        return linha;
    }

    private HBox criarLinhaVoltar() {
        HBox linha = new HBox(10);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.setStyle("-fx-padding: 8; -fx-background-color: #e0e0e0; -fx-cursor: hand; -fx-border-radius: 4; -fx-background-radius: 4;");

        Label lblVoltar = new Label("⬅️ .. (Voltar)");
        lblVoltar.setStyle("-fx-font-weight: bold;");
        linha.getChildren().add(lblVoltar);

        linha.setOnMouseClicked(event -> {
            if (diretorioAtualDisk != null) {
                diretorioAtualDisk = diretorioAtualDisk.getParentFile();
                carregarArquivosDoDisco();
            }
        });

        return linha;
    }

    private void selecionarLinha(HBox linha, SistemaArquivos item) {
        conteudoPastas.getChildren().forEach(child -> {
            if (child != linha && child.getStyle().contains("#cce5ff")) {
                child.setStyle("-fx-padding: 8; -fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
            }
        });

        linha.setStyle("-fx-padding: 8; -fx-background-color: #cce5ff; -fx-border-color: #0066cc; -fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand;");
        this.itemSelecionado = item;
    }

    @FXML
    protected void onNovaPastaClick() {
        if (diretorioAtualDisk == null) {
            exibirAlerta("Aviso", "Selecione uma unidade de disco para criar uma pasta.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("Nova Pasta");
        dialog.setTitle("Criar Pasta");
        dialog.setHeaderText("Digite o nome da nova pasta:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(nome -> {
            if (!nome.trim().isEmpty()) {
                File novaPasta = new File(diretorioAtualDisk, nome.trim());
                if (novaPasta.mkdir()) {
                    carregarArquivosDoDisco();
                } else {
                    exibirAlerta("Erro", "Não foi possível criar a pasta.");
                }
            }
        });
    }

    @FXML
    protected void onNovoArquivoClick() {
        if (diretorioAtualDisk == null) {
            exibirAlerta("Aviso", "Selecione uma unidade de disco para criar um arquivo.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("novo_documento.txt");
        dialog.setTitle("Criar Arquivo");
        dialog.setHeaderText("Digite o nome do novo arquivo:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(nome -> {
            if (!nome.trim().isEmpty()) {
                File novoArquivo = new File(diretorioAtualDisk, nome.trim());
                try {
                    if (novoArquivo.createNewFile()) {
                        carregarArquivosDoDisco();
                    } else {
                        exibirAlerta("Aviso", "O arquivo já existe.");
                    }
                } catch (IOException e) {
                    exibirAlerta("Erro", "Erro ao criar arquivo: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    protected void onDeletarClick() {
        if (diretorioAtualDisk == null) {
            exibirAlerta("Aviso", "Não é possível deletar unidades de disco em 'Este Computador'.");
            return;
        }

        if (itemSelecionado == null) {
            exibirAlerta("Aviso", "Selecione um item para deletar.");
            return;
        }

        File arquivoParaDeletar = new File(diretorioAtualDisk, itemSelecionado.getNome());

        if (arquivoParaDeletar.exists()) {
            if (deletarRecursivo(arquivoParaDeletar)) {
                carregarArquivosDoDisco();
            } else {
                exibirAlerta("Erro", "Não foi possível deletar o item.");
            }
        }
    }

    private boolean deletarRecursivo(File f) {
        if (f.isDirectory()) {
            File[] sub = f.listFiles();
            if (sub != null) {
                for (File c : sub) {
                    deletarRecursivo(c);
                }
            }
        }
        return f.delete();
    }

    @FXML
    protected void onRenomearClick() {
        if (diretorioAtualDisk == null) {
            exibirAlerta("Aviso", "Não é possível renomear discos em 'Este Computador'.");
            return;
        }

        if (itemSelecionado == null) {
            exibirAlerta("Aviso", "Selecione um item para renomear.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(itemSelecionado.getNome());
        dialog.setTitle("Renomear Item");
        dialog.setHeaderText("Digite o novo nome:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(novoNome -> {
            if (!novoNome.trim().isEmpty()) {
                File antigo = new File(diretorioAtualDisk, itemSelecionado.getNome());
                File novo = new File(diretorioAtualDisk, novoNome.trim());

                if (antigo.renameTo(novo)) {
                    carregarArquivosDoDisco();
                } else {
                    exibirAlerta("Erro", "Não foi possível renomear o item.");
                }
            }
        });
    }

    @FXML
    protected void onCopiarClick() {
        if (diretorioAtualDisk == null || itemSelecionado == null) {
            exibirAlerta("Aviso", "Selecione um item válido para copiar.");
            return;
        }
        itemCopiadoOuRecortadoDisk = new File(diretorioAtualDisk, itemSelecionado.getNome());
        eModoRecortar = false;
    }

    @FXML
    protected void onRecortarClick() {
        if (diretorioAtualDisk == null || itemSelecionado == null) {
            exibirAlerta("Aviso", "Selecione um item válido para recortar.");
            return;
        }
        itemCopiadoOuRecortadoDisk = new File(diretorioAtualDisk, itemSelecionado.getNome());
        eModoRecortar = true;
    }

    @FXML
    protected void onColarClick() {
        if (diretorioAtualDisk == null) {
            exibirAlerta("Aviso", "Entre em um disco ou pasta para colar o item.");
            return;
        }

        if (itemCopiadoOuRecortadoDisk == null || !itemCopiadoOuRecortadoDisk.exists()) {
            exibirAlerta("Aviso", "Nenhum item foi copiado ou recortado.");
            return;
        }

        File destino = new File(diretorioAtualDisk, itemCopiadoOuRecortadoDisk.getName());

        try {
            if (eModoRecortar) {
                Files.move(itemCopiadoOuRecortadoDisk.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                itemCopiadoOuRecortadoDisk = null;
            } else {
                if (itemCopiadoOuRecortadoDisk.isDirectory()) {
                    copiarDiretorioRecursivo(itemCopiadoOuRecortadoDisk, destino);
                } else {
                    Files.copy(itemCopiadoOuRecortadoDisk.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            carregarArquivosDoDisco();
        } catch (IOException e) {
            exibirAlerta("Erro", "Erro ao colar item: " + e.getMessage());
        }
    }

    private void copiarDiretorioRecursivo(File origem, File destino) throws IOException {
        if (origem.isDirectory()) {
            if (!destino.exists()) {
                destino.mkdir();
            }
            String[] arquivos = origem.list();
            if (arquivos != null) {
                for (String file : arquivos) {
                    File srcFile = new File(origem, file);
                    File destFile = new File(destino, file);
                    copiarDiretorioRecursivo(srcFile, destFile);
                }
            }
        } else {
            Files.copy(origem.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @FXML
    protected void onVerArvoreClick() {
        if (diretorioAtualDisk == null) {
            exibirAlerta("Aviso", "Entre em um disco específico antes de ver a árvore.");
            return;
        }

        // Janela de progresso para evitar travamento da UI
        Alert progressoAlert = new Alert(Alert.AlertType.INFORMATION);
        progressoAlert.setTitle("A gerar árvore...");
        progressoAlert.setHeaderText("A mapear estrutura de diretórios em segundo plano...");
        progressoAlert.setContentText("A aguardar...");

        ArvoreDiscoTask task = new ArvoreDiscoTask(diretorioAtualDisk);

        // Vincula a mensagem da Task ao texto do Alerta em tempo real
        progressoAlert.contentTextProperty().bind(task.messageProperty());

        // Quando a árvore estiver totalmente gerada com sucesso
        task.setOnSucceeded(event -> {
            progressoAlert.close();
            String arvoreTexto = task.getValue();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Estrutura Hierárquica");
            alert.setHeaderText("Árvore Completa (Recursiva):");

            TextArea textArea = new TextArea(arvoreTexto);
            textArea.setEditable(false);
            textArea.setWrapText(false);
            textArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 13px;");
            textArea.setPrefWidth(520);
            textArea.setPrefHeight(380);

            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
        });

        // Se ocorrer falha
        task.setOnFailed(event -> {
            progressoAlert.close();
            exibirAlerta("Erro", "Ocorreu um erro ao gerar a árvore do diretório.");
        });

        progressoAlert.show();

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    protected void onVerPropriedadesClick() {
        // Verifica se há um item selecionado (clicado uma vez)
        if (itemSelecionado == null) {
            exibirAlerta("Aviso", "Selecione um item (clique uma vez) para ver suas propriedades.");
            return;
        }

        File arquivoSelecionado;
        if (diretorioAtualDisk == null) {
            arquivoSelecionado = new File(itemSelecionado.getNome());
        } else {
            arquivoSelecionado = new File(diretorioAtualDisk, itemSelecionado.getNome());
        }

        if (!arquivoSelecionado.exists()) {
            exibirAlerta("Erro", "O item selecionado não está mais disponível no disco.");
            return;
        }

        boolean isDiretorio = arquivoSelecionado.isDirectory();
        String tipoItem = isDiretorio ? "📁 Pasta" : "📄 Arquivo";

        // Coleta as datas rapidamente
        String textoCriacao = "Desconhecida";
        String textoModificacao = "Desconhecida";
        try {
            java.nio.file.attribute.BasicFileAttributes attr = java.nio.file.Files.readAttributes(
                    arquivoSelecionado.toPath(),
                    java.nio.file.attribute.BasicFileAttributes.class
            );
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            textoCriacao = java.time.LocalDateTime.ofInstant(attr.creationTime().toInstant(), java.time.ZoneId.systemDefault()).format(formatter);
            textoModificacao = java.time.LocalDateTime.ofInstant(attr.lastModifiedTime().toInstant(), java.time.ZoneId.systemDefault()).format(formatter);
        } catch (Exception ignored) {} // Ignora erros de permissão em arquivos do sistema

        final String finalCriacao = textoCriacao;
        final String finalModificacao = textoModificacao;

        // Se for Pasta: Executa a Task em segundo plano para contar os itens e calcular tamanho
        if (isDiretorio) {
            Alert progressoAlert = new Alert(Alert.AlertType.INFORMATION);
            progressoAlert.setTitle("A calcular propriedades...");
            progressoAlert.setHeaderText("A varrer o conteúdo da pasta em segundo plano...");
            progressoAlert.setContentText("A calcular, por favor aguarde...");

            // Inicia a task focada apenas no diretório selecionado
            EstatisticasDiscoTask task = new EstatisticasDiscoTask(arquivoSelecionado);
            progressoAlert.contentTextProperty().bind(task.messageProperty());

            task.setOnSucceeded(event -> {
                progressoAlert.close();
                EstatisticasDiscoTask.EstatisticasResultado stats = task.getValue();

                // Desconta 1 do total de pastas para não contar a própria pasta raiz selecionada
                long qtdPastas = stats.totalPastas > 0 ? stats.totalPastas - 1 : 0;

                String mensagem = String.format(
                        "Nome: %s\nTipo: %s\nCaminho: %s\n\n📅 Criação: %s\n🕒 Modificação: %s\n\n" +
                                "--- Estatísticas do Conteúdo ---\n📁 Pastas contidas: %d\n📄 Arquivos contidos: %d\n💾 Tamanho Total: %s",
                        itemSelecionado.getNome(),
                        tipoItem,
                        arquivoSelecionado.getAbsolutePath(),
                        finalCriacao,
                        finalModificacao,
                        qtdPastas,
                        stats.totalFicheiros,
                        stats.formatarTamanho()
                );
                exibirAlerta("Propriedades da Pasta", mensagem);
            });

            task.setOnFailed(event -> {
                progressoAlert.close();
                exibirAlerta("Erro", "Ocorreu um erro ao calcular as estatísticas da pasta.");
            });

            progressoAlert.show();
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        } else {
            // Se for Arquivo: Calcula o tamanho instantaneamente sem precisar de Task
            long tamanhoBytes = arquivoSelecionado.length();
            String tamanhoFormatado;

            double mb = tamanhoBytes / (1024.0 * 1024.0);
            if (mb < 1) {
                tamanhoFormatado = String.format("%.2f KB", tamanhoBytes / 1024.0);
            } else if (mb < 1024) {
                tamanhoFormatado = String.format("%.2f MB", mb);
            } else {
                tamanhoFormatado = String.format("%.2f GB", mb / 1024.0);
            }

            String mensagem = String.format(
                    "Nome: %s\nTipo: %s\nCaminho: %s\n\n📅 Criação: %s\n🕒 Modificação: %s\n\n💾 Tamanho: %s",
                    itemSelecionado.getNome(),
                    tipoItem,
                    arquivoSelecionado.getAbsolutePath(),
                    finalCriacao,
                    finalModificacao,
                    tamanhoFormatado
            );
            exibirAlerta("Propriedades do Arquivo", mensagem);
        }
    }

    private void exibirAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}