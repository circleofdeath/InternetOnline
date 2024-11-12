package org.iol.lib;

import java.net.SocketException;
import java.util.*;
import java.util.function.Consumer;

import org.iol.lib.IOLConnection.*;

public class PacketHandlerTool {
    public Map<String, List<Consumer<ServerPacket>>> serverPacketMap = new HashMap<>();
    public Map<String, List<Consumer<Packet>>> packetMap = new HashMap<>();
    public List<Consumer<ServerPacket>> receivers2 = new ArrayList<>();
    public List<Consumer<Packet>> receivers = new ArrayList<>();
    public IOLConnection connection;

    public PacketHandlerTool(IOLConnection connection) {
        this.connection = Objects.requireNonNull(connection);
    }

    public void onServerPacketReceived(String name, Consumer<ServerPacket> consumer) {
        if(!serverPacketMap.containsKey(name)) {
            serverPacketMap.put(name, new ArrayList<>());
        }

        serverPacketMap.get(name).add(consumer);
    }

    public void onPacketReceived(String name, Consumer<Packet> consumer) {
        if(!packetMap.containsKey(name)) {
            packetMap.put(name, new ArrayList<>());
        }

        packetMap.get(name).add(consumer);
    }

    public void sendServerPacket(String name, String value) {
        connection.sendServerPacket(new ServerPacket(name, value));
    }

    public void sendPacket(String name, String value) {
        connection.sendPacket(new Packet(name, null, value));
    }

    public void sendEncryptedPacket(String name, String key, String value) {
        connection.sendPacket(new Packet(name, key, RSAConnection.encryptMessage(key, value)));
    }

    public void listenClient() {
        while(true) {
            try {
                String line = connection.getIn().readLine();
                if(line == null) continue;

                if(line.startsWith("0")) {
                    line = line.substring(1);
                    Packet packet = connection.receivePacket(line);
                    if(packet == null) continue;

                    receivers.forEach(c -> c.accept(packet));
                    String name = packet.name();

                    if(packetMap.containsKey(name)) {
                        packetMap.get(name).forEach(c -> c.accept(packet));
                    }
                } else {
                    line = line.substring(1);
                    ServerPacket packet = IOLConnection.parseFromStringServer(line);
                    if(packet == null) continue;

                    receivers2.forEach(c -> c.accept(packet));
                    String name = packet.name();

                    if(serverPacketMap.containsKey(name)) {
                        serverPacketMap.get(name).forEach(c -> c.accept(packet));
                    }
                }
            } catch(SocketException e) {
                System.out.println("Socket closed");
                break;
            } catch(Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}