package com.example.easypc.parse;

import com.example.easypc.data.entity.Source;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.*;
import java.time.Duration;
import java.util.NoSuchElementException;

import static java.util.Map.entry;

@Component
public class DNSProductParser implements ProductParser {

    private final WebDriverPool driverPool;

    @Autowired
    public DNSProductParser(WebDriverPool driverPool) {
        this.driverPool = driverPool;
    }

    @Override
    public ProductData parse(Source source, String category) {
        WebDriver driver = driverPool.getDriver();
        String url = source.getSource();
        try {
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            // 1. Получаем название товара (новые селекторы)
            String productName = getProductName(driver, wait);
            productName = cleanName(productName);

            // 2. Получаем цену товара
            String productPrice = getProductPrice(driver, wait);

            // 3. Получаем URL изображения
            String imageUrl = getProductImageUrl(driver);

            Long urlId = Long.valueOf(source.getId());
            Map<String, String> characteristics = extractCharacteristics(category, url, driver);

            return new ProductData(productName, productPrice, category, imageUrl, urlId, characteristics);

        } catch (Exception e) {
            return null;
        }
    }

    private String getProductName(WebDriver driver, WebDriverWait wait) {
        // Пробуем несколько вариантов селекторов для названия
        List<By> nameLocators = Arrays.asList(
                By.cssSelector("h1.product-card-top__title"), // Старый вариант
                By.cssSelector("h1.product-card__title"),     // Новый вариант
                By.xpath("//h1[contains(@class, 'title')]"), // Универсальный вариант
                By.cssSelector("h1.title")                    // Самый простой вариант
        );

        for (By locator : nameLocators) {
            try {
                WebElement nameElement = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                return nameElement.getText();
            } catch (TimeoutException e) {
                continue;
            }
        }
        throw new RuntimeException("Не удалось найти название товара на странице: " + driver.getCurrentUrl());
    }

    private String getProductPrice(WebDriver driver, WebDriverWait wait) {
        // Пробуем несколько вариантов селекторов для цены
        List<By> priceLocators = Arrays.asList(
                By.cssSelector("div.product-buy__price"),          // Основной вариант
                By.cssSelector("span.price"),                      // Альтернатива 1
                By.cssSelector("div.product-card-price__current"), // Альтернатива 2
                By.xpath("//div[contains(@class, 'price')]")       // Универсальный вариант
        );

        for (By locator : priceLocators) {
            try {
                WebElement priceElement = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                return priceElement.getText().replaceAll("[^0-9]", "").trim();
            } catch (TimeoutException e) {
                continue;
            }
        }

        throw new RuntimeException("Не удалось найти цену товара на странице: " + driver.getCurrentUrl());
    }

