package explorador.arquivos.demo.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

public class TelinhaController {

    private HelloController controllerPrincipal;

    public void setControllerPrincipal(HelloController controllerPrincipal) {
        this.controllerPrincipal = controllerPrincipal;
    }

    @FXML
    public void onOpcaoUmClick(ActionEvent event) {
        if (controllerPrincipal != null) {
            controllerPrincipal.executarArvoreCompleta();
        }
        fecharJanela(event);
    }

    @FXML
    public void onOpcaoDoisClick(ActionEvent event) {
        if (controllerPrincipal != null) {
            controllerPrincipal.executarArvoreParcial();
        }
        fecharJanela(event);
    }

    private void fecharJanela(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}