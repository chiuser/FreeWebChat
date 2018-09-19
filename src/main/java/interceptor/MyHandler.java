package interceptor;

import com.xnpe.fchat.data.Message;
import com.xnpe.fchat.data.MessageKey;
import org.json.JSONObject;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MyHandler implements WebSocketHandler {

    private static final Map<String, Map<String, WebSocketSession>> sUserMap = new HashMap<>(3);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("成功建立连接");
        String INFO = session.getUri().getPath().split("INFO=")[1];
        System.out.println(INFO);
        if (INFO != null && INFO.length() > 0) {
            JSONObject jsonObject = new JSONObject(INFO);
            String command = jsonObject.getString("command");
            String roomId = jsonObject.getString("roomId");
            if (command != null && MessageKey.ENTER_COMMAND.equals(command)) {
                Map<String, WebSocketSession> mapSession = sUserMap.get(roomId);
                if (mapSession == null) {
                    mapSession = new HashMap<>(3);
                    sUserMap.put(roomId, mapSession);
                }
                mapSession.put(jsonObject.getString("name"), session);
                session.sendMessage(new TextMessage("当前房间在线人数" + mapSession.size() + "人"));
                System.out.println(session);
            }
        }
        System.out.println("当前在线人数：" + sUserMap.size());
    }

    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) {
        try {
            JSONObject jsonobject = new JSONObject(webSocketMessage.getPayload().toString());
            Message message = new Message(jsonobject.toString());
            System.out.println(jsonobject.toString());
            System.out.println(message + ":来自" + webSocketSession.getAttributes().get(MessageKey.KEY_WEBSOCKET_USERNAME) + "的消息");
            if (message.getName() != null && message.getCommand() != null) {
                switch (message.getCommand()) {
                    case MessageKey.ENTER_COMMAND:
                        sendMessageToRoomUsers(message.getRoomId(), new TextMessage("【" + getNameFromSession(webSocketSession) + "】加入了房间，欢迎！"));
                        break;
                    case MessageKey.MESSAGE_COMMAND:
                        if (message.getName().equals("all")) {
                            sendMessageToRoomUsers(message.getRoomId(), new TextMessage(getNameFromSession(webSocketSession) +
                                    "说：" + message.getInfo()
                            ));
                        } else {
                            sendMessageToUser(message.getRoomId(), message.getName(), new TextMessage(getNameFromSession(webSocketSession) +
                                    "悄悄对你说：" + message.getInfo()));
                        }
                        break;
                    case MessageKey.LEAVE_COMMAND:
                        sendMessageToRoomUsers(message.getRoomId(), new TextMessage("【" + getNameFromSession(webSocketSession) + "】离开了房间，欢迎下次再来"));
                        break;
                        default:
                            break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送信息给指定用户
     *
     * @param name
     * @param message
     * @return
     */
    public boolean sendMessageToUser(String roomId, String name, TextMessage message) {
        if (roomId == null || name == null) return false;
        if (sUserMap.get(roomId) == null) return false;
        WebSocketSession session = sUserMap.get(roomId).get(name);
        if (!session.isOpen()) return false;
        try {
            session.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 广播信息
     *
     * @param message
     * @return
     */
    public boolean sendMessageToRoomUsers(String roomId, TextMessage message) {
        if (roomId == null) return false;
        if (sUserMap.get(roomId) == null) return false;
        boolean allSendSuccess = true;
        Collection<WebSocketSession> sessions = sUserMap.get(roomId).values();
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
                allSendSuccess = false;
            }
        }

        return allSendSuccess;
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
        System.out.println("连接出错");
        if (webSocketSession.isOpen()) {
            webSocketSession.close();
        }
        Map<String, WebSocketSession> map = sUserMap.get(getRoomIdFromSession(webSocketSession));
        if (map != null) {
            map.remove(getNameFromSession(webSocketSession));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        System.out.println("连接已关闭：" + closeStatus);
        Map<String, WebSocketSession> map = sUserMap.get(getRoomIdFromSession(webSocketSession));
        if (map != null) {
            map.remove(getNameFromSession(webSocketSession));
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 获取用户名称
     *
     * @param session
     * @return
     */
    private String getNameFromSession(WebSocketSession session) {
        try {
            String name = (String) session.getAttributes().get(MessageKey.KEY_WEBSOCKET_USERNAME);
            return name;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取房间号
     *
     * @param session
     * @return
     */
    private String getRoomIdFromSession(WebSocketSession session) {
        try {
            String roomId = (String) session.getAttributes().get(MessageKey.KEY_ROOM_ID);
            return roomId;
        } catch (Exception e) {
            return null;
        }
    }

}