    private String getProductImageUrl(WebDriver driver) {
        try {
            return driver.findElement(By.cssSelector("img.product-images-slider__main-img"))
                    .getAttribute("src");
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public boolean supports(String url) {
        return url.contains("dns-shop.ru");
    }

    private Map<String, String> extractCharacteristics(String category, String url, WebDriver driver) {
        Map<String, String> result = new LinkedHashMap<>();

        try {
            // Ждем загрузки блока характеристик
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div.product-characteristics__group")));

            // Получаем все группы характеристик
            List<WebElement> groups = driver.findElements(
                    By.cssSelector("div.product-characteristics__group"));

            for (WebElement group : groups) {
                try {
                    // Название группы (например, "Основные характеристики")
                    String groupName = group.findElement(By.cssSelector("div.product-characteristics__group-title"))
                            .getText()
                            .trim();

                    // Все строки характеристик в группе
                    List<WebElement> specs = group.findElements(
                            By.cssSelector("div.product-characteristics__spec"));

                    for (WebElement spec : specs) {
                        try {
                            String name = spec.findElement(By.cssSelector("div.product-characteristics__spec-title"))
                                    .getText()
                                    .trim();

                            String value = spec.findElement(By.cssSelector("div.product-characteristics__spec-value"))
                                    .getText()
                                    .trim();

                            // Нормализация названий характеристик
                            String normalizedName = normalizeCharacteristicName(name);

                            // Нормализация значений для определенных характеристик
                            String normalizedValue = normalizeCharacteristicValue(normalizedName, value);

                            result.put(normalizedName, normalizedValue);
                        } catch (NoSuchElementException e) {
                            continue;
                        }
                    }
                } catch (NoSuchElementException e) {
                    continue;
                }
            }

            // Дополнительная обработка для конкретных категорий
            processCategorySpecificCharacteristics(category, result);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    // Нормализация названий характеристик
    private String normalizeCharacteristicName(String name) {
        // Удаляем двоеточия и лишние пробелы
        name = name.replace(":", "").trim();

        // Словарь для унификации названий
        Map<String, String> normalizationMap = Map.ofEntries(
                entry("Сокет процессора", "Сокет"),
                entry("Тип сокета", "Сокет"),
                entry("Тепловыделение (TDP)", "TDP"),
                entry("Тепловыделение", "TDP"),
                entry("Встроенная графика", "Графическое ядро"),
                entry("Тактовая частота", "Частота"),
                entry("Базовая частота", "Частота"),
                entry("Объем памяти", "Объем видеопамяти"),
                entry("Объем накопителя", "Емкость"),
                entry("Тип памяти", "Тип памяти"),
                entry("Энергопотребление", "TDP")
        );

        return normalizationMap.getOrDefault(name, name);
    }

    // Нормализация значений характеристик
    private String normalizeCharacteristicValue(String name, String value) {
        // Обработка частот (перевод в MHz/GHz)
        if (name.contains("Частота")) {
            return normalizeFrequency(value);
        }

        // Обработка TDP (удаляем "Вт")
        if (name.equals("TDP")) {
            return value.replace("Вт", "").trim();
        }

        // Обработка объема памяти
        if (name.contains("Объем") || name.contains("Емкость")) {
            return value.replace("ГБ", "").replace("ТБ", "000 ГБ").trim();
        }

        return value;
    }

    // Обработка частотных характеристик
    private String normalizeFrequency(String frequency) {
        if (frequency.contains("МГц")) {
            return frequency.replace("МГц", "").trim();
        }
        if (frequency.contains("ГГц")) {
            try {
                double ghz = Double.parseDouble(frequency.replace("ГГц", "").trim());
                return String.valueOf((int)(ghz * 1000));
            } catch (NumberFormatException e) {
                return frequency;
            }
        }
        return frequency;
    }

    // Дополнительная обработка для конкретных категорий
    private void processCategorySpecificCharacteristics(String category, Map<String, String> characteristics) {
        switch (category.toLowerCase()) {
            case "cpu":
                // Для процессоров добавляем количество ядер/потоков в название
                if (characteristics.containsKey("Количество ядер")) {
                    String cores = characteristics.get("Количество ядер");
                    String threads = characteristics.getOrDefault("Количество потоков", cores);
                    characteristics.put("Ядер/потоков", cores + "/" + threads);
                }
                break;

            case "gpu":
                // Для видеокарт объединяем информацию о памяти
                if (characteristics.containsKey("Объем видеопамяти") &&
                        characteristics.containsKey("Тип видеопамяти")) {
                    String memorySize = characteristics.get("Объем видеопамяти");
                    String memoryType = characteristics.get("Тип видеопамяти");
                    characteristics.put("Видеопамять", memorySize + " ГБ " + memoryType);
                }
                break;

            case "ram":
                // Для памяти добавляем тайминги, если они есть
                if (characteristics.containsKey("Тайминги")) {
                    String timings = characteristics.get("Тайминги");
                    characteristics.put("Тайминги", timings.replace("CL", "").trim());
                }
                break;
        }
    }

    private String cleanName(String name) {
        if (name == null) return null;
        return name.replaceAll("\\s*\\[[^]]*\\]", "").trim();
    }
}