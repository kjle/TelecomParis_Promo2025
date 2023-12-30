## Q1: Once connected, can you find the 'Connection Options'?
That is, what are the configuration parameter values of your connection?
E.g. CleanSession - true or false? what does this mean? (Cf. lecture notes or online documentation);
> true. CleanSession: Delete the specified clientIds context. True for silence.

E.g. KeepAliveInterval - what is the value and its measuring unit? what does it mean?
> 60 second. This value, measured in seconds, defines the maximum time interval between messages sent or received. It enables the client to detect if the server is no longer available, without having to wait for the TCP/IP timeout. The client will ensure that at least one message travels across the network within each keep alive period. In the absence of a data-related message during the time period, the client sends a very small "ping" message, which the server will acknowledge. A value of 0 disables keepalive processing in the client.
The default value is 60 seconds

E.g. ConnectionTimeout - what is the value and its measuring unit? what does it mean?
> 30 second. This value, measured in seconds, defines the maximum time interval the client will wait for the network connection to the MQTT server to be established. The default timeout is 30 seconds.

E.g. Is your connection secure? if so, how?
> No by default. To ensure a secure connection, user is required the usr name and pw.

## Q2: Can you figure-out how to change your connection's configuration values?
E.g. change the KeepAliveInterval to 70s.
Hint: For your connection in the Manage Connections window, select a Detailed configuration perspective. Use 'Close and re-open existing connection' button to see the result as before.

## Q3: can you check that the message was sent correctly?
> no
what is the message's payload size? (i.e. number of data bytes)
> 15
how does this payload compare to the number of characters in the published Data field?
> payload = nb of ch in published data field

## Q4: how can you tell whether the subscription was established correctly?
Hint 1: can you see the subscription confirmation message in the window where you launched the Mqtt-Spy Client?
> No, but it'll open a new tag with color yellow.

it should include your topic name and confirm that the subscriber is CONNECTED. Hint 2: publish another message as before. Does your subscriber client receive the message?
> yes
Now publish several messages and note the message counter increment in the GUI.
Try to browse through each message received; and check its data content and reception date.

## Q5:
Connect two new MQTT-Spy Clients to the Broker (as before).
Have one Client publish on a new topic (e.g. labs/topic2) and all the others subscribe to that topic. Check that it works.
Pick another Client and have it publish to yet another topic (e.g. labs/topic3); and subscribe one other Client to this topic. Have the two publishers send several messages and check which subscribers receive them. Is this what you expected?
> yes

## Q6:
From the Publish options, pick 'save current message as script' to save your publishing command as a java script (e.g. name it 'my-script').
Can you see your script in the 'Scripted publications' pane of the GUI?
> yes
If so, then publish a message using the script. Then select the 'Repeat' option of your script to publish messages in a loop.
Uncheck the 'Repeat' option of your script to stop sending messages repeatedly. Check the message counter of all subscribers to the topic you've just published on. What does it indicate?
Have all subscribers received the same number of messages?