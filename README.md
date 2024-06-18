# araceli-backend
Backend for the Araceli File Manager Application.

### Deployment of the backend
1. Clone this repository and cd into it.
2. Create a PostgreSQL database. Per default it should be called mongodb2. You may adjust the database name by editing the **application.properties** file.
3. Create an environment variable called PATH_TO_FS. The value of this variable must be the path to the folder where the file structure for users will be created. Alternatively, you can edit the value of at.araceli.backend.path-to-file-system in **application.properties**.
##### How to set environment variables?
If you use an superior operating system (UNIX-Like), add the path to your .bashrc / .zshrc or whatever other shell you use by inserting:

````shell
export PATH_TO_FS=path/to/folder
````

If you're on Windows: Look at this heated discussion https://stackoverflow.com/questions/5898131/set-a-persistent-environment-variable-from-cmd-exe
or press Windows + R -> enter "SystemPropertiesAdvanced.exe" -> Environment Variables...

**Make sure to restart IntelliJ IDEA / the terminal / source your .bashrc or .zshrc file after doing that.**

4. Build & run the backend by executing the following command in the directory:
````shell
./mvnw spring-boot::run
````

