# Event-Tracker
An event tracking application that incorporated Android native mobile front end and Google Cloud Platform REST API backend.

# Entities and Properties
# Event
An event entity records the information, such as name, date, time and description of an event. Methods allowed with this API include:
-	Create a new event
-	Delete an event 
-	Delete all events
-	Update an event
-	Retrieve an event
-	Retrieve all event

# User
A user entity records information such as first name, last name, email, username, password and a list of events this user has. Methods allowed with this API include:
-	Create a new user account
-	Delete a user account
-	Delete all users
-	Update a user account
-	Retrieve a user
-	Retrieve all users

# EventHistory
An event history entity stores information of an event deleted by a user. Methods allowed with this API include:
-	Retrieve all event history entities
-	Retrieve event history entities of a user
-	Delete all event history entities
-	Delete event history entities of a user

# Relationships Between Entities
Some relationships between all the entities listed above:
-	New event created is added to user account if the user has previously created an account. Users without an account in data store are those users who logged in using their Facebook account. 
-	Events associated to a specific user can be retrieved
-	Deleting events will simultaneously remove references to that event in a user account and create an event history entity for the event to be deleted
-	Deleting an account will simultaneously delete all events associated with the account and create event history entities for events deleted

# API Documentation
Refer to API pdf
