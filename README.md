## Messages

### Register
Not used
---
### RegisterAccept
Not used
---
### RegisterReject
Not used
---
### Login
Not used
---
### LoginAccept
Not used
---
### LoginReject
Not used
---
### QuickLogin
The primary method of logging into the server.

##### handle (String)
The user’s display name

##### UUID (String)
A UUID obtained from the user’s mobile device.

##### phoneNumber (long)
The user’s phone number. This is used so contacts may invite each other to chatrooms. The field is optional, but preferred.
---
### Connect
The first message a client sends to the server.

##### APIVersion (int)
The message version that the client is using.

##### UUID (String)
A UUID obtained from the user’s mobile device.
---
### ConnectAccept 
Sent from the server on a successful client connection.

##### APIVersion (int)
The version of messages that the server is using.

##### globalChatId (long)
A user’s global ID assigned by the server.

##### imageUploadUrl (String)
Not used

##### imageDownloadUrl (String)
Not used
---
### ConnectReject
Sent from the server if a connection request from the user failed.

##### reason (String)
The error message related to the connection request failure.
---
### Heartbeat
Not used
---
### SubmitMessage 
Submit a message to a chatroom.

##### userId (long)
The sender’s userId

##### chatroomId (long)
The destination chatroom id

##### message (String)
The message to send
---
### Message (String)
A chat message received from the server

##### messageId (long)
The message’s id

##### messageTimestamp (long)
Timestamp when the message was created on the server (not when it originated from the sender).

##### senderId (long)
The id of the message sender

##### chatroomId (long)
The id of the destination chatroom

##### senderHandle (String)
The displayed name of the message sender

##### message (String)
The received chat message
---
### SubmitMessageReject
Sent from the server if a submitted message is rejected

##### userId (long)
The user id of the user who tried to send the message

##### chatroomId (long)
The chatroom id of the chatroom the user tried to join

##### reason (String)
An explanation of the message rejection
---
### SearchChatrooms
Search over chatrooms on server

##### latitude (long)
Latitude of the same style as WOTW— (value + 400) * 1000000

##### longitude (long)
Longitude of the same style as WOTW— (value + 400) * 1000000

##### onlyJoinable (byte)
If 1, return only chatrooms joinable from the supplied lat-long. If 0, return all chatrooms.

##### metersFromCoords (long)
If onlyJoinable is 1, the search will return chatrooms that are joinable from any coordinates within a radius from the supplied lat-long.
---
### Chatroom
All information for one chatroom, sent from the server in response to a user’s chatroom query.

##### chatroomId (long)
The chatroom’s id

##### chatroomOwnerId (long)
The id of the user who created the chatroom

##### chatroomName (String)
The name of the chatroom

##### chatroomOwnerHandle (String)
The display name for the chatroom owner

##### latitude (long)
Latitude for chatroom origin

##### longitude (long)
Longitude for chatroom origin

##### radius (long)
Chatroom radius size in meters

##### userCount (int)
The number of users in the chatroom

##### chatActivity (short)
Not used
---
### JoinChatroom
A request from a user to join a chatroom. A user cannot join a chatroom of which he is already a member.

##### userId (long)
The sender’s id

##### chatroomId (long)
The id of the chatroom to join

##### latitude (long)
The user’s lat coord ( (value + 400) * 1000000 )

##### longitude (long)
The user’s long coord ( (value + 400) * 1000000 )
---
### LeaveChatroom
Sent from the user to actively leave a chatroom

##### userId (long)
The user’s id who wants to leave the chatroom

##### chatroomId (long)
The id of the chatroom to leave
---
### CreateChatroom 
Sent by a user to create a chatroom 

##### ownerId (long)
The sender’s user id

##### chatroomName (String)
The proposed name of the chatroom

##### latitude (long)
The user’s lat coord ( (value + 400) * 1000000 )

##### longitude (long)
The user’s long coord ( (value + 400) * 1000000 )

##### radius (long)
Proposed chatroom radius size in meters
---
### JoinChatroomReject
A message sent to a user who failed to join a chatroom

##### chatroomId (long)
The id of chatroom that the user failed to join

##### reason (String)
An explanation of the message rejection
---
### JoinedChatroom
The message sent to all users in a chatroom when a new user joins, including the user who just joined.

##### userId (long)
The id of the user who joined the chatroom

##### userHandle (String)
The handle of the user who joined the chatroom

##### chatroomId (long)
The id of the chatroom that the user joined

##### chatroomOwnerId (long)
The handle of the user who created the chatroom

##### chatroomName (String)
The name of the chatroom joined

##### chatroomOwnerHandle (String)
The hand of the user who created the chatroom

##### latitude (long)
The chatroom origin’s lat coord ( (value + 400) * 1000000 )

##### longitude (long)
The chatroom origin’s long coord ( (value + 400) * 1000000 )

##### radius (long)
Chatroom radius size in meters

##### userCount (int)
The number of users in the chatroom

##### chatActivity (short)
Not used
---
### LeftChatroom
The message sent to all users in a chatroom when a user leaves the chatroom, including the user who left.

##### chatroomId (long)
The id of chatroom left

##### userId (long)
The id of the user who left the chatroom
---
### CreateChatroomReject
A message sent to a user when the server rejects an attempt to create a chatroom

##### chatroomName (String)
The name of the failed chatroom

##### reason (String)
An explanation of the message rejection
---
### Vote
A message used to vote on a chat message in a chatroom

##### voterId (long)
The sender’s id

##### votedId (long)
The id of the user who created the chat message

##### msgId (long)
The id of the chat message being voted on

##### chatroomId (long)
The id of the chatroom where the message was sent

##### upvote (byte)
If 1, the it’s an up vote. If 0, it’s a downvote.
---
### InviteUser
This message travels client-to-server and server-to-client. A user sends this message to invite another user to a chatroom.

##### senderId (longer)
The inviter’s user id

##### senderHandle (String)
The inviter’s displayed name

##### recipientId (long)
The invitee’s user id

##### chatroomId (long)
The id of chatroom to which the invitee is being invited

##### chatroomName (String)
The name of the chatroom to which the invitee is being invited

##### chatroomLat (long)
The latitude of the chatroom to which the invitee is being invited

##### chatroomLong (long)
The longitude of the chatroom to which the invitee is being invited

##### chatroomRadius (long)
The radius of the chatroom to which the user is being invited, in meters

##### recipientPhoneNumber (long)
** This will change soon
The phone number (country code + area code + number) of the invitee
---
### InviteUserReject
A message sent to a user who invited a user who could not be invited. This does not mean that the user rejected the invitation.

##### reason (String)
An explanation for why the user could not be invited to the chatroom
---
### InviteUserSuccess
A message sent to a user who successfully invited another user. This does not mean that the user accepted the invitation.

##### inviteeUserId (long)
The user id of the invitee

##### inviteeUserHandle (String)
The displayed name of the invitee

##### chatroomName (String)
The name of the chatroom to which the user was invited
---
