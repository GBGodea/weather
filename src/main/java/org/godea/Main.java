package org.godea;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.setContextPath("/");
        handler.addServlet(WeatherServlet.class, "/weather");
        server.setHandler(handler);

        server.start();
        System.out.println("Server started at http://localhost:8080");
        server.join();
    }
}
