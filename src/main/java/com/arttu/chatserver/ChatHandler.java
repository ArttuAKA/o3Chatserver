package com.arttu.chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
//http://localhost:8001/Chat
import java.util.stream.Collectors;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.Headers;
import java.nio.charset.*;


public class ChatHandler implements HttpHandler {
	
	private static final Collection<?> jsonMessage = null;

	private String responseBody = "";

    private ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
		int code = 200;
		try {
			if(exchange.getRequestMethod().equalsIgnoreCase("POST")) {
		        code = handleChatMessageFromClient(exchange);
			} else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
				code = handleGetRequestFromClient(exchange);
			} else {
				code = 400;
				responseBody = "Not supported.";
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
			byte [] bytes = responseBody.getBytes("UTF-8");
			exchange.sendResponseHeaders(code, bytes.length);
			OutputStream os = exchange.getResponseBody();
			os.write(bytes);
			os.close();
		}
	}
	
	private int handleChatMessageFromClient(HttpExchange exchange) throws Exception {
		int code = 200;
		Headers headers = exchange.getRequestHeaders();
		int contentLength = 0;
		String contentType = "";
		if (headers.containsKey("Content-Length")) {
			contentLength = Integer.parseInt(headers.get("Content-Length").get(0));
		} else {
			code = 411;
			responseBody = "Invalid user credentials";
			ChatServer.log(responseBody);
			return code;
		}
		if (headers.containsKey("Content-Type")) {
			contentType = headers.get("Content-Type").get(0);
		} else {
			code = 400;
			responseBody = "No content type in request";
			return code;
		}
		if (contentType.equalsIgnoreCase("text/plain")) {
			InputStream stream = exchange.getRequestBody();
			String text = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
						.lines()
						.collect(Collectors.joining("\n"));
			ChatServer.log(text);
			stream.close();
			if (text.trim().length() > 0) {
				processMessage(text);
				exchange.sendResponseHeaders(code, -1);
				ChatServer.log("New chatmessage saved");
			} else {
				code = 400;
				responseBody = "No content in request";
				ChatServer.log(responseBody);
			}
		} else {
			code = 411;
			responseBody = "Content-Type must be text/plain.";
			ChatServer.log(responseBody);
		}
		return code;
		
	}
	
	private void processMessage( String text) throws JSONException{
		JSONObject jsonObject = new JSONObject(text);
		ChatMessage newMessage = new ChatMessage();
		newMessage.nick = jsonObject.getString("user");
		String dateStr = jsonObject.getString("sent");
		OffsetDateTime odt = OffsetDateTime.parse(dateStr);
		newMessage.sent = odt.toLocalDateTime();
		newMessage.message = jsonObject.getString("message");
		

		messages.add(newMessage);
		Collections.sort(messages, new Comparator<ChatMessage>() {
			@Override
			public int compare(ChatMessage lhs, ChatMessage rhs) {
			return lhs.sent.compareTo(rhs.sent);
			}
			});
	}
	
	private int handleGetRequestFromClient(HttpExchange exchange) throws IOException, SQLException {
		int code = 200;
		
		if (messages.isEmpty()) {
			code = 204; // response code is 20, No Content
			exchange.sendResponseHeaders(code, -1); // -1 as content length: No content
			} else {
				JSONArray responseMessages = new JSONArray();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMdd'T'HH:mm:ss.SSSX");
				//responseMessages.put(jsonMessage);
				for (ChatMessage message : messages) {
					LocalDateTime date = message.sent;
					ZonedDateTime toSend = ZonedDateTime.of(date, ZoneId.of("UTC"));
					String dateText = toSend.format(formatter);
					responseBody += message + "\n";
					JSONObject jsonmg = new JSONObject();
					jsonmg.put("user", message.nick);
					jsonmg.put("message", message.message);
					jsonmg.put("sent", dateText);
					//formatter.toString();
					responseMessages.put(jsonMessage);
					
				}
			}
		responseBody = "";
		
		
		ChatServer.log("Delivering " + messages.size() + " messages to client");
		byte [] bytes;
		bytes = responseBody.toString().getBytes("UTF-8");
		exchange.sendResponseHeaders(code, bytes.length);
		OutputStream os = exchange.getResponseBody();
		os.write(bytes);
		os.close();
		return code;
	}
}
