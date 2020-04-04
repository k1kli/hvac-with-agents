# hvac-with-agents
To run this project in InteliJ IDEA:
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

