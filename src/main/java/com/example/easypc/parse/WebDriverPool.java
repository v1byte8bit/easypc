package com.example.easypc.parse;

import jakarta.annotation.PreDestroy;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebDriverPool {
    private final ChromeOptions options;
    private final ThreadLocal<WebDriver> threadLocalDriver;

    public WebDriverPool() {
        System.setProperty("webdriver.chrome.driver", "C:/chromedriver-win64/chromedriver.exe");

        options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--blink-settings=imagesEnabled=false");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation", "disable-popup-blocking"));
        options.setExperimentalOption("useAutomationExtension", false);
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);

        threadLocalDriver = ThreadLocal.withInitial(() -> new ChromeDriver(options));
    }

    public WebDriver getDriver() {
        return threadLocalDriver.get();
    }

    @PreDestroy
    public void closeAllDrivers() {
        WebDriver driver = threadLocalDriver.get();
        if (driver != null) {
            driver.quit();
            threadLocalDriver.remove();
        }
    }
}
