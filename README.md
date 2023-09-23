# GuessWhoIAm-Sever

This project is James Cook University CP3405 Group6 game project's Sever
The project is divided into two files
Sever - the server class file that provides the services used to run the game. Building an instance of Sever and calling
its public void GettingMessageSQS() method is the core functionality of the project.
StartRun - builds an instance of Sever and calls its public void GettingMessageSQS() method at regular intervals to keep
the game running.

public void GettingMessageSQS() method - the core functionality, each call will poll the specified SQS message for a
certain duration, process it and send the result message to the specified SQS.

State Transition Diagram:
![image](https://github.com/Meten1/GuessWhoIAm-Sever/assets/94505408/53b147e5-c83e-47da-80e6-c31a9e667818)

For more detailed information, please refer to the technical documentation provided with the project file.
