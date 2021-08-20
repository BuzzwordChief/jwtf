module de.buzzwordchief.jwtf {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.net.http;
    requires com.google.gson;


    opens de.buzzwordchief.jwtf to javafx.fxml, com.google.gson;
    exports de.buzzwordchief.jwtf;
}
