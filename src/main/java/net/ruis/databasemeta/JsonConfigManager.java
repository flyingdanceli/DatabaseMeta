package net.ruis.databasemeta;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class JsonConfigManager {

    private JsonObject rootNode;
    private String configFilePath;

    public JsonConfigManager(String configFilePath) {
        this.configFilePath = configFilePath;
        rootNode = new JsonObject();  // 初始为空的 JSON 对象
    }

    // 通过路径获取配置项，支持路径访问（点分隔）
    public String getConfig(String path) {
        String[] keys = path.split("\\.");  // 按点分割路径
        JsonElement currentElement = rootNode;

        // 遍历每个键，逐步深入嵌套的 JSON 对象
        for (String key : keys) {
            if (currentElement != null && currentElement.isJsonObject()) {
                currentElement = currentElement.getAsJsonObject().get(key);
            } else {
                return null;  // 如果路径不正确，返回 null
            }
        }
        return currentElement != null ? currentElement.getAsString() : null;
    }

    // 通过路径设置配置项，支持路径访问（点分隔）
    public void setConfig(String path, String value) {
        String[] keys = path.split("\\.");  // 按点分割路径
        JsonElement currentElement = rootNode;

        for (int i = 0; i < keys.length - 1; i++) {
            if (currentElement != null && currentElement.isJsonObject()) {
                currentElement = currentElement.getAsJsonObject().get(keys[i]);
            } else {
                // 如果中间的对象不存在，创建它
                JsonObject newObject = new JsonObject();
                ((JsonObject) currentElement).add(keys[i], newObject);
                currentElement = newObject;
            }
        }

        // 最后一个键设置值
        ((JsonObject) currentElement).addProperty(keys[keys.length - 1], value);
    }

    // 加载 JSON 配置文件
    public void loadConfig() {
        File configFile = new File(configFilePath);

        // 打印出文件的绝对路径
        System.out.println("正在加载的配置文件的绝对路径: " + configFile.getAbsolutePath());

        // 使用 FileReader 读取文件内容
        try (FileReader reader = new FileReader(configFile)) {
            // 使用 JsonParser 解析文件内容
            rootNode = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            System.out.println("配置文件加载失败，使用默认配置");
            rootNode = new JsonObject();  // 使用空的 JSON 对象
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("解析 JSON 时发生错误");
            rootNode = new JsonObject();
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        JsonConfigManager configManager = new JsonConfigManager("config.json");

        // 加载配置并输出
        configManager.loadConfig();

        // 获取和设置配置项
        System.out.println("应用名称: " + configManager.getConfig("app.name"));
        System.out.println("应用版本: " + configManager.getConfig("app.version"));
    }
}
