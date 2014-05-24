package ru.devlot;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

public class LotDeveloperEngine {

    public static String username;
    public static String password;

    public static final String SERVER_NAME = "ciblock";

    public static final long SLEEP_TIME;
    static {
        if (System.getProperty("user.name").equals(SERVER_NAME)) {
            SLEEP_TIME = 60 * 60 * 1000;
        } else {
            SLEEP_TIME = 10 * 1000;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new Exception("No username or password");
        }

        username = args[0];
        password = args[1];

        Server server = new Server();

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);

        server.addConnector(connector);

        WebAppContext context = new WebAppContext();
        context.setWar("src/main/webapp");

        server.setHandler(context);

        server.start();

        System.out.println("Server started!");
    }

}
