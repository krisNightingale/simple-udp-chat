package ai151.solovyova;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    private final int PORT = 7645;
    private String broadcastAddress;

    @FXML
    Label receiverLabel;

    @FXML
    Button sendBtn;

    @FXML
    ListView<String> messages;

    @FXML
    TextField textFld;

    @FXML
    ListView<String> addresses;

    public ListView<String> getMessages() {
        return messages;
    }

    public ListView<String> getAddresses() {
        return addresses;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ArrayList<String> addressesAvailable = new ArrayList<>();
        String host = null;
        try {
           host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        if (host != null){
            addressesAvailable.add(host);
            try {
                broadcastAddress = getIPBroadcast();
                addressesAvailable.add(broadcastAddress);
                receiverLabel.setText(broadcastAddress);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        ObservableList<String> ipAddresses = FXCollections.observableArrayList(addressesAvailable);

        addresses.setItems(ipAddresses);
        messages.setPlaceholder(new Label("No messages yet"));
        messages.setFocusTraversable(false);

        addresses.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String old_val, String new_val) {
                receiverLabel.setText(new_val);
            }
        });

        sendBtn.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    sendMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                event.consume();
            }
        });

        textFld.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent key) {
                if (key.getCode().equals(KeyCode.ENTER)) {
                    try {
                        sendMessage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //key.consume();
            }
        });
    }

    private void sendMessage() throws Exception{
        DatagramSocket sendSocket = new DatagramSocket();;
        String selectedAddress = receiverLabel.getText();
        InetAddress IPAddress = InetAddress.getByName(selectedAddress);

        byte[] sendData;
        String message = textFld.getText();
        if (!message.equals("")){
            textFld.setText("");
            sendData = message.getBytes("UTF-8");
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, PORT);
            sendSocket.send(sendPacket);
        }
        if (!selectedAddress.equals(broadcastAddress)){
            message = "Me: " + message;
            messages.getItems().add(message);
            messages.scrollTo(message);
        }
    }

    private String getIPBroadcast() throws SocketException {
        Enumeration<NetworkInterface> interfaces =
                NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback())
                continue;    // Don't want to broadcast to the loopback interface
            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress broadcast = interfaceAddress.getBroadcast();
                if (broadcast == null)
                    continue;
                return broadcast.getHostAddress();
            }
        }
        return null;
    }

    private ArrayList<String> getHosts() throws Exception {
        InetAddress host = InetAddress.getLocalHost();
        ArrayList<InetAddress> addresses = new ArrayList<>();

        byte[] ip = host.getAddress();
        for (int i = 1; i <= 254; i++) {
            ip[3] = (byte)i;
            InetAddress address = InetAddress.getByAddress(ip);
            if (address.isReachable(100)) {
                System.out.println(address + " machine is turned on and can be pinged");
                addresses.add(address);
            } else if (!address.getHostAddress().equals(address.getHostName())){
                System.out.println(address + " machine is known in a DNS lookup");
            } else{
                System.out.println(address + " the host address and host name are equal, meaning the host name could not be resolved");
            }
        }
        //addresses = InetAddress.getAllByName(host.getHostAddress());
        //for (InetAddress address : addresses) System.out.println(address);
        ArrayList<String> stringAddresses = new ArrayList<>();

        for (InetAddress address: addresses) {
            stringAddresses.add(address.getHostAddress());
        }
        return stringAddresses;
    }
}
