package org.back.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 物流大屏WebSocket服务端点
 * 用于向前端推送实时数据更新
 */
@ServerEndpoint("/ws/logistics-dashboard")
@Component
public class LogisticsDashboardWebSocket {

    private static final Logger logger = LoggerFactory.getLogger(LogisticsDashboardWebSocket.class);
    
    // 静态变量，用来记录当前在线连接数
    private static final AtomicInteger onlineCount = new AtomicInteger(0);
    
    // concurrent包的线程安全Set，用来存放每个客户端对应的WebSocket对象
    private static final CopyOnWriteArraySet<LogisticsDashboardWebSocket> webSocketSet = new CopyOnWriteArraySet<>();
    
    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    
    // 用户ID，用于标识不同的连接
    private String userId;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        this.userId = session.getId();
        webSocketSet.add(this);
        addOnlineCount();
        logger.info("新连接加入，当前在线人数为: {}", getOnlineCount());
        
        try {
            sendMessage("{\"type\":\"connection\",\"message\":\"连接成功\"}");
        } catch (IOException e) {
            logger.error("发送消息异常: {}", e.getMessage());
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        subOnlineCount();
        logger.info("连接关闭，当前在线人数为: {}", getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("收到来自客户端的消息: {}", message);
        
        try {
            // 可以在这里处理客户端发送的消息
            // 比如客户端请求特定数据等
            if ("ping".equals(message)) {
                sendMessage("{\"type\":\"pong\",\"message\":\"pong\"}");
            }
        } catch (IOException e) {
            logger.error("处理客户端消息异常: {}", e.getMessage());
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("WebSocket发生错误: {}", error.getMessage());
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 群发自定义消息
     */
    public static void sendInfo(String message) {
        logger.info("推送消息到所有客户端，推送内容: {}", message);
        
        for (LogisticsDashboardWebSocket item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                logger.error("推送消息异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 推送大屏数据更新
     */
    public static void pushDashboardUpdate(Object data) {
        try {
            String message = objectMapper.writeValueAsString(new WebSocketMessage("dashboard-update", data));
            sendInfo(message);
        } catch (Exception e) {
            logger.error("推送大屏数据更新异常: {}", e.getMessage());
        }
    }

    /**
     * 推送实时统计数据
     */
    public static void pushStatisticsUpdate(Object statistics) {
        try {
            String message = objectMapper.writeValueAsString(new WebSocketMessage("statistics-update", statistics));
            sendInfo(message);
        } catch (Exception e) {
            logger.error("推送统计数据更新异常: {}", e.getMessage());
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount.get();
    }

    public static synchronized void addOnlineCount() {
        onlineCount.incrementAndGet();
    }

    public static synchronized void subOnlineCount() {
        onlineCount.decrementAndGet();
    }

    /**
     * WebSocket消息包装类
     */
    public static class WebSocketMessage {
        private String type;
        private Object data;
        private long timestamp;

        public WebSocketMessage() {
            this.timestamp = System.currentTimeMillis();
        }

        public WebSocketMessage(String type, Object data) {
            this.type = type;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters and Setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}