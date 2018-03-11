#CBRM-gui

This prototype demonstrates the firsts steps taken to visualize the Contexts and Rules parsed out of Flora-Files (.flr) and Flora-Shell Comannd Output. 

## Run in (eg. Eclipse) IDE 
To start up CBRM-gui, install all Maven-Dependencies and run '/gui/src/main/java/dke/cbrm/CbrmApplication.java' as Spring-Boot Application. Verify that lombok.jar is included in IDE libraries-folder and installed (according to https://projectlombok.org/setup/eclipse and https://stackoverflow.com/a/3425327 ) and .flr Files are under a Folder <ws>/OO/, where <ws> is your current workspace. After authenticating with the Credentials initialized during Spring-Security-Context Startup (with '/gui/src/main/java/dke/cbrm/UserRolePrivilegeInitializer.java') under 'localhost:8080' Login-Page the User will be allowed to access cbrm-gui main-view for showing contexts-hierarchy and rules related to them. User 'sa' is allowed to access H2-Console under 'localhost:8080/console' pass
