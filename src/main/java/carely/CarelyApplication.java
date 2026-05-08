package carely;

import carely.config.DatabaseConfig;
import carely.config.MigrationRunner;
import carely.utils.ViewNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class CarelyApplication extends Application {
    @Override
    public void start(Stage stage) {
        ViewNavigator.setStage(stage);

        if (DatabaseConfig.shouldRunMigrations()) {
            try {
                new MigrationRunner().runMigrations();
            } catch (RuntimeException exception) {
                System.err.println("Carely migrations failed: " + exception.getMessage());
            }
        }

        stage.setTitle("Carely");
        stage.setMinWidth(1024);
        stage.setMinHeight(640);
        ViewNavigator.showLogin();
    }
}
