package com.example.easypc.parse;

import com.example.easypc.data.entity.Source;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
public class OneTwoThreeProductParser implements ProductParser {
    @Override
    public ProductData parse(Source source, String category) {
        String url = source.getSource();
        try {
            Connection connection = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                    .timeout(5000)
                    .ignoreHttpErrors(true)
                    .followRedirects(true);
            Document doc = connection.get();

            String productName = doc.select("#ga-ecommerce-data").attr("data-product-name");
            String productPrice = doc.select("#ga-ecommerce-data").attr("data-product-price");
            String imageUrl = doc.select("meta[property=og:image:url]").attr("content");
            Long urlId = Long.valueOf(source.getId());
            Map<String, String> characteristics = extractCharacteristics(doc, category);

            return new ProductData(productName, productPrice, category, imageUrl, urlId, characteristics);

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean supports(String url) {
        return url.contains("123.ru");
    }

    private Map<String, String> extractCharacteristics(Document doc, String category) {
        Map<String, Map<String, String>> categoryMappings = Map.of(
                "cpu", Map.of(
                        "Socket", "socket",
                        "Типичная рассеиваемая мощность (TDP)", "tdp",
                        "Наличие встроенного графического ядра", "graph",
                        "Рабочая частота процессора", "frequency"
                ),
                "psu", Map.of(
                        "Мощность", "power"
                ),
                "ram", Map.of(
                        "Количество модулей памяти в комплекте, шт.", "count",
                        "Тип модуля памяти", "type",
                        "Объем одного модуля памяти", "volume",
                        "Рабочая частота, МГц", "frequency"
                ),
                "motherboard", Map.of(
                        "Разъем CPU", "socket",
                        "Чипсет (Intel)", "chipset",
                        "Чипсет (AMD)", "chipset",
                        "Поддержка оперативной памяти", "ram_type",
                        "Количество слотов M.2", "m2_count"
                ),
                "fan", Map.of(
                        "Совместимые разъёмы CPU", "socket"
                ),
                "hdd", Map.of(
                        "Ёмкость", "hdd_volume"
                ),
                "ssd", Map.of(
                        "Объём", "ssd_volume"
                )
        );

        Map<String, String> mapping = categoryMappings.getOrDefault(category, Map.of());
        Map<String, String> characteristics = new HashMap<>();
        Elements lines = doc.select("#tab-char .line");

        for (Element line : lines) {
            Element keyElement = line.selectFirst("strong");
            Element valueElement = line.selectFirst("span");

            if (keyElement != null && valueElement != null) {
                String key = keyElement.text().trim();
                if (mapping.containsKey(key)) {
                    String normalizedKey = mapping.get(key);
                    String value = valueElement.text().trim();
                    if (normalizedKey.equals("socket")) {
                        value = normalizeSocket(value);
                    }
                    if (normalizedKey.equals("tdp")) {
                        value = String.valueOf(normalizePower(value));
                    }
                    characteristics.put(normalizedKey, value);
                }
            }
        }
        return characteristics;
    }

    public String normalizeSocket(String socket) {
        if (socket == null) return null;
        socket = socket.replace("AMD ", "");
        return socket.trim();
    }

    private Integer normalizePower(String text) {
        if (text == null) return null;
        text = text.replaceAll("[^0-9]", "");
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}