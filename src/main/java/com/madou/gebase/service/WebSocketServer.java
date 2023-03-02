package com.madou.gebase.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.madou.gebase.common.ErrorCode;
import com.madou.gebase.exception.BusinessException;
import com.madou.gebase.model.Message;
import com.madou.gebase.model.dto.UserConsumerQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 聊天服务器
 *
 * @author MA_dou
 */
@Slf4j
// websocket服务器注解
@ServerEndpoint("/webSocket/{userAccount}")
// 将这个监听器纳入到Spring容器中进行管理
@Component
public class WebSocketServer {
    /**
     * service的静态注入，webSocket是多对象与spring管理的单例冲突
     */
    private static UserService userService;
    @Resource
    public void setUserService(UserService userService){
        WebSocketServer.userService = userService;
    }
    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static AtomicInteger onlineCount = new AtomicInteger();
    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的WebSocketServer对象。
     */
    private static ConcurrentHashMap<String, Session> sessionPools = new ConcurrentHashMap<>();


    /**
     * 连接建立成功
     */
    @OnOpen
    public synchronized void onOpen(Session session, @PathParam(value = "userAccount") String userAccount) {
        //用户是否已经登录过
        if (sessionPools.get(userAccount) != null) {
            // 移除旧session，存入新session
            sessionPools.remove(userAccount);
            if (onlineCount.get() > 0){
                subOnlineCount();
            }
        }
        sessionPools.put(userAccount, session);
        addOnlineCount();
        System.out.println(userAccount + "加入webSocket！当前人数为" + onlineCount);
        onlineUser();
    }
    public void onlineUser(){
        //储存在线用户
        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();
        result.put("userList",array);
        sessionPools.keySet().forEach(account ->{
            JSONObject user = new JSONObject();
            UserConsumerQuery userConsumer = userService.getByAccount(account);
            user.put("userId",userConsumer.getId());
            user.put("userAccount",userConsumer.getUserAccount());
            user.put("username",userConsumer.getUsername());
            user.put("avatarUrl",userConsumer.getAvatarUrl());
            array.add(user);
        });
        //群发给所有用户，在线用户的信息
        broadcast(JSON.toJSONString(result,true));
    }
    /**
     * 收到客户端信息后，根据接收人的userAccount把消息推下去或者群发
     * to=-1群发消息
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        // todo 持久化
        System.out.println("server get " + message);
        // 字符串转化为对象
        Message msg = JSON.parseObject(message, Message.class);
        if (message == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息体为空");
        }
        // 设置发送日期
        msg.setDate(new Date());
        // 发送时将对象转化为字符串
        if (msg.getConsumer().equals("-1")) {
            broadcast(JSON.toJSONString(msg, true));
        }
        else {
            sendInfo(msg.getConsumer(), JSON.toJSONString(msg, true));
        }
    }

    /**
     * 服务器推送信息
     * @param session
     * @param message
     * @throws IOException
     */
    public void sendMessage(Session session, String message) throws IOException {
        if (session != null) {
            synchronized (session) {
                System.out.println("发送数据：" + message);
                session.getBasicRemote().sendText(message);
            }
        }
    }

    /**
     * 发送信息给指定用户
     * @param userAccount
     * @param message
     */
    public void sendInfo(String userAccount, String message) {
        Session session = sessionPools.get(userAccount);
        try {
            sendMessage(session, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 推送发送信息给所有人
     *
     * @param message 要推送的消息
     */
    public void broadcast(String message) {
        for (Session toSession : sessionPools.values()) {
            try {
                sendMessage(toSession, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 连接关闭
     */
    @OnClose
    public synchronized void onClose(@PathParam(value = "userAccount") String userAccount) {
        if (sessionPools.get(userAccount) != null) {
            sessionPools.remove(userAccount);
            subOnlineCount();
        } else return;
        System.out.println(userAccount + "断开webSocket连接！当前人数为" + onlineCount);
        // 广播下线消息
        Message msg = new Message();
        msg.setDate(new Date());
        msg.setConsumer("0");
        msg.setContent(userAccount + "断开连接(ó﹏ò｡)");
        broadcast(JSON.toJSONString(msg, true));
        onlineUser();
    }



    /**
     * 连接错误
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("发生错误");
        throwable.printStackTrace();
    }

    /**
     * 操作变量
     */
    public static AtomicInteger getOnlineNumber() {
        return onlineCount;
    }

    public static void addOnlineCount() {
        onlineCount.incrementAndGet();
    }

    public static void subOnlineCount() {
        onlineCount.decrementAndGet();
    }

    public static ConcurrentHashMap<String, Session> getSessionPools() {
        return sessionPools;
    }

}
