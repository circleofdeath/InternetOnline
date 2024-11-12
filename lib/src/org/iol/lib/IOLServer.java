package org.iol.lib;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class IOLServer {
    public static final List<PrintWriter> users = new ArrayList<>();

    public static void publicSendPacket(PrintWriter sender, String data) {
        synchronized(users) {
            users.forEach(p -> {
                if(p != sender) {
                    p.println(data);
                }
            });
        }
    }

    public static void start(Consumer<PacketHandlerTool> consumer, Consumer<PacketHandlerTool> onClose) {
        try(ServerSocket server = new ServerSocket(IOL.port)) {
            while(true) {
                Socket socket = server.accept();
                PrintWriter user = new PrintWriter(socket.getOutputStream(), true);

                synchronized(users) {
                    System.out.println("User connected: " + user);
                    users.add(user);
                }

                PacketHandlerTool[] tmp = new PacketHandlerTool[1];
                new Thread(() -> IOL.connect(socket, (tool) -> {
                    tmp[0] = tool;
                    tool.receivers.add(packet -> publicSendPacket(user, "0" + packet.getSendData()));
                    consumer.accept(tool);
                })).start();

                new Thread(() -> {
                    while(
                            socket.isConnected()        &&
                            !socket.isClosed()          &&
                            socket.isBound()            &&
                            !socket.isInputShutdown()   &&
                            !socket.isOutputShutdown())
                    {}

                    try {
                        socket.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    onClose.accept(tmp[0]);
                    tmp[0] = null;
                    users.remove(user);
                    System.out.println("User disconnected: " + user);
                }).start();
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}