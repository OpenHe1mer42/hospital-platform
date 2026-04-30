module com.example.hospitalproject {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.hospitalproject to javafx.fxml;
    exports com.example.hospitalproject;
}