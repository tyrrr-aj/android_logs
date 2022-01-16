module pl.edu.agh.student.adbreader {
    requires javafx.controls;
    requires javafx.fxml;
    requires ddmlib.r16;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires java.sql;
    requires com.google.gson;

    opens pl.edu.agh.student.adbreader to javafx.fxml;
    exports pl.edu.agh.student.adbreader;
    exports pl.edu.agh.student.adbreader.requests;
    opens pl.edu.agh.student.adbreader.requests to javafx.fxml;
    exports pl.edu.agh.student.adbreader.utils;
    opens pl.edu.agh.student.adbreader.utils to javafx.fxml;
}
