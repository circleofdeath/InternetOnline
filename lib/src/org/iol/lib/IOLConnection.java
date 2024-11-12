package org.iol.lib;

import java.io.BufferedReader;
import java.io.PrintWriter;

public abstract class IOLConnection {
    public RSAConnection rsaConnection = new RSAConnection();

    public abstract PrintWriter getWriter();
    public abstract BufferedReader getIn();

    public void sendServerPacket(ServerPacket packet) {
        getWriter().println("1" + packet.getSendData());
    }

    public void sendPacket(Packet packet) {
        getWriter().println("0" + packet.getSendData());
    }

    public Packet receivePacket(String line) {
        try {
            Packet packet = parseFromString(line);

            if(packet == null) {
                return null;
            }

            if(packet.isEncrypted()) {
                String key = packet.key();

                // It can't decrypt using private key if public not the same
                if(!rsaConnection.key.equals(key)) {
                    return null;
                }

                return new Packet(packet.name(), packet.key(), rsaConnection.decrypt(packet.value()));
            } else {
                return packet;
            }
        } catch(Exception ignored) {
            return null;
        }
    }

    public static ServerPacket parseFromStringServer(String packet) {
        int i = packet.indexOf(";");
        if(i == -1) return null;
        return new ServerPacket(packet.substring(0, i), packet.substring(i + 1));
    }

    public static Packet parseFromString(String packet) {
        int i = packet.indexOf(";");
        if(i == -1) return null;
        String name = packet.substring(0, i);
        packet = packet.substring(i + 1);

        i = packet.indexOf(';');
        if(i != 1) return null;
        String encryptionStatus = packet.substring(0, i);
        String key;

        if("1".equals(encryptionStatus)) {
            packet = packet.substring(i + 1);
            i = packet.indexOf(';');
            if(i == -1) return null;
            key = packet.substring(0, i);
        } else if("0".equals(encryptionStatus)) {
            key = null;
        } else {
            return null;
        }

        return new Packet(name, key, packet.substring(i + 1));
    }

    public record ServerPacket(String name, String value) {
        public String getSendData() {
            return name + ";" + value;
        }
    }

    public record Packet(String name, String key, String value) {
        public String getSendData() {
            return name + ";" + (key == null ? "0" : "1;" + key) + ";" + value;
        }

        public boolean isEncrypted() {
            return key != null;
        }
    }
}