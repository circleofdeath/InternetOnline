package org.iol.lib;

import org.iol.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.function.Consumer;

public class IOL {
    public static String data_directory;
    public static File data_file;

    public static String host = "localhost";
    public static int port = 50128;

    public static void load() {
        try {
            data_directory = Utils.concatChild(Utils.getApplicationData(), "InternetOnline");
            data_file = new File(data_directory);

            if(!data_file.exists()) {
                data_file.mkdirs();
            }

            File hostFile = new File(Utils.concatChild(data_directory, "hostname.txt"));
            File portFile = new File(Utils.concatChild(data_directory, "port.txt"));

            if(hostFile.exists()) {
                host = Files.readString(hostFile.toPath());
            } else {
                Files.writeString(hostFile.toPath(), host);
            }

            if(portFile.exists()) {
                port = Integer.parseInt(Files.readString(portFile.toPath()));
            } else {
                Files.writeString(portFile.toPath(), port + "");
            }
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void connect(Consumer<PacketHandlerTool> consumer) {
        try(Socket socket = new Socket(host, port)) {
            connect(socket, consumer);
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void connect(Socket socket, Consumer<PacketHandlerTool> consumer) {
        try(BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true))
        {
            IOLConnection connection = new IOLConnection() {
                @Override
                public PrintWriter getWriter() {
                    return out;
                }

                @Override
                public BufferedReader getIn() {
                    return in;
                }
            };

            PacketHandlerTool tool = new PacketHandlerTool(connection);
            consumer.accept(tool);
            tool.listenClient();
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }
}