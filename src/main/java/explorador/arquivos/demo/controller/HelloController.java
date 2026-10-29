package explorador.arquivos.demo.controller;

import explorador.arquivos.demo.controller.TelinhaController;
import explorador.arquivos.demo.model.Arquivo;
import explorador.arquivos.demo.model.EstatisticasDiscoTask;
import explorador.arquivos.demo.model.No;
import explorador.arquivos.demo.model.Pasta;
import explorador.arquivos.demo.model.SistemaArquivos;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import explorador.arquivos.demo.model.ArvoreDiscoTask;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;

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
    private TextField txtPesquisa;

    @FXML
    private Button btnNovaPasta;

    @FXML
    private Button btnArvore1;

    @FXML
    private Button btnArvore2;

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

    @FXML
    private Button btnArvore; // Referência necessária para fixar o modal na janela atual

    private File diretorioAtualDisk = null; // null = "Este Computador"
    private Pasta pastaAtual;
    private SistemaArquivos itemSelecionado;
    private File itemCopiadoOuRecortadoDisk;
    private boolean eModoRecortar = false;

    @FXML
    public void initialize() {
        carregarArquivosDoDisco();

        // Listener que escuta cada caractere digitado na caixa de texto
        if (txtPesquisa != null) {
            txtPesquisa.textProperty().addListener((observable, valorAntigo, valorNovo) -> {
                atualizarInterface(valorNovo); // Redesenha a tela aplicando o filtro
            });
        }
    }

    private File obterNomeUnico(File diretorioPai, String nomeOriginal) {
        File arquivoFinal = new File(diretorioPai, nomeOriginal);

        if (!arquivoFinal.exists()) {
            return arquivoFinal;
        }

        String nomeBase = nomeOriginal;
        String extensao = "";

        int pontoIndex = nomeOriginal.lastIndexOf(".");
        if (pontoIndex > 0) {
            nomeBase = nomeOriginal.substring(0, pontoIndex);
            extensao = nomeOriginal.substring(pontoIndex);
        }

        int contador = 1;
        while (arquivoFinal.exists()) {
            String novoNome = String.format("%s (%d)%s", nomeBase, contador, extensao);
            arquivoFinal = new File(diretorioPai, novoNome);
            contador++;
        }

        return arquivoFinal;
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
                for (File f : arquivosNoDisco) {
                    if (f.isDirectory()) {
                        Pasta subPasta = new Pasta(f.getName(), pastaAtual);
                        pastaAtual.adicionarItem(subPasta);
                    }
                }

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
        if (txtPesquisa != null) {
            txtPesquisa.setText(""); // Limpa a pesquisa ao mudar de pasta
        }
        atualizarInterface("");
    }

    // Nova versão que aplica o filtro no nome dos arquivos
    private void atualizarInterface(String filtro) {
        if (diretorioAtualDisk == null) {
            txtCaminhoAtual.setText("Este Computador");
        } else {
            txtCaminhoAtual.setText(diretorioAtualDisk.getAbsolutePath());
        }

        conteudoPastas.getChildren().clear();
        itemSelecionado = null;

        // Botão voltar (sempre exibido se não estivermos na raiz do disco)
        if (diretorioAtualDisk != null) {
            HBox linhaVoltar = criarLinhaVoltar();
            conteudoPastas.getChildren().add(linhaVoltar);
        }

        No<SistemaArquivos> noAtual = pastaAtual.getItens().getInicio();

        while (noAtual != null) {
            SistemaArquivos item = noAtual.getDado();

            // Lógica do filtro em tempo real (ignorando letras maiúsculas/minúsculas)
            boolean atendeFiltro = filtro == null || filtro.trim().isEmpty() ||
                    item.getNome().toLowerCase().contains(filtro.toLowerCase());

            if (atendeFiltro) {
                HBox linhaItem = criarLinhaItem(item);
                conteudoPastas.getChildren().add(linhaItem);
            }

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
                File novaPasta = obterNomeUnico(diretorioAtualDisk, nome.trim());
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
                File novoArquivo = obterNomeUnico(diretorioAtualDisk, nome.trim());
                try {
                    if (novoArquivo.createNewFile()) {
                        carregarArquivosDoDisco();
                    } else {
                        exibirAlerta("Erro", "Não foi possível criar o arquivo.");
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

        File destino = obterNomeUnico(diretorioAtualDisk, itemCopiadoOuRecortadoDisk.getName());

        try {
            if (eModoRecortar) {
                if (itemCopiadoOuRecortadoDisk.equals(destino)) {
                    return;
                }
                Files.move(itemCopiadoOuRecortadoDisk.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                itemCopiadoOuRecortadoDisk = null;
            } else {
                if (itemCopiadoOuRecortadoDisk.isDirectory()) {
                    copiarDiretorioRecursivo(itemCopiadoOuRecortadoDisk, destino);
                } else {
                    Files.copy(itemCopiadoOuRecortadoDisk.toPath(), destino.toPath());
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

    // AGORA O BOTÃO PRINCIPAL ABRE A TELINHA COM AS DUAS OPÇÕES
    @FXML
    protected void onVerArvoreClick(ActionEvent event) {
        if (diretorioAtualDisk == null) {
            exibirAlerta("Aviso", "Entre em um disco específico antes de ver a árvore.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/explorador/arquivos/demo/telaArvore.fxml"));

            // 1. Carrega o FXML primeiro (o JavaFX cria a TelinhaController sozinho usando o FXML)
            Parent root = loader.load();

            // 2. Obtém a instância que o JavaFX criou
            TelinhaController telinhaController = loader.getController();

            // 3. Injeta a referência do HelloController atual
            telinhaController.setControllerPrincipal(this);

            Stage modalStage = new Stage();
            modalStage.setTitle("Selecione uma Opção");
            modalStage.setScene(new Scene(root));
            modalStage.initModality(Modality.APPLICATION_MODAL);

            if (btnArvore != null && btnArvore.getScene() != null) {
                modalStage.initOwner(btnArvore.getScene().getWindow());
            }

            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            exibirAlerta("Erro", "Não foi possível abrir a janela de opções.");
        }
    }

    // MÉTODO 1: Árvore Limitada a 2 Níveis
    public void executarArvoreParcial() { // ou renomeie para executarArvoreLimitada se preferir
        Alert progressoAlert = new Alert(Alert.AlertType.INFORMATION);
        progressoAlert.setTitle("A gerar árvore...");
        progressoAlert.setHeaderText("A mapear estrutura até 2 níveis...");
        progressoAlert.setContentText("A aguardar...");

        int profundidadeMaxima = 2;
        // Passa o diretório e o limite 2
        ArvoreDiscoTask task = new ArvoreDiscoTask(diretorioAtualDisk, profundidadeMaxima);

        progressoAlert.contentTextProperty().bind(task.messageProperty());

        task.setOnSucceeded(event -> {
            progressoAlert.close();
            String arvoreTexto = task.getValue();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Estrutura Hierárquica");
            alert.setHeaderText("Árvore de Diretórios (Limitada a 2 Níveis):");

            TextArea textArea = new TextArea(arvoreTexto);
            textArea.setEditable(false);
            textArea.setWrapText(false);
            textArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 13px;");
            textArea.setPrefWidth(550);
            textArea.setPrefHeight(400);

            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
        });

        task.setOnFailed(event -> {
            progressoAlert.close();
            exibirAlerta("Erro", "Ocorreu um erro ao gerar a árvore do diretório.");
        });

        progressoAlert.show();

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // MÉTODO 2: Árvore Completa (Percorre Tudo)
    public void executarArvoreCompleta() { // ou renomeie para executarArvoreCompleta
        Alert progressoAlert = new Alert(Alert.AlertType.INFORMATION);
        progressoAlert.setTitle("A gerar árvore...");
        progressoAlert.setHeaderText("A mapear estrutura completa (Tudo)...");
        progressoAlert.setContentText("A aguardar...");

        // Se o seu construtor padrão percorre tudo, basta chamá-lo:
        ArvoreDiscoTask task = new ArvoreDiscoTask(diretorioAtualDisk);

        // OU, se a sua task exige um número e você quiser passar "infinito",
        // use Integer.MAX_VALUE: ArvoreDiscoTask task = new ArvoreDiscoTask(diretorioAtualDisk, Integer.MAX_VALUE);

        progressoAlert.contentTextProperty().bind(task.messageProperty());

        task.setOnSucceeded(event -> {
            progressoAlert.close();
            String arvoreTexto = task.getValue();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Estrutura Hierárquica");
            alert.setHeaderText("Árvore de Diretórios (Completa / Tudo):");

            TextArea textArea = new TextArea(arvoreTexto);
            textArea.setEditable(false);
            textArea.setWrapText(false);
            textArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 13px;");
            textArea.setPrefWidth(550);
            textArea.setPrefHeight(400);

            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
        });

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
        } catch (Exception ignored) {}

        final String finalCriacao = textoCriacao;
        final String finalModificacao = textoModificacao;

        if (isDiretorio) {
            Alert progressoAlert = new Alert(Alert.AlertType.INFORMATION);
            progressoAlert.setTitle("A calcular propriedades...");
            progressoAlert.setHeaderText("A varrer o conteúdo da pasta em segundo plano...");
            progressoAlert.setContentText("A calcular, por favor aguarde...");

            EstatisticasDiscoTask task = new EstatisticasDiscoTask(arquivoSelecionado);
            progressoAlert.contentTextProperty().bind(task.messageProperty());

            task.setOnSucceeded(event -> {
                progressoAlert.close();
                EstatisticasDiscoTask.EstatisticasResultado stats = task.getValue();

                long qtdPastas = stats.totalPastas > 0 ? stats.totalPastas - 1 : 0; // CORRIGIDO O SINAL DE MENOS

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
            long tamanhoBytes = arquivoSelecionado.length();
            String tamanhoFormatado;

            double mb = tamanhoBytes / (1024.0 * 1024.0); // CORRIGIDO O SINAL DE MULTIPLICAÇÃO
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