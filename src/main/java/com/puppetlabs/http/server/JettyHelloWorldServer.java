package com.puppetlabs.http.server;


import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.ssl.SslSocketConnector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JettyHelloWorldServer {

    public static void main(String[] args) throws Exception {
        Handler handler=new AbstractHandler()
        {
            @Override
            public void handle(String path, Request request, HttpServletRequest httpServletRequest, HttpServletResponse response)
                    throws IOException, ServletException
            {
                response.setContentType("text/html");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("<h1>Hello</h1>");
                ((Request)request).setHandled(true);
            }

        };

        Server server = new Server();
        SslSocketConnector connector = new SslSocketConnector();
        connector.setPort(8140);
        connector.setKeystore("./src/main/resources/com/puppetlabs/http/server/jetty.keystore");
        connector.setTrustPassword("puppet");
        connector.setKeyPassword("puppet");
        server.setConnectors(new Connector[] { connector });
        server.setHandler(handler);
        server.start();
    }
}
