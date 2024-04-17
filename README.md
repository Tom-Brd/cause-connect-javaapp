# How to install the application on Mac OS
1. Compile the application using the following command:
    ```bash
    mvn clean install -Pproduction
    ```
2. Rename the -exec compiled jar file to causeconnect.jar
3. Put the compiled jar file in CauseConnect folder
4. Create a folder plugins in the CauseConnect folder
5. CD in CauseConnect folder
6. Create an Application using the following command:
    ```bash
    jpackage --type dmg \         
         --input . \
         --name CauseConnect \
         --main-jar causeconnect.jar \
         --main-class org.springframework.boot.loader.launch.JarLauncher
    ```
   The input is the folder where the jar file is located.
7. Click on the CauseConnect.dmg file to install the application.
8. The application will be installed in the Applications folder.
9. Open the application by clicking on the CauseConnect icon.

If there is a new version of the application, it will be updated automatically when the application is opened.
