package com.example.easypc.parse;

import com.example.easypc.data.entity.Source;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class CitilinkProductParser implements ProductParser {

    private final WebDriverPool driverPool;

    @Autowired
    public CitilinkProductParser(WebDriverPool driverPool) {
        this.driverPool = driverPool;
    }

    @Override
    public ProductData parse(Source source, String category) {
        WebDriver driver = driverPool.getDriver();
        String url = source.getSource();
        try {
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1")));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String productName = (String) js.executeScript(
                    "return Array.from(document.querySelectorAll('h1'))" +
                            ".map(e => e.textContent.trim())" +
                            ".find(t => t.length > 0);"
            );
            productName = cleanName(productName);

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("span[class*='MainPriceNumber']")));
            String productPrice = driver.findElement(By.cssSelector("span[class*='MainPriceNumber']")).getText();

            String imageUrl = driver.findElement(By.cssSelector("meta[property='og:image']")).getAttribute("content");
            Long urlId = Long.valueOf(source.getId());

            Map<String, String> characteristics = extractCharacteristics(category, url, driver);

            return new ProductData(productName, productPrice, category, imageUrl, urlId, characteristics);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
        return null;
    }

    @Override
    public boolean supports(String url) {
        return url.contains("citilink.ru");
    }

    private Map<String, String> extractCharacteristics(String category, String url, WebDriver driver)
    {
        Map<String, Map<String, String>> categoryMappings = Map.of(
                "cpu", Map.of(
                        "Сокет", "Сокет",
                        "Тепловыделение", "Тепловыделение (TDP)",
                        "Встроенное графическое ядро", "Встроенная графика",
                        "Частота", "Частота"
                ),
                "gpu", Map.of(
                        "Объем видеопамяти", "Объем видеопамяти",
                        "Тип видеопамяти", "Тип видеопамяти",
                        "Максимальное энергопотребление", "Тепловыделение (TDP)"
                ),
                "psu", Map.of(
                        "Мощность", "Мощность"
                ),
                "ram", Map.of(
                        "Количество модулей", "Количество модулей в комплекте",
                        "Тип памяти", "Тип памяти",
                        "Объем одного модуля", "Объем одного модуля",
                        "Тактовая частота", "Частота"
                ),
                "motherboard", Map.of(
                        "Сокет", "Сокет",
                        "Чипсет", "Чипсет",
                        "Тип памяти", "Тип памяти",
                        "Разъемов M.2", "Количество разъемов M.2"
                ),
                "fan", Map.of(
                        "Совместимые разъёмы CPU", "Сокет"
                ),
                "hdd", Map.of(
                        "Объем накопителя", "Объем накопителя"
                ),
                "ssd", Map.of(
                        "Объем накопителя", "Объем накопителя"
                )
        );
        Map<String, String> mapping = categoryMappings.getOrDefault(category, Map.of());
        Map<String, String> result = new HashMap<>();
        try {

            String productId = extractProductIdFromUrl(url);
            if (productId == null || productId.isEmpty()) {
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
                            String mappedKey = mapping.get(key);
                            // Нормализация значений
                            if ("Частота".equals(mappedKey)) {
                                String normalizedFrequency = normalizeFrequencyToMHz(value);
                                if (normalizedFrequency != null) {
                                    value = normalizedFrequency.toString();
                                }
                            }
                            if ("Встроенная графика".equals(mappedKey)) {
                                value = normalizeGraphicsCore(value);
                            }
                            result.put(mappedKey, value);
                        }
                    }
                }
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

    private String normalizeGraphicsCore(String value) {
        if (value == null) return "нет";
        String lower = value.toLowerCase();
        if (lower.contains("нет") || lower.contains("отсутствует") || lower.contains("n/a") || lower.contains("без")) {
            return "нет";
        }
        return value.trim();
    }

    private String normalizeFrequencyToMHz(String text) {
        if (text == null) return null;

        text = text.toLowerCase().replace(",", ".").replaceAll("[^0-9.]", " ");
        Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            double value = Double.parseDouble(matcher.group(1));
            if (value <= 10) {
                return Math.round(value * 1000) + " Мгц";
            } else {
                return Math.round(value) + " Мгц";
            }
        }
        return null;
    }

    private String cleanName(String name) {
        if (name == null) return null;
        return name.replaceAll("\\s*\\[[^]]*\\]", "").trim();
    }
}