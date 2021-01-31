package com.arttu.chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
//http://localhost:8001/Chat
import java.util.stream.Collectors;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.nio.charset.*;


public class ChatHandler implements HttpHandler {

    private ArrayList<String> messages = new ArrayList<String>();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
		int code = 200;
		String errorMessages = "";
		try {
			if(exchange.getRequestMethod().equalsIgnoreCase("POST")) {
			
			InputStream input = exchange.getRequestBody();
			String text = new BufferedReader(new InputStreamReader(input,
									  StandardCharsets.UTF_8))
									  .lines()
									  .collect(Collectors.joining("\n"));
			messages.add(text);
			input.close();
			exchange.sendResponseHeaders(code, -1);
		  } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
		    String messageBody = "";
			for (String message : messages) {
				messageBody += message + "\n";
			}
			byte [] bytes = messageBody.getBytes("UTF-8");
			exchange.sendResponseHeaders(code, bytes.length);
			OutputStream os = exchange.getResponseBody();
			os.write(bytes);
			os.close();
		  } else {
		    code = 400;
			errorMessages = "Not supported";
		  }
	} catch (Exception e) {
	    code = 500;
		errorMessages = "Internal server error";
	}
	if (code >= 400) {
	     byte [] bytes = errorMessages.getBytes("UTF-8");
		 exchange.sendResponseHeaders(code, bytes.length);
		 OutputStream os = exchange.getResponseBody();
		 os.write(bytes);
		 os.close();
	}
}
}
