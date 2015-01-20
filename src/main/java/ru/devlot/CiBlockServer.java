package ru.devlot;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.apache.commons.cli.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("AccessStaticViaInstance")
public class CiBlockServer {
    private static final char OPT_USER = 'u';
    private static final char OPT_PASSWORD = 'p';

    private static final Options OPTIONS = new Options() {{
        addOption(OptionBuilder.hasArg().isRequired().withDescription("google name").create(OPT_USER));
        addOption(OptionBuilder.hasArg().isRequired().withDescription("google password").create(OPT_PASSWORD));
    }};

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
        CommandLineParser cmdParser = new GnuParser();
        CommandLine cmd = cmdParser.parse(OPTIONS, args);

        username = cmd.getOptionValue(OPT_USER);
        password = cmd.getOptionValue(OPT_PASSWORD);

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
