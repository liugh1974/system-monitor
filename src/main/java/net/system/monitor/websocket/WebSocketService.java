package net.system.monitor.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@ServerEndpoint(value = "/ws/{id}")
@Component
public class WebSocketService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketService.class);

    private static final List<WebSocketListener> listeners = new ArrayList<>();
    private static Map<String, Session> websocketSessions = new HashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("id") String id) {
        websocketSessions.put(id, session);
        LOGGER.info("websocket session({}) added.", id);
    }

    @OnClose
    public void onClose(Session session, @PathParam("id") String id) {
        websocketSessions.remove(id);
        LOGGER.info("The session({}) close", id);
    }

    public void addListener(WebSocketListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.info("###### Received send message: {}", message);
        for (WebSocketListener listener : listeners) {
            listener.process(message);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        LOGGER.error(error.getMessage(), error);
    }

    private static void sendMessage(String id, Session session, String msg) {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(msg);
                LOGGER.info("Sent message to client: {}, message: {}", id, msg);
            } catch (IOException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(e.getMessage(), e);
                }
            }
        } else {
            LOGGER.info("The session({}) is closed, cannot push the message to it.", id);
        }
    }

    public static void sendMessage(String id, WebsocketMessage message) {
        Session session = websocketSessions.get(id);
        sendMessage(id, session, JsonUtil.toJson(message));
    }

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void broadcast(WebsocketMessage msg) {
        String json = JsonUtil.toJson(msg);
        for (Entry<String, Session> entry : websocketSessions.entrySet()) {
            executorService.execute(() -> sendMessage(entry.getKey(), entry.getValue(), json));
        }
    }
}
