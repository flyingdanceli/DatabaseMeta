package net.ruis.databasemeta;

import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DatabaseMetaApplication extends Application {

    // Spring Boot 上下文
    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        context = SpringApplication.run(DatabaseMetaApplication.class, args);
        launch(args);  // 启动 JavaFX 应用
    }

    @Override
    public void start(Stage primaryStage) {
        // 从 Spring 上下文获取 JavaFXUI Bean
        JavaFXUI javafxUI = context.getBean(JavaFXUI.class);
        javafxUI.start(primaryStage);  // 加载并显示 JavaFX UI
    }

    @Override
    public void stop() {
        // 关闭 Spring 上下文
        context.close();
    }
}
