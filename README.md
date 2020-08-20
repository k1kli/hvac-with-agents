# hvac-with-agents
### Running in IntelliJ IDEA
File > New > Project from version Control
Paste in this project's github url

When project is opened there will be Maven projects need to be imported box in bottom right corner. Select enable auto-import.

Click on maven section on right edge of the screen.
Press wrench icon (maven settings)
Expand maven in menu on the left side and choose Runner.
Select Delegate IDE build/run actions to Maven and apply

In top right corner press Add configuration.
Click the + button and choose application.
Select jade.Boot as the main class and specify arguments, for example:
-gui -agents a:HelloWorldAgent

You can name this configuration apropriately.

Press ok and run.

### Database
To use this project you need MySQL database running locally on port 3306.
[Download link](https://dev.mysql.com/downloads/)

Database initialization query can be found in src/main/scripts/create_database.sql.
To enter it on windows run mysql command line client, log in as root and paste in scripts contents.

You can change used database in src/main/resources/META-INF/persistence.xml file.

To fill database with weather data run with starting class hvac.weather.parsing.WeatherSaver
This will also delete previous weather data.

### Google Calendar
To use this project you need to register it with google calendar.
To do it place provided StoredCredential file in the same location as StoredCredential.hehe file
or generate credentials for your google account [here](https://developers.google.com/calendar/quickstart/java)
(click Enable the google calendar API button) and place generated file in the same location as
credentials.json.here file. As StoredCredentials and credentials.json files contain sensitive data
 they are excluded from the repository.
### Weather data
Data provided by [meteostat](https://www.meteostat.net).
Meteorological data: Copyright &copy; National Oceanic and Atmospheric Administration (NOAA),
 Deutscher Wetterdienst (DWD). Learn more about the
 [meteostat Sources](https://www.meteostat.net/sources).

