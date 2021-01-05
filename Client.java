
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.*;

public class Client extends JFrame {
    JTextField searchTf;
    JButton searchBtn;
    JLabel resultLabel;
    JTextArea resultTa;
    JScrollPane scrollPane;
    JList<String> recordsList;
    DefaultListModel listModel;

    String searchInput;
    String result; // Response from Server
    Dictionary<String, String> records = new Hashtable<String, String>();

    Socket socket;
    ObjectInputStream input;
    ObjectOutputStream output;
    String serverName;
    Scanner scanner;
    int port;

    public void loadGUI() {
        Container cp = getContentPane();
        cp.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 20));
        JPanel searchPanel = new JPanel(new FlowLayout());
        JPanel resultPanel = new JPanel(new BorderLayout());
        JPanel recordPanel = new JPanel(new BorderLayout());

        searchBtn = new JButton("GO!");
        searchTf = new JTextField();
        searchTf.setColumns(10);
        // searchTf.setSize(50, 20);

        resultLabel = new JLabel("Result:");
        resultTa = new JTextArea(10, 25);
        resultTa.setLineWrap(true);

        listModel = new DefaultListModel<String>();
        recordsList = new JList<String>(listModel);
        recordsList.setVisibleRowCount(5);

        searchBtn.addActionListener(e -> {
            result = "";
            searchInput = searchTf.getText();
            searchTf.setText("");
            resultLabel.setText(searchInput + ":");

            try {
                // pass the user's input to server
                output.writeObject(searchInput);
                output.flush();

                // server response
                result = (String) input.readObject();
                resultTa.setText(result);
                records.put(searchInput, result); // Add to Dictionary

                // add result to searchRecord JList
                if (!listModel.contains(searchInput)) {
                    if (result.trim().contains("查無資料")) {
                        listModel.addElement(searchInput + " (查無資料)");
                    } else {
                        listModel.addElement(searchInput);
                    }
                }
            } catch (IOException e1) {
                e1.printStackTrace();
                close();
            } catch (Exception e2) {
                e2.printStackTrace();
                close();
            }
        });

        recordsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                resultLabel.setText(recordsList.getSelectedValue() + ":");
                resultTa.setText(records.get(recordsList.getSelectedValue()));
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        searchPanel.add(new JLabel("Search in WIKI:"));
        searchPanel.add(searchTf);
        searchPanel.add(searchBtn);
        cp.add(searchPanel);

        resultPanel.add(resultLabel, BorderLayout.NORTH);
        resultPanel.add(new JScrollPane(resultTa), BorderLayout.SOUTH);
        cp.add(resultPanel);

        recordPanel.add(new JLabel("Your records:"), BorderLayout.NORTH);
        recordPanel.add(new JScrollPane(recordsList), BorderLayout.SOUTH);
        cp.add(recordPanel);

        setSize(400, 500);
        setVisible(true);
    }

    @Override
    public Insets getInsets() {
        return new Insets(20, 20, 20, 20);
    }

    public void close() {
        try {
            input.close();
            output.close();
            socket.close();
            System.out.println("connection is closed");
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    public Client(String name, int p) {
        super("WIKI Search");
        serverName = name;
        port = p;
        try {
            socket = new Socket(InetAddress.getByName(serverName), port);
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            System.out.println("create connect successful");
        } catch (UnknownHostException x) {
            x.printStackTrace();
            close();
        } catch (IOException x) {
            x.printStackTrace();
            close();
        }

        loadGUI();
    }

    public static void main(String args[]) {
        Client client = new Client("localhost", 20000);
        // client.exec();
    }

    class MyThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                result = (String) input.readObject();
                resultTa.setText(result);
                records.put(searchInput, result); // Add to Dictionary
                // add result to searchRecord JList
                if (!listModel.contains(searchInput)) {
                    if (result.trim().contains("查無資料")) {
                        listModel.addElement(searchInput + " (查無資料)");
                    } else {
                        listModel.addElement(searchInput);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}