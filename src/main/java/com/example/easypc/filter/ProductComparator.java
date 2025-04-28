package com.example.easypc.filter;

import com.example.easypc.parse.ProductData;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProductComparator {

    private final LevenshteinDistance levenshtein = new LevenshteinDistance();

    public boolean areSameProduct(ProductData a, ProductData b) {
        // Fuzzy-сравнение названий
        String nameA = normalizeName(a.getName());
        String nameB = normalizeName(b.getName());

        int distance = levenshtein.apply(nameA, nameB);
        int maxLength = Math.max(nameA.length(), nameB.length());

        double similarity = 1.0 - ((double) distance / maxLength);
        /*System.out.println("Name A: " + nameA);
        System.out.println("Name B: " + nameB);
        System.out.println("Similarity: " + similarity);*/
        if (similarity > 0.70) {
            Map<String, String> charA = a.getCharacteristics();
            Map<String, String> charB = b.getCharacteristics();
            return charA.equals(charB);
        }
        return false;
    }

    private String normalizeName(String name) {
        return name == null ? "" : name.toLowerCase()
                .replaceAll("[^a-zа-я0-9]", "");
    }
}