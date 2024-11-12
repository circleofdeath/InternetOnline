package org.iol;

public class App {
    public static void main(String[] args) {
        if("client".equals(args[0])) {
            Client.main(args);
        } else if("server".equals(args[0])) {
            Server.main(args);
        } else {
            System.out.println("Invalid: " + args[0]);
        }
    }
}