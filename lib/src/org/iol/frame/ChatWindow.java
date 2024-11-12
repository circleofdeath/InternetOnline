package org.iol.frame;

import org.iol.Client;

import javax.swing.*;
import java.awt.*;

import java.util.function.Consumer;

public class ChatWindow extends JFrame {
    private final JTextArea messageArea;
    private final JTextField inputField;

    public ChatWindow(Consumer<String> sender) {
        setTitle("Internet Online v1.0: logged as " + Client.name);
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(messageArea);

        inputField = new JTextField(20);
        JButton sendButton = new JButton("Send");

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> {
            String message = inputField.getText().trim();
            if(!message.isEmpty()) {
                sender.accept(message);
                inputField.setText("");
            }
        });

        setVisible(true);
    }

    public void appendMessage(String message) {
        messageArea.append(message + "\n");
    }
}