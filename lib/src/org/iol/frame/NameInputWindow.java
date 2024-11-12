package org.iol.frame;

import org.iol.Client;
import org.iol.lib.PacketHandlerTool;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class NameInputWindow extends JFrame {
    private final JTextField nameField;
    private final JTextField pswdField;

    public NameInputWindow(PacketHandlerTool tool) {
        setTitle("Internet Online Login");
        setSize(300, 365);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());

        nameField = new JTextField(20);
        pswdField = new JTextField(20);

        ImageIcon buttonImageIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/btn_texture.png")));
        JButton submitButton = new JButton(buttonImageIcon);
        submitButton.setToolTipText("Submit");

        ImageIcon imageIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/title.png")));
        JLabel imageLabel = new JLabel(imageIcon);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        add(imageLabel);

        add(new JLabel("Enter your name:"));
        add(nameField);
        add(new JLabel("Enter your password:"));
        add(pswdField);
        add(submitButton);

        submitButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String pswd = pswdField.getText().trim();

            if(name.contains(";")) {
                JOptionPane.showMessageDialog(NameInputWindow.this, "Name cannot contain it!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if(!name.isEmpty()) {
                Client.name = name;
                tool.sendServerPacket("Login", name + ';' + pswd);
            } else {
                JOptionPane.showMessageDialog(NameInputWindow.this, "Name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }
}