module carely {
    requires java.sql;
    requires jbcrypt;
    requires javafx.controls;
    requires javafx.fxml;


    opens carely to javafx.fxml;
    opens carely.controller.auth to javafx.fxml;
    opens carely.controller.layout to javafx.fxml;
    opens carely.controller.pages to javafx.fxml;
    exports carely;
    exports carely.config;
    exports carely.mapper;
    exports carely.model;
    exports carely.model.dto.diagnosis;
    exports carely.repository;
    exports carely.service;
    exports carely.utils;
}
