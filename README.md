# whatsapp-messager
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
3. Send message on http://<host>:<port>/send/<message>/to/<phone number>
