package com.masteryoim.whatsapp.service;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class WhatsappWebAgent {
    private static final Logger log = LoggerFactory.getLogger(WhatsappWebAgent.class);
    private static final String WHATSAPP_SITE = "https://web.whatsapp.com/";
    private static final String WHATSAPP_API = "https://web.whatsapp.com";
    private static final String SEND_MSG_SYNTAX = WHATSAPP_API + "/send?phone=%s&text=%s";
    private static final String LOGIN_BARCODE_IMG_XPATH = "//img[@alt=\"Scan me!\"]";
    private static final String SEND_BUTTON_CLASS = "_35EW6";
    private static final String INVALID_PHONE_BUTTON_DIV_CLASS = "_3QNwO";
    private static final String INVALID_PHONE_BUTTON_XPATH = "//div[@role=\"button\"]";

    @Value("${chromedrive.path}") private String chromeDrivePath;
    @Value("${chrome.user.folder.path}") private String userFolderPath;

    private RemoteWebDriver webDriver = null;

    @PostConstruct
    public void init() {
        try {
            System.setProperty("webdriver.chrome.driver", this.chromeDrivePath);
            webDriver = createNewRemoteDriver();
        } catch (Exception e) {
            log.error("Error in init remote driver", e);
        }
    }

    public void close(){
        webDriver.quit();
    }

    private void openGroupAndSend(String groupName, String message) {
        WebElement groupNameInput = waitAndGetWebElement("//*[@id='app']//label//input[@type='text']");
        groupNameInput.sendKeys(groupName);
        log.info("inputted group name");

        By groupSelector = By.xpath("//*[@class='_2EXPL aZ91u']");
        (new WebDriverWait(webDriver, 10)).until(ExpectedConditions.numberOfElementsToBe(groupSelector, 1));
        WebElement group = waitAndGetWebElement(groupSelector);
        group.click();
        log.info("group is selected");

        By buttonSelector = By.xpath("//*[@id='app']//div[@role='button']");
        (new WebDriverWait(webDriver, 10)).until(ExpectedConditions.elementToBeClickable(buttonSelector));
        WebElement button = waitAndGetWebElement(buttonSelector);
        button.click();
        log.info("clicked send to which group button");

        By textboxSelector = By.className("_2S1VP");
        WebElement textbox = waitAndGetWebElement(textboxSelector);
        textbox.sendKeys(Keys.ENTER);
    }

    private WebElement waitAndGetWebElement(String xPath) {
        try{
            (new WebDriverWait(webDriver, 10)).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
            return webDriver.findElement(By.xpath(xPath));
        } catch (UnhandledAlertException e) {
            log.info("Accept unhandled alert");
            webDriver.switchTo().alert().accept();
            (new WebDriverWait(webDriver, 10)).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
            return webDriver.findElement(By.xpath(xPath));
        }
    }

    private WebElement waitAndGetWebElement(By selector) {
        (new WebDriverWait(webDriver, 10)).until(ExpectedConditions.visibilityOfElementLocated(selector));
        return webDriver.findElement(selector);
    }

    public boolean sendToGroup(String groupName, String msg) {
        log.info("sendToGroup [{}] to [{}]", msg, groupName);

        if(!isLoggedIn()) return false;
        if(StringUtils.isEmpty(groupName)) return false;

        String apiUrl = String.format(SEND_MSG_SYNTAX, "", msg);
        webDriver.navigate().to(apiUrl);

        //after the page load, if the phone is correct, the send button should show
        try{
            openGroupAndSend(groupName, msg);
            return true;
        } catch(NoSuchElementException e) {
            log.error("cannot find the send button");
            return false;
        }
    }

    public Boolean sendMsg(String phoneNumber, String msg) {
        log.info("sendMsg [{}] to [{}]", msg, phoneNumber);

        if(!isLoggedIn()) {
            return false;
        }

        if(!StringUtils.isNumeric(phoneNumber)) {
            log.error("Phone number [{}] is not in correct format", phoneNumber);
            return false;
        }

        String apiUrl = String.format(SEND_MSG_SYNTAX, phoneNumber, msg);
        webDriver.navigate().to(apiUrl);

        //after the page load, if the phone is correct, the send button should show
        try{
            clickSend();
            return true;
        } catch (UnhandledAlertException e) {
            log.info("Accept unhandled alert");
            webDriver.switchTo().alert().accept();
            clickSend();
            return true;
        } catch(NoSuchElementException e) {
            //if the send button cannot be found, an error message should have pop up
            log.error("cannot find the send button");

            try{
                (new WebDriverWait(webDriver, 30)).until(ExpectedConditions.visibilityOfElementLocated(By.className(INVALID_PHONE_BUTTON_DIV_CLASS)));
                WebElement invalidDiv = webDriver.findElement(By.className(INVALID_PHONE_BUTTON_DIV_CLASS));
                WebElement invalid = invalidDiv.findElement(By.xpath(INVALID_PHONE_BUTTON_XPATH));
                invalid.click();
            } catch(NoSuchElementException e2) {
                log.error("cannot find the invalid button");
                return false;
            }

            return false;
        }
    }

    private void clickSend() {
        (new WebDriverWait(webDriver, 30)).until(ExpectedConditions.visibilityOfElementLocated(By.className(SEND_BUTTON_CLASS)));
        WebElement send = webDriver.findElement(By.className(SEND_BUTTON_CLASS));

        // retry loop to click send button until fully loaded
        int retry = 0;
        while (retry < 30) {
            retry++;
            try {
                log.info("click send");
                send.click();
                retry = Integer.MAX_VALUE;
            } catch (WebDriverException e) {
                if (e.toString().contains("is not clickable")) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e2) {
                        log.error("interrupted exception", e2);
                    }
                }
            }
        }
    }

    //check if the screen contain the element with id = side
    public Boolean isLoggedIn(){
        try{
            WebElement e  = webDriver.findElement(By.id("side"));
            return e != null;
        } catch(NoSuchElementException e) {
            log.error("Element with id=side cannnot be found");
        }
        return false;
    }

    private void loadWhatsappPage(RemoteWebDriver driver){
        driver.navigate().to(WHATSAPP_SITE);
        try {
            (new WebDriverWait(driver, 10)).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(LOGIN_BARCODE_IMG_XPATH)));
        } catch (Exception e) {
            log.info("No login barcode provided");
        }
    }

    public String getLoginBarcode() {
        if (webDriver == null)
            return "No url. Remote web driver not ready";

        if (isLoggedIn())
            return "No url. Already Login.";

        try {
            loadWhatsappPage(webDriver);
            WebElement img  = webDriver.findElement(By.xpath(LOGIN_BARCODE_IMG_XPATH));
            String barcode = img.getAttribute("src");
            log.info("login barcode: {}", barcode);
            return barcode;
        } catch(Exception e) {
            return "Cannot get login barcode. " + e.getMessage();
        }
    }

    private RemoteWebDriver createNewRemoteDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-data-dir=" + userFolderPath);
        RemoteWebDriver driver = new ChromeDriver(options);

        loadWhatsappPage(driver);
        return driver;
    }

}
