package com.arttu.chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationHandler implements HttpHandler {

    ChatAuthenticator auth = null;

    RegistrationHandler(ChatAuthenticator authenticator) {
        auth = authenticator;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String responseBody = "";
        int code = 200;
        try {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

                Headers headers = exchange.getRequestHeaders();
                int contentLength = 0;
                String contentType = "";
                if (headers.containsKey("Content-Length")) {
                    contentLength = Integer.parseInt(headers.get("Content-Length").get(0));
                } else {
                    code = 411;
                    responseBody = "Invalid user credentials";
                    ChatServer.log(responseBody);


                }
                if (headers.containsKey("Content-Type")) {
                    contentType = headers.get("Content-Type").get(0);
                } else {
                    code = 400;
                    responseBody = "No content type in request";
                }
                    
                if (contentType.equalsIgnoreCase("application/json")) {
                   
                    InputStream stream = exchange.getRequestBody();
                    String text = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines()
                            .collect(Collectors.joining("\n"));
                    ChatServer.log(text);
                    stream.close();
                    if (text.trim().length() > 0) {
                        JSONObject registrationMsg = new JSONObject(text);
                        String username = registrationMsg.getString("username");
                        String password = registrationMsg.getString("password");
                        String email = registrationMsg.getString("email");
                        User newUser = new User(username, password, email);
                        // luo tunnus käyttäjälle
                        if (auth.addUser(newUser)) {
                            exchange.sendResponseHeaders(code, -1);
                            ChatServer.log("Added as user");
                        } else {
                            code = 400;
                            responseBody = "registration failed";
                        }
                    } else {
                        code = 400;
                        responseBody = "No content in request";
                        ChatServer.log(responseBody);
                    }
                } else {
                    code = 411;
                    responseBody = "Content-Type must be application/json";
                    ChatServer.log(responseBody);
                }
            } else {
                code = 400;
                responseBody = "Not supported";
            }
        } catch (IOException e) {
            code = 500;
            responseBody = "Error in handling the request: " + e.getMessage();
        } catch (JSONException e) {
            code = 400;
            responseBody = "JSON error" + e.getMessage();
        }
         catch (Exception e) {
            code = 500;
            responseBody = "Server error: " + e.getMessage();
        }
        if (code < 200 || code > 299) {
            ChatServer.log("*** Error in /chat: " + code + " " + responseBody);
            byte[] bytes = responseBody.getBytes("UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }

    }

}
