team50
======
name: Shao Lan
Student Number: 2393767
Email Address: Sarah479960@gmail.com

Instruction:

/****************************build and usage instructions******************************/

My project work are mainly done according to the exercise introduction which is so useful to a new android developer. The functions of my project are as follows:

1. Download all the votings and voting result of one certain voting
2. Upload voting information to server.
3. Present voting detail on UI
  All the votings and their options can be operated by filing and orienting screen.
  3.1 The close voting
       The options of closed voting is gray that means any gesture to options are  disable, so user can't vote for a                closed voting. Meanwhile, the submit botton is also disable.
  3.2 The future voting
      The situation of future voting is similar to close voting below
  3.3 The open voting
      User are allowed to vote for open voting by fling screen left/right/up/down and orienting left/right/down/up.The            Accordingly, the application will quickly response. In addition, the submit botton is validate to use.
4. Notification
  4.1 nofify user "New voting arrived" when client first gets votings or  gets new voting from server
  4.2 notify user "voting is posted" when user submits a new vote successfully.
  4.3 notify user "No New Voting Available" when client doesn't get any voting.
  4.4 nofify user "It's already end!" when user see a close voting.
  4.5 notify user "It's not yet open!" when user see a upcoming voting.
  4.6 notify user <error messag> when error occurs.
5. Filter voting type
  user is enbale to select one type voting by using the dropdown list located in the head of screen. The choices are          "All votings","Closed votings","Open votings","Future votings", and the "All votings" are seleted as default.
6. Alter setting
   Change server address by using Setting option. When client check the updated address, it will download votings again.

/****************************should but didn't have functionality******************************/

The application is expected to retrieve result of closed voting and show them on view. My application can download and get the result of closed voting, but cann't present them correctly. I have spent on it for 6 hours, but it still doesn't work. 
/***************************information about  exercise work process**************************/

Because I am in one-person group, there is no information about work division. Truely, it is much more difficult to complete this work alone, but also learn more. The harvest in the whole process of exercise work is incremental, which push me deeper to Android development field.

/***************************summary of the results of testing**************************/

On the basis of this application's functionality, the testing work are conducted as follows:

Test 1. Action: Open and run application on virtual or real device
        result: 1.All the votings are retrieved and presented on the screen; 
                2.The message of “New voting arrived” is notified synchronously.
        
Test 2. Action: Fling or orienting(on for real device) device towad left, right, up and down.
        result: 1. The votings and options will be changed over at quick response;
                2. The application show differnt notification according to type of voting;
                3. The submit botton be setted enbale and disable according to type of voting.
                4. The startTime and/or endTime of each voting are showed according to the type of voting.
              
Test 3. Action:　Go to a open voting and select one option to submit.
        result:  1. It shows message of "Voting is posted". (I don't how to check whether it is really posted successfully                    on server)
                 2. User can vote for one voting more than once.
         
        
Test 4. Action: Click the dropdown list botton and select one option randomly. Then, go to home page and back. Then, exit                 and reopen it.
         result: 1. The option can be seleted and reletive type of votings be showed quickly.
                 2. The seleted option and voting are still there
                 3. Previous things are resetted.
                 
Test 5. Actios: Chang values of parameters in the internal code of application to test all notification.
        result: 1. Set the number of retrieve votings is equal to 0, the notification works
                2. Set the response code out of correct bound, the error notification works. 
                
Test 6. Action: Alter the content (server address) of Setting
        result: 1. The text area of Server address is writeble
                2. The button of Cancel and Confirm work, but there is reaction after changing the setting content.
