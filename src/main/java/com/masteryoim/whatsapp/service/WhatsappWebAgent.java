package com.masteryoim.whatsapp.service;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.remote.http.JsonHttpCommandCodec;
import org.openqa.selenium.remote.http.JsonHttpResponseCodec;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collections;

@Service
public class WhatsappWebAgent {
    private static final Logger log = LoggerFactory.getLogger(WhatsappWebAgent.class);
    private static final String WHATSAPP_SITE = "https://web.whatsapp.com/";
    private static final String WHATSAPP_API = "https://web.whatsapp.com";
    private static final String SEND_MSG_SYNTAX = WHATSAPP_API + "/send?phone=%s&text=%s";
    private static final String LOGIN_BARCODE_IMG_XPATH = "//img[@alt=\"Scan me!\"]";
    private static final String ACTION_BUTTON_ID = "action-button";
    private static final String SEND_BUTTON_CLASS = "_2lkdt";
    private static final String INVALID_PHONE_BUTTON_DIV_CLASS = "_3QNwO";
    private static final String INVALID_PHONE_BUTTON_XPATH = "//div[@role=\"button\"]";
    private static final String START_UP_ID = "startup";

    @Value("${chromedrive.path}")
    private String chromeDrivePath;
    private RemoteWebDriver remoteWebDriver = null;

    @PostConstruct
    public void init() {
        try {
            System.setProperty("webdriver.chrome.driver", this.chromeDrivePath);
            remoteWebDriver = getExistingRemoteDriver();
        } catch (Exception e) {
            log.error("Error in init remote driver", e);
        }
    }

    public void close(){
        remoteWebDriver.quit();
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
        remoteWebDriver.navigate().to(apiUrl);

        /*try {
            (new WebDriverWait(remoteWebDriver, 30)).until(ExpectedConditions.visibilityOfElementLocated(By.id(ACTION_BUTTON_ID)));
            WebElement action = remoteWebDriver.findElement(By.id(ACTION_BUTTON_ID));

            logger.info("action button click");

            action.click();
        } catch(Exception e) {
            logger.error("cannot find the action button");
            return false;
        }

        //wait for the loading
        try {
            (new WebDriverWait(remoteWebDriver, 30)).until(ExpectedConditions.visibilityOfElementLocated(By.id(START_UP_ID)));
            (new WebDriverWait(remoteWebDriver, 30)).until(ExpectedConditions.invisibilityOfElementLocated(By.id(START_UP_ID)));
        } catch(Exception e) {
            logger.error("the loading screen does not disappear after 30s");
            return false;
        }*/

        //after the page load, if the phone is correct, the send button should show
        try{
            clickSend();
            return true;
        } catch (UnhandledAlertException e) {
            log.info("Accept unhandled alert");
            remoteWebDriver.switchTo().alert().accept();
            clickSend();
            return true;
        } catch(NoSuchElementException e) {
            //if the send button cannot be found, an error message should have pop up
            log.error("cannot find the send button");

            try{
                (new WebDriverWait(remoteWebDriver, 30)).until(ExpectedConditions.visibilityOfElementLocated(By.className(INVALID_PHONE_BUTTON_DIV_CLASS)));
                WebElement invalidDiv = remoteWebDriver.findElement(By.className(INVALID_PHONE_BUTTON_DIV_CLASS));
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
        (new WebDriverWait(remoteWebDriver, 30)).until(ExpectedConditions.visibilityOfElementLocated(By.className(SEND_BUTTON_CLASS)));
        WebElement send = remoteWebDriver.findElement(By.className(SEND_BUTTON_CLASS));
        send.click();
    }

    //check if the screen contain the element with id = side
    public Boolean isLoggedIn(){
        try{
            WebElement e  = remoteWebDriver.findElement(By.id("side"));

            if(e!=null){
                return true;
            }
        } catch(NoSuchElementException e) {
            log.error("Element with id=side cannnot be found");
        }

        return false;
    }

    private static RemoteWebDriver getExistingRemoteDriver() {
        RemoteWebDriver driver = null;

        SessionId sessionId = deserializeSessionId();
        URL url = deserializeUrl();

        if(sessionId!=null && url!=null) {
            driver = createDriverFromSession(sessionId, url);

            //test the driver, if the driver not found it will throw exception
            try {
                String currentUrl = driver.getCurrentUrl();
                if(currentUrl.equals(WHATSAPP_SITE)) {
                    try {
                        driver.findElement(By.id("side"));
                        log.info("whatapp is logged in");
                    } catch(NoSuchElementException e) {
                        log.error("whatapp is not logged in");
                        loadWhatsappPage(driver);
                    }
                } else {
                    loadWhatsappPage(driver);
                }
            } catch(Exception e) {
                log.error(e.getMessage(), e);
                driver = createNewRemoteDriver();
            }
        } else {
            driver = createNewRemoteDriver();
        }

        return driver;
    }

    private static void loadWhatsappPage(RemoteWebDriver driver){
        driver.navigate().to(WHATSAPP_SITE);
        (new WebDriverWait(driver, 30)).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(LOGIN_BARCODE_IMG_XPATH)));
    }

