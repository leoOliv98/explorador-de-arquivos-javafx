module explorador.arquivos.demo {
    requires javafx.controls;
    requires javafx.fxml;


    opens explorador.arquivos.demo to javafx.fxml;
    exports explorador.arquivos.demo;
    exports explorador.arquivos.demo.controller;
    opens explorador.arquivos.demo.controller to javafx.fxml;
}