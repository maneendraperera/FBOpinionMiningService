Opinion Mining Application Version 1.0 01/08/2017

Opinion Mining Application can be reached at:
E-mail:		maneendra123@gmail.com

Installing Instructions
------------------------

Setting the Enviornment Variable
================================
1. Go to PC --> Enviornment Variables
2. Add a variable called APP_CONFIG
3. Set the value to folder location where Opinion Mining data files are located

Setting Configuration Details
==============================
1. Go to appconfig folder
2. Set configuration details in config.properties file
3. Set logging configuration details in log.properties file

Run the Application
==================== 
1. Open the cmd prompt
2. Go to application folder where Opinion Mining application is located
3. Run the command - "java -jar target/FbOpinionMiningApplication-0.0.1-SNAPSHOT.jar"
4. It will start the server 
5. Access http://localhost:8080/login via browser
6. Login the application using credentails test/123
7. Enter the post id which opinions need to be mined

format of the post id - {profileId_postId}
Eg:-
100013612860254_105490496581379
100013612860254_224072298056531