    private static RemoteWebDriver createNewRemoteDriver() {
        RemoteWebDriver driver = new ChromeDriver();
        loadWhatsappPage(driver);
        serializeSessionId(driver.getSessionId());
        HttpCommandExecutor executor = (HttpCommandExecutor) driver.getCommandExecutor();
        serializeUrl(executor.getAddressOfRemoteServer());

        return driver;
    }

    private static void serializeSessionId(SessionId sessionId){
        try {
            FileOutputStream fileOut = new FileOutputStream("./sessionId.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(sessionId.toString());
            out.close();
            fileOut.close();
            log.info("Serialized sessionId is saved");
        } catch (IOException e) {
            log.error("Error serialize session id", e);
        }
    }

    private static void serializeUrl(URL url){
        try {
            FileOutputStream fileOut = new FileOutputStream("./url.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(url);
            out.close();
            fileOut.close();
            log.info("Serialized url is saved");
        } catch (IOException e) {
            log.error("Error in serialize url", e);
        }
    }

    private static SessionId deserializeSessionId(){
        SessionId sessionId = null;
        try {
            FileInputStream fileIn = new FileInputStream("./sessionId.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            String sessionStr = (String) in.readObject();
            sessionId = new SessionId(sessionStr);
            in.close();
            fileIn.close();
        } catch (IOException e) {
            log.error("Error in deserialize session id", e);
        } catch (ClassNotFoundException e) {
            log.error("sessionId.ser not found", e);
        }
        return sessionId;
    }

    private static URL deserializeUrl(){
        URL url = null;
        try {
            FileInputStream fileIn = new FileInputStream("./url.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            url = (URL) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException e) {
            log.error("Error in deserialize url", e);
        } catch (ClassNotFoundException e) {
            log.error("url.ser not found", e);
        }
        return url;
    }

    //copy from http://tarunlalwani.com/post/reusing-existing-browser-session-selenium-java/
    private static RemoteWebDriver createDriverFromSession(final SessionId sessionId, URL command_executor){

        CommandExecutor executor = new HttpCommandExecutor(command_executor) {

            @Override
            public Response execute(Command command) throws IOException {
                Response response = null;
                if (command.getName() == "newSession") {
                    response = new Response();
                    response.setSessionId(sessionId.toString());
                    response.setStatus(0);
                    response.setValue(Collections.<String, String>emptyMap());

                    try {
                        Field commandCodec = null;
                        commandCodec = this.getClass().getSuperclass().getDeclaredField("commandCodec");
                        commandCodec.setAccessible(true);
                        commandCodec.set(this, new JsonHttpCommandCodec());

                        Field responseCodec = null;
                        responseCodec = this.getClass().getSuperclass().getDeclaredField("responseCodec");
                        responseCodec.setAccessible(true);
                        responseCodec.set(this, new JsonHttpResponseCodec());
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        log.error("Error in create driver from session", e);
                    }
                } else {
                    response = super.execute(command);
                }
                return response;
            }
        };

        return new RemoteWebDriver(executor, new DesiredCapabilities());
    }
}
