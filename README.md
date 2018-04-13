# whatsapp-messager [![CircleCI](https://circleci.com/gh/thcathy/whatsapp-messager/tree/master.svg?style=svg)](https://circleci.com/gh/thcathy/whatsapp-messager/tree/master)
Send WhatsApp message by web interface through selenium test.

## Prerequisite
1. A hosting Operating System with GUI
2. Install Google Chrome
3. Download Selenium chromedriver
4. Must start this server in GUI
5. Keep browser open and server running

## Send message
1. Start server
  java -jar whatsapp-messager.jar
2. Login whatsapp by scanning barcode on opened browser
   (or get barcode from - ```http://<host>:<port>/login/barcode```)
3. Send message by
```bash
http://<host>:<port>/sendto/<phone number>?message=<message to send>
```