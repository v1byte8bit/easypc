package com.example.easypc.data.parse;

import com.example.easypc.data.entity.Source;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;
import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class CitilinkProductParser implements ProductParser {

    private final WebDriver driver;

    public CitilinkProductParser() {
        System.setProperty("webdriver.chrome.driver", "C:/chromedriver-win64/chromedriver.exe");
        String chromeDriverPath = "C:/chromedriver-win64/chromedriver.exe";
        File driverFile = new File(chromeDriverPath);
        if (!driverFile.exists()) {
            throw new RuntimeException("ChromeDriver not found at " + chromeDriverPath);
        }
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Безголовый режим
        this.driver = new ChromeDriver(options);
    }

    @Override
    public ProductData parse(Source source, String category) {
        String url = source.getSource();
        try {
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            String productName = driver.findElement(By.cssSelector("div[data-meta-name='ProductHeaderLayout__title'] h1")).getText();
            String productPrice = driver.findElement(By.cssSelector("span[class*='MainPriceNumber']")).getText();
            String imageUrl = driver.findElement(By.cssSelector("meta[property='og:image']")).getAttribute("content");
            Long urlId = Long.valueOf(source.getId());
            Map<String, String> characteristics = extractCharacteristics(category,url);

            return new ProductData(productName, productPrice, category, imageUrl, urlId, characteristics);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean supports(String url) {
        return url.contains("citilink.ru");
    }

    private Map<String, String> extractCharacteristics(String category, String url) {
        Map<String, Map<String, String>> categoryMappings = Map.of(
                "cpu", Map.of(
                        "Сокет", "socket",
                        "Тепловыделение","tdp"
                ),
                "gpu", Map.of(
                        "Объем видеопамяти", "memory_size",
                        "Тип видеопамяти", "memory_type",
                        "Максимальное энергопотребление", "gpu_tdp"
                ),
                "psu", Map.of(
                        "Мощность", "power"
                ),
                "ram", Map.of(
                        "Количество модулей", "count",
                        "Тип памяти", "type",
                        "Объем одного модуля", "volume",
                        "Тактовая частота", "frequency"
                ),
                "motherboard", Map.of(
                        "Сокет", "socket",
                        "Чипсет", "chipset",
                        "Тип памяти","ram_type",
                        "Разъемов M.2","m2_count"
                ),
                "fan", Map.of(
                        "Совместимые разъёмы CPU", "socket"
                ),
                "hdd", Map.of(
                        "Объем накопителя", "hdd_volume"
                ),
                "ssd", Map.of(
                        "Объем накопителя", "ssd_volume"
                )
        );
        Map<String, String> mapping = categoryMappings.getOrDefault(category, Map.of());
        Map<String, String> result = new HashMap<>();
        try {

            String productId = extractProductIdFromUrl(url);
            if (productId == null || productId.isEmpty()) {
                System.out.println("Не удалось извлечь productId из URL");
                return result;
            }

            //GraphQL-запрос
        String query = """
            {
                product(filter: {id: "%s"}) {
                    propertiesGroup {
                        name
                        properties {
                            name
                            value
                        }
                    }
                 }
            }
        """.formatted(productId).replace("\n", "");
        //Скрипт для асинхронного запроса
        String script = """
            const callback = arguments[arguments.length - 1];
            fetch('https://www.citilink.ru/graphql/', { 
                method: 'POST', 
                headers: { 'Content-Type': 'application/json' }, 
                body: JSON.stringify({ query: '%s' }) 
            })
            .then(res => res.json())
            .then(data => callback(data))
            .catch(err => callback({error: err.message}));
        """.formatted(query);
        Object response = ((JavascriptExecutor) driver).executeAsyncScript(script);
            if (response instanceof Map<?, ?> json && json.containsKey("data")) {
                Map<?, ?> data = (Map<?, ?>) json.get("data");
                Map<?, ?> product = (Map<?, ?>) data.get("product");
                List<?> propertiesGroups = (List<?>) product.get("propertiesGroup");
                for (Object groupObj : propertiesGroups) {
                    Map<?, ?> group = (Map<?, ?>) groupObj;
                    List<?> properties = (List<?>) group.get("properties");

                    for (Object propObj : properties) {
                        Map<?, ?> prop = (Map<?, ?>) propObj;
                        String key = (String) prop.get("name");
                        String value = (String) prop.get("value");

                        if (mapping.containsKey(key)) {
                            result.put(mapping.get(key), value);
                        }
                    }
                }
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String extractProductIdFromUrl(String url) {
        Pattern pattern = Pattern.compile("-(\\d+)/?");
        Matcher matcher = pattern.matcher(url);
        String productId = null;

        while (matcher.find()) {
            productId = matcher.group(1);
        }

        return productId;
    }
}