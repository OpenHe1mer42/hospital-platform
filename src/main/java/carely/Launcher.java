package carely;

import carely.config.MigrationRunner;
import javafx.application.Application;

import java.util.Arrays;

public class Launcher {
    public static void main(String[] args) {
        if (Arrays.asList(args).contains("--migrate")) {
            new MigrationRunner().runMigrations();
            System.out.println("Carely migrations completed.");
            return;
        }

        Application.launch(CarelyApplication.class, args);
    }
}
