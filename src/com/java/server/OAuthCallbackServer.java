package com.java.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class OAuthCallbackServer {
    private static String authorizationCode;

    public static String getAuthorizationCode() {
        return authorizationCode;
    }

    public static void startServer() throws IOException {
    	
    	HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);
        server.createContext("/callback", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.contains("code=")) {
                    authorizationCode = extractCode(query);
                    String response = "Authorization successful! You may now close this window.";
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    server.stop(1); // stop the server after handling the request
                }
            }

            private String extractCode(String query) {
                for (String param : query.split("&")) {
                    if (param.startsWith("code=")) {
                        return param.substring("code=".length());
                    }
                }
                return null;
            }
        });
        server.start();
    }
}