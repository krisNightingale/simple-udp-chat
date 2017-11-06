package ai151.solovyova;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends Application{

    public static void main(String[] args) {
	  launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("chat_interface.fxml").openStream());

        ChatController controller = fxmlLoader.getController();
        ListView<String> messages = controller.getMessages();
        ListView<String> addresses = controller.getAddresses();

        Thread thread = new Thread(() -> {
            DatagramSocket receiveSocket = null;
            try {
                receiveSocket = new DatagramSocket(7645);
            } catch (SocketException e1) {
                e1.printStackTrace();
            }
            Pattern regex = Pattern.compile("[\u0020-\uFFFF]");

            while (true){
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    receiveSocket.receive(receivePacket);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                InetAddress senderAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();

                Platform.runLater(() -> {
                    if (!addresses.getItems().contains(senderAddress.getHostAddress())){
                        addresses.getItems().add(senderAddress.getHostAddress());
                    }
                });

                String receiveMessage = new String(receivePacket.getData());
                Matcher matcher = regex.matcher(receiveMessage);

                StringBuilder message = new StringBuilder(senderAddress.getHostAddress() + ":" + port + ": ");
                while(matcher.find())
                    message.append(receiveMessage.substring(matcher.start(), matcher.end()));

                Platform.runLater(() -> {
                    messages.getItems().add(message.toString());
                    messages.scrollTo(message.toString());
                });
            }
        });
        thread.start();


        Scene scene = new Scene(root, 789, 533);
        primaryStage.setTitle("UDP Chat");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }
}
