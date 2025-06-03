package com.example.easypc.parse;

import com.example.easypc.data.entity.Source;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Bit28ProductParser implements ProductParser {

    private final WebDriverPool driverPool;

    @Autowired
    public Bit28ProductParser(WebDriverPool driverPool) {
        this.driverPool = driverPool;
    }

    @Override
    public ProductData parse(Source source, String category) {
        WebDriver driver = driverPool.getDriver();
        String url = source.getSource();

        try {
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            String productName = wait.until(d -> {
                try {
                    return d.findElement(By.cssSelector("h1.product__title")).getText().trim();
                } catch (Exception e) {
                    List<WebElement> h1Elements = d.findElements(By.tagName("h1"));
                    if (!h1Elements.isEmpty()) {
                        return h1Elements.get(0).getText().trim();
                    }
                    throw new NoSuchElementException("Название товара не найдено");
                }
            });

            String productPrice = wait.until(d -> {
                try {
                    WebElement priceElement = d.findElement(By.cssSelector("div.pr-prices__price.js-product-price"));
                    String priceText = priceElement.getText()
                            .replaceAll("[^0-9]", "")
                            .trim();
                    if (priceText.isEmpty()) {
                        throw new RuntimeException("Цена пустая");
                    }
                    return priceText;
                } catch (Exception e) {
                    try {
                        List<WebElement> priceElements = d.findElements(By.cssSelector("div[class*='price']"));
                        for (WebElement el : priceElements) {
                            String text = el.getText().replaceAll("[^0-9]", "").trim();
                            if (!text.isEmpty()) {
                                return text;
                            }
                        }
                    } catch (Exception ex) {
                    }
                    throw new NoSuchElementException("Цена товара не найдена");
                }
            });

            String imageUrl = wait.until(d -> {
                try {
                    return d.findElement(By.cssSelector("img.product-gallery__image")).getAttribute("src");
                } catch (Exception e) {
                    try {
                        return d.findElement(By.cssSelector("meta[property='og:image']")).getAttribute("content");
                    } catch (Exception ex) {
                        return null;
                    }
                }
            });
            Map<String, String> characteristics = new HashMap<>();
            Map<String, String> mappedCharacteristics = extractCharacteristics(category, driver);
            characteristics.putAll(mappedCharacteristics);

            Long urlId = Long.valueOf(source.getId());

            return new ProductData(
                    cleanName(productName),
                    productPrice,
                    category,
                    imageUrl,
                    urlId,
                    characteristics
            );

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean supports(String url) {
        return url.contains("28bit.ru");
    }

    private Map<String, String> extractCharacteristics(String category, WebDriver driver) {
        Map<String, Map<String, String>> categoryMappings = Map.of(
                "cpu", Map.of(
                        "Сокет", "Сокет",
                        "Базовая мощность процессора", "Тепловыделение (TDP)",
                        "Интегрированное графическое ядро", "Встроенная графика",
                        "Частота процессора", "Частота"
                ),
                "gpu", Map.of(
                        "Объём видеопамяти", "Видеопамять",
                        "Тип видеопамяти", "Тип памяти",
                        "Энергопотребление", "TDP"
                ),
                "motherboard", Map.of(
                        "Сокет", "Сокет",
                        "Чипсет", "Чипсет",
                        "Тип памяти", "Тип памяти",
                        "Количество разъемов M.2", "Количество разъемов M.2"
                ),
                "ram", Map.of(
                        "Количество модулей в комплекте", "Количество модулей в комплекте",
                        "Тип памяти", "Тип памяти",
                        "Объем одного модуля памяти", "Объем одного модуля",
                        "Тактовая частота", "Частота"
                ),
                "psu", Map.of(
                        "Мощность Вт", "Мощность"
                ),
                "fan", Map.of(
                        "Поддержка сокета", "Сокет"
                ),
                "hdd", Map.of(
                        "Объем памяти", "Объем накопителя"
                ),
                "ssd", Map.of(
                        "Объем памяти", "Объем накопителя"
                )
        );

        Map<String, String> mapping = categoryMappings.getOrDefault(category, Map.of());
        Map<String, String> result = new HashMap<>();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));


            List<WebElement> featureItems = wait.until(d ->
                    d.findElements(By.cssSelector("div.features__item")));

            for (WebElement item : featureItems) {
                try {
                    String name = item.findElement(By.cssSelector("div.features__name"))
                            .getText()
                            .replaceAll("\\s+", " ")
                            .trim();

                    if (mapping.containsKey(name)) {
                        String value = item.findElement(By.cssSelector("div.features__value"))
                                .getText()
                                .replaceAll("\\s+", " ")
                                .trim();

                        String mappedKey = mapping.get(name);
                        result.put(mappedKey, normalizeCharacteristicValue(mappedKey, value));
                    }

                } catch (Exception e) {
                }
            }
            applyCategorySpecificNormalization(category, result);
        } catch (Exception e) {
        }
        return result;
    }

    private void applyCategorySpecificNormalization(String category, Map<String, String> characteristics) {
        switch (category) {
            case "cpu":
                if (characteristics.containsKey("Частота")) {
                    String freq = characteristics.get("Частота процессора");
                    if (freq.contains("-")) {
                        String[] ranges = freq.split("-");
                        characteristics.put("Базовая частота", ranges[0].trim());
                        characteristics.put("Максимальная частота", ranges[1].trim());
                    }
                }
                break;

            case "gpu":
                if (characteristics.containsKey("Видеопамять")) {
                    String mem = characteristics.get("Видеопамять");
                    characteristics.put("Видеопамять", mem.replaceAll("[^0-9]", "") + " ГБ");
                }
                break;
        }
    }

    private String normalizeCharacteristicValue(String key, String value) {
        if (value == null || value.isEmpty()) {
            return "не указано";
        }
        value = value.replaceAll("\\s+", " ").trim();
        switch(key) {
            case "Тепловыделение (TDP)":
                return value.replaceAll("[^0-9]", "");

            case "Встроенная графика":
                if (value.toLowerCase().contains("есть") ||
                        value.toLowerCase().contains("да") ||
                        value.equalsIgnoreCase("+")) {
                    return "есть";
                }
                return "нет";

            case "Сокет":
                return value.replaceAll("\\s+", "");

            case "Частота":
                if (value.contains("ГГ") || value.contains("Гг")) {
                    String num = value.replaceAll("[^0-9.]", "");
                    if (!num.isEmpty()) {
                        double ghz = Double.parseDouble(num);
                        return (int)(ghz * 1000) + " Мгц";
                    }
                } else if (value.matches(".*[0-9].*")) {
                    return value.replaceAll("[^0-9]", "") + " Мгц";
                }
                return value;

            default:
                return value;
        }
    }

    private String cleanName(String name) {
        if (name == null) return null;
        name = name.replaceAll("\\s*\\d+-\\d+$", "");
        name = name.replaceAll("\\s*\\[[^]]*\\]", "");
        name = name.replaceAll("\\s+", " ").trim();
        return name;
    }
}