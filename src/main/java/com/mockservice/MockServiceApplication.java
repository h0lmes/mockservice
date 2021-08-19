package com.mockservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

@SpringBootApplication
public class MockServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(MockServiceApplication.class);

    public static void main(String[] args) {
        ConfigurableEnvironment environment = SpringApplication.run(MockServiceApplication.class, args).getEnvironment();
        logStartup(environment);
    }

    private static void logStartup(Environment env) {
        String appName = env.getProperty("spring.application.name");
        String serverPort = env.getProperty("server.port");
        String contextPath = env.getProperty("server.servlet.context-path");
        if (contextPath == null || contextPath.isEmpty()) {
            contextPath = "/";
        }
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            // ignore
        }

        String msg = String.format("%s is running at: http://localhost:%s%s and http://%s:%s%s (dev mode UI http://localhost:3000%s)",
                appName, serverPort, contextPath, hostAddress, serverPort, contextPath, contextPath);
        String line = repeat('-', msg.length());
        log.info("\n{}\n{}\n{}", line, msg, line);
    }

    private static String repeat(char ch, int count) {
        char[] chars = new char[count];
        Arrays.fill(chars, ch);
        return new String(chars);
    }
}
