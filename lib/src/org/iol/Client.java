package org.iol;

import org.iol.frame.ChatWindow;
import org.iol.frame.NameInputWindow;
import org.iol.lib.IOL;

import javax.swing.*;

public class Client {
    public static volatile boolean login_ed = false;
    public static NameInputWindow nameInput;
    public static ChatWindow chat;
    public static String name;

    public static void main(String[] args) {
        IOL.load();
        System.out.println("Welcome to Internet Online! Type !help for help!");

        IOL.connect(tool -> {
            tool.onPacketReceived("Message", (packet) -> {
                if(!login_ed) return;
                String message = packet.value();
                String content;
                String author;

                int i = message.indexOf(';');
                if(i == -1) {
                    author = "Unknown";
                    content = message;
                } else {
                    author = message.substring(0, i);
                    content = message.substring(i + 1);
                }

                chat.appendMessage("[" + author + "]: " + content);
            });

            tool.onServerPacketReceived("InvalidName", (packet) -> {
                JOptionPane.showMessageDialog(nameInput, "Error occurred while log-in, you're online now or incorrect password", "Error", JOptionPane.ERROR_MESSAGE);
            });

            tool.onServerPacketReceived("LoginStatus200", (packet) -> {
                login_ed = true;
                nameInput.setVisible(false);
                tool.sendServerPacket("MyToken", tool.connection.rsaConnection.key);
            });

            tool.onServerPacketReceived("UserJoined", (packet) -> {
                if(chat != null) {
                    chat.appendMessage("User " + packet.value() + " joined chat!");
                }
            });

            tool.onServerPacketReceived("Logout", (packet) -> chat.appendMessage("User " + packet.value() + " left chat!"));

            new Thread(() -> {
                nameInput = new NameInputWindow(tool);

                while(!login_ed) {
                    Thread.onSpinWait();
                }

                chat = new ChatWindow((msg) -> {
                    tool.sendPacket("Message", name + ';' + msg);
                    chat.appendMessage("[" + name + "]: " + msg);
                });
            }).start();
        });
    }
}