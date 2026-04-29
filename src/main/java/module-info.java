module com.carthagegg.carthagegg {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires java.sql;
    requires org.mariadb.jdbc;
    requires jbcrypt;
    requires org.apache.commons.io;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires AnimateFX;
    requires com.google.gson;
    requires java.prefs;
    requires java.desktop;
    requires java.net.http;
    requires jdk.httpserver;

    opens com.carthagegg to javafx.fxml;
    opens com.carthagegg.controllers.auth to javafx.fxml;
    opens com.carthagegg.controllers.front to javafx.fxml;
    opens com.carthagegg.controllers.front.components to javafx.fxml;
    opens com.carthagegg.controllers.back to javafx.fxml;
    opens com.carthagegg.utils to javafx.fxml;
    opens com.carthagegg.models to com.google.gson, javafx.base;

    exports com.carthagegg;
    exports com.carthagegg.controllers.auth;
    exports com.carthagegg.controllers.front;
    exports com.carthagegg.controllers.back;
    exports com.carthagegg.models;
    exports com.carthagegg.utils;
    exports com.carthagegg.dao;
}
