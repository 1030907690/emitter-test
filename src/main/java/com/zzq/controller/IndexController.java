package com.zzq.controller;


import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: zzq
 * @date: 4/29/2025 9:50 PM
 */
@RestController
@RequestMapping("/api/index")
public class IndexController {
    private final Logger log = LoggerFactory.getLogger(IndexController.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(2000);


    private final List<String> replyData = Arrays.asList("我是", "您的AI助手", "有什么可以帮您", "我是", "您的AI助手", "有什么可以帮您");
//private final List<String> replyData = Arrays.asList("我是", "您的AI助手");

    @RequestMapping("/chat")
    @CrossOrigin
    public SseEmitter chat(String query) {
        SseEmitter emitter = new SseEmitter(180000L);

        executorService.execute(() -> {
            try {
                for (int i = 0; i < replyData.size(); i++) {
                    String value = replyData.get(i);
                    emitter.send(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    Thread.sleep(1000);
                }
                emitter.send(SseEmitter.event().name("end").data("[DONE]"));
                Thread.sleep(1000);
                emitter.complete();
            } catch (Exception e) {
                log.error("其他的请求聊天异常 {}", e);
                emitter.completeWithError(e);
                throw new RuntimeException(e);
            }
        });
        log.info("返回emitter");
        return emitter;
    }

    /**
     * 返回ResponseBodyEmitter灵活性强，也可以自己构造标准的SSE返回
     * @param response
     * @return
     */
    @RequestMapping(value = "/responseBodyEmitter")
    @CrossOrigin
    public ResponseBodyEmitter responseBodyEmitter(HttpServletResponse response) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(180000L);
        executorService.execute(() -> {
            try {
                for (String value : replyData) {
                    emitter.send(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    Thread.sleep(1000);
                }
                emitter.complete();
            } catch (Exception e) {
                log.error("其他的请求聊天异常 {}", e);
                emitter.completeWithError(e);
                throw new RuntimeException(e);
            }
        });
        return emitter;
    }

}
