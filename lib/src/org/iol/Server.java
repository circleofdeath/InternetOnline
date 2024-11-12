package org.iol;

import org.iol.lib.IOL;
import org.iol.lib.IOLServer;
import org.iol.lib.PacketHandlerTool;
import org.iol.util.SHA256;
import org.iol.util.Utils;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public static final List<Connection> users = new ArrayList<>();
    public static final List<UserDBEntry> database = new ArrayList<>();

    public static UserDBEntry findDB(String name) {
        synchronized(database) {
            for(UserDBEntry entry : database) {
                if(entry.name.equals(name)) {
                    return entry;
                }
            }
            return null;
        }
    }

    public static Connection getByTool(PacketHandlerTool tool) {
        synchronized(users) {
            for(Connection connection : users) {
                if(connection.tool == tool) {
                    return connection;
                }
            }
            return null;
        }
    }

    public static boolean contains(String name) {
        synchronized(users) {
            for(Connection connection : users) {
                if(connection.name != null && connection.name.equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static void loadDatabase() {
        try {
            File dbFile = new File(Utils.concatChild(IOL.data_directory, "database.txt"));

            if(!dbFile.exists()) {
                dbFile.createNewFile();
            }

            String[] entries = Files.readString(dbFile.toPath()).split(";");
            if(entries.length % 2 == 0) {
                System.out.print("Failed to load database!");
                return;
            }

            for(int i = 0; i < entries.length - 1; i += 2) {
                database.add(new UserDBEntry(entries[i].trim(), entries[i + 1].trim()));
            }
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void addToDatabase(UserDBEntry entry) {
        File dbFile = new File(Utils.concatChild(IOL.data_directory, "database.txt"));
        try {
            Files.writeString(dbFile.toPath(), Files.readString(dbFile.toPath()) + entry.name + ";" + entry.password + ";\n");
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
        database.add(entry);
    }

    public static void main(String[] args) {
        IOL.load();
        loadDatabase();
        IOLServer.start(tool -> {
            Connection connection = new Connection();
            connection.tool = tool;

            synchronized(users) {
                users.add(connection);
            }

            tool.onServerPacketReceived("MyToken", (packet) -> {
                connection.key = packet.value();
            });

            tool.onServerPacketReceived("Login", (packet) -> {
                String value = packet.value();
                int i = value.indexOf(';');
                if(i == -1) {
                    tool.sendServerPacket("InvalidName", "");
                    return;
                }

                String name = value.substring(0, i);
                String pswd = SHA256.encode(value.substring(i + 1));

                synchronized(users) {
                    if(contains(name) || connection.name != null) {
                        tool.sendServerPacket("InvalidName", "");
                    } else {
                        UserDBEntry entry = findDB(name);

                        if(entry != null) {
                            if(entry.password.equals(pswd)) {
                                tool.sendServerPacket("LoginStatus200", "");
                            } else {
                                tool.sendServerPacket("InvalidName", "");
                                return;
                            }
                        } else {
                            addToDatabase(new UserDBEntry(name, pswd));
                        }

                        tool.sendServerPacket("LoginStatus200", "");
                        IOLServer.publicSendPacket(null, "1UserJoined;" + name);
                        connection.name = name;
                    }
                }
            });
        }, (tool) -> {
            Connection connection = getByTool(tool);

            if(connection != null) {
                IOLServer.publicSendPacket(null, "1Logout;" + connection.name);
            }
        });
    }

    public static class UserDBEntry {
        public String name, password;

        public UserDBEntry(String name, String password) {
            this.password = password;
            this.name = name;
        }
    }

    public static class Connection {
        public PacketHandlerTool tool;
        public String name;
        public String key;
    }
}