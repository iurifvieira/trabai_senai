module com.mycompany.disparadordemensagens {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql; 
   
    opens com.mycompany.disparadordemensagens to javafx.fxml;
    opens com.mycompany.disparadordemensagens.controller to javafx.fxml;
    opens com.mycompany.disparadordemensagens.models to javafx.fxml;
    
    
    exports com.mycompany.disparadordemensagens;
    exports com.mycompany.disparadordemensagens.controller;
    exports com.mycompany.disparadordemensagens.models;
}
