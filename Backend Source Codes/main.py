import webapp2
from google.appengine.ext import ndb
import json
import datetime
from types import *
import sys

methods = set(webapp2.WSGIApplication.allowed_methods)
methods.add('PATCH')
webapp2.WSGIApplication.allowed_methods = frozenset(methods)

#event model
class Event (ndb.Model):
	name = ndb.StringProperty(required=True)
	date = ndb.StringProperty(required=True)
	time = ndb.StringProperty()
	description = ndb.StringProperty()
	all_day = ndb.BooleanProperty()
	account_id = ndb.StringProperty(required=True)
	id = ndb.StringProperty()

#user account model
class User (ndb.Model):
	first_name = ndb.StringProperty(required=True)
	last_name = ndb.StringProperty(required=True)
	username = ndb.StringProperty(required=True)
	email = ndb.StringProperty(required=True)
	id = ndb.StringProperty()
	events = ndb.JsonProperty()
	password = ndb.StringProperty(required=True)

#event history model
class EventHistory (ndb.Model):
	name = ndb.StringProperty()
	date = ndb.StringProperty()
	time = ndb.StringProperty()
	description = ndb.StringProperty()
	all_day = ndb.BooleanProperty()
	account_id = ndb.StringProperty()
	event_id = ndb.StringProperty()
	id = ndb.StringProperty()

#report error in request
def badRequest(self, message):
	error = {"error": message}
	self.response.status = "400 Bad Request"
	self.response.write(json.dumps(error))
	return

#check if argument is string type
def stringCheck(string):
	if (type(string) is not UnicodeType):
		return False
	else: 
		return True

#check if argument is boolean type
def boolCheck(val):
	if (type(val) is not BooleanType):
		return False
	else:
		return True

#check if all variables in request body is valid
def checkRequestBody(self, data):
	if ('name' in data):
		if (not stringCheck(data['name'])):
			badRequest(self, "Name should be a string")
			return False

	if ('date' in data):
		if (not stringCheck(data['date'])):
			badRequest(self, "Date should be a string")
			return False

	if ('time' in data):
		if (not stringCheck(data['time'])):
			badRequest(self, "Time should be a string")
			return False

	if ('description' in data):
		if (not stringCheck(data['description'])):
			badRequest(self, "Description should be a string")
			return False

	if ('all_day' in data):
		if (not boolCheck(data['all_day'])):
			badRequest(self, "All_day should be a string")
			return False

	if ('account_id' in data):
		if (not stringCheck(data['account_id'])):
			badRequest(self, "Account_id should be a string")
			return False

	if ('username' in data):
		if (not stringCheck(data['username'])):
			badRequest(self, "Username should be a string")
			return False

	if ('first_name' in data):
		if (not stringCheck(data['first_name'])):
			badRequest(self, "First_name should be a string")
			return False

	if ('last_name' in data):
		if (not stringCheck(data['last_name'])):
			badRequest(self, "Last_name should be a string")
			return False

	if ('email' in data):
		if (not stringCheck(data['email'])):
			badRequest(self, "Email should be a string")
			return False

	if ('password' in data):
		if (not stringCheck(data['password'])):
			badRequest(self, "Password should be a string")
			return False

	return True

#Event handler: 
# POST - create a new event
# GET - get all events in datastore
# DELETE - delete all events in datastore
class EventHandler(webapp2.RequestHandler):
	
	#### POST - create a new event ###
	def post(self):
		#get event details from request body
		event_data = json.loads(self.request.body)
		print (event_data)

		#check request body
		if (not checkRequestBody(self, event_data)):
			return

		#make sure all required properties are present in request body
		if ('name' not in event_data) or ('date' not in event_data) or ('account_id' not in event_data):
			badRequest(self, "Name, date and account_id are required")
			return

		#if all_day is true, ignore time value in request body
		if ('all_day' in event_data):
			if (event_data['all_day']):
				event_data['time'] = None
		
		# check if description is in request body. 
		# set to null if description is not in request body
		if ('description' not in event_data):
			event_data['description'] = None

		# check if all_day is in request body. 
		# Set to false if not in request body
		if ('all_day' not in event_data):
			event_data['all_day'] = False

		# check if time is in request body
		# set to null if not in request body
		if ('time' not in event_data):
			event_data['time'] = None

		#create new event 
		new_event = Event(
			name = event_data['name'], 
			date = str(event_data['date']), 
			time = str(event_data['time']), 
			description = event_data['description'], 
			all_day = event_data['all_day'], 
			account_id = str(event_data['account_id']))
		new_event.put()

		#get event id
		new_event.id = str(new_event.key.urlsafe())
		new_event.put()

		#check if account_id is a valid user account in datastore
		user_query_object = ndb.gql("SELECT * FROM User WHERE id = '" + str(event_data['account_id']) + "'")
		if (user_query_object.count() > 0):
			#update user's account events field
			user_key = ndb.Key(urlsafe = str(new_event.account_id))
			user = user_key.get()
			event_json = {"id": new_event.id, "self": "/events/" + new_event.id}
			user.events.append(event_json)
			user.put()

		#set a self link to event and user account
		new_event_dict = new_event.to_dict()
		new_event_dict['self'] = "/events/" + new_event.id
		new_event_dict['user_self'] = "/users" + new_event.account_id

		#return event created
		self.response.write(json.dumps(new_event_dict))


	### GET - get all events in datastore ###
	def get(self):
		#query for events from datastore
		event_query_objects = ndb.gql("SELECT * FROM Event ORDER BY date, time, name ASC")
		events_list = {}
		events = []
		
		if (event_query_objects.count() > 0):
			# get each event from query and append to events list
			for event_query in event_query_objects:
				event = event_query.to_dict()
				event['self'] = "/events/" + event['id']
				events.append(event)
		
		events_list['events'] = events
		print(events_list)
		# return list of events
		self.response.write(json.dumps(events_list))

	
	### DELETE - delete all events in datastore ###
	def delete(self):
		# query for events from datastore
		event_query_objects = ndb.gql("SELECT * FROM Event")

		# 1 - get each event from query
		# 2 - create a history event and add to history datastore
		# 3 - remove event from user's account
		if (event_query_objects.count() > 0):
			for event_query in event_query_objects:
				event_key = ndb.Key(urlsafe = event_query.key.urlsafe())
				event = event_key.get()

				# create a history event entity
				history = EventHistory(name = event.name, date = event.date, time = event.time, description = event.description, all_day = event.all_day, account_id = event.account_id, event_id = event.id)
				history.put()

				#get history id
				history.id = str(history.key.urlsafe())
				history.put()

				# remove event from user's account
				user_query_object = ndb.gql("SELECT * FROM User WHERE id = '" + event.account_id + "'")
				if (user_query_object.count() > 0):
					for user_query in user_query_object:
						# get user
						user_key = ndb.Key(urlsafe = str(event.account_id))
						user = user_key.get()

						# loop through user's events list
						for e in xrange(len(user.events)):
							# look for event to be deleted in user's events list
							# when found, remove event from user's events list
							if (user.events[e]['id'] == event.id):
								user.events.pop(e)
								break

					# update changes to user
					user.put()

				#delete event from datastore
				event.key.delete()


# Single event handler
# GET - get a single event 
# DELETE - delete a single event
# PUT - updates event details
class SingleEventHandler(webapp2.RequestHandler):
	### GET - get a single event using event id ###
	def get(self, *args, **kwargs):
		try:
			# get event 
			event_key = ndb.Key(urlsafe=args[0])
			event = event_key.get()

			# create a json object for event
			event_dict = event.to_dict()

			# add a self link to event
			event_dict['self'] = "/events/" + event.id

			# return event
			self.response.write(json.dumps(event_dict))

		except (Exception):
			badRequest(self, "Invalid eventID")
			return


	### DELETE - delete a single event using event id ###
	def delete(self, *args, **kwargs):
		try:
			#get event to delete
			event_key = ndb.Key(urlsafe = args[0])
			event = event_key.get()

			#create an history entity
			history = EventHistory(name = event.name, date = event.date, time = event.time, description = event.description, all_day = event.all_day, account_id = event.account_id, event_id = event.id)
			#add event to history 
			history.put()

			#get history id
			history.id = str(history.key.urlsafe())
			history.put()
			
			#get associated user account
			user_query_object = ndb.gql("SELECT * FROM User WHERE id = '" + str(event.account_id) + "'")

			if (user_query_object.count() > 0):
				for user_query in user_query_object:
					user_key = ndb.Key(urlsafe = event.account_id)
					user = user_key.get()

					#look for event to delete from user's events list
					for e in xrange(len(user.events)):
						# found 
						if (user.events[e]['id'] == event.id):
							#remove event from user's list
							user.events.pop(e)
							break

				# update user account
				user.put()

			# delete event
			event.key.delete()

		except (Exception):
			badRequest(self, "Invalid eventID")
			return

	
	### PUT - update a single event using event id ###
	def put(self, *args, **kwargs):
		try:
			#get event
			event_key = ndb.Key(urlsafe = args[0])
			event = event_key.get()

			#get new event information from request body
			new_data = json.loads(self.request.body)

			#check for valid request body
			if (not checkRequestBody(self, new_data)):
				return

			#update information
			if ('name' in new_data):
				event.name = new_data['name']

			if ('date' in new_data):
				event.date = new_data['date']

			if ('time' in new_data):
				event.time = new_data['time']

			if ('description' in new_data):
				event.description = new_data['description']

			if ('all_day' in new_data):
				event.all_day = new_data['all_day']

			#check if all_day is true. If all_day is true, set time to null
			if (event.all_day):
				event.time = None

			# update event
			event.put()

			# create a json object for event
			event_dict = event.to_dict()

			# add self link to event
			event_dict['self'] = "/events/" + event.id

			# return event
			self.response.write(json.dumps(event_dict))

		except (Exception):
			badRequest(self, "Invalid eventID")
			return


# User handler
# POST - create new user account
# GET - get all users
# DELETE - delete all users
class UserHandler(webapp2.RequestHandler):
	### POST - create new user account ###
	def post(self):
		# get information from request body
		user_data = json.loads(self.request.body)

		#check if request body is valid
		if (not checkRequestBody(self, user_data)):
			return

		#check if all required properties are provided
		if ('first_name' not in user_data) or ('last_name' not in user_data) or ('username' not in user_data) or ('email' not in user_data) or ('password' not in user_data):
			badRequest(self, "First_name, last_name, username, email, and password are required");
			return

		#check if events is provided in user_data
		if ('events' in user_data):
			badRequest(self, "New user should start with empty events")
			return
			
		#check if username has been used
		new_username = str(user_data['username'])
		username_exist = ndb.gql("SELECT * FROM User WHERE username = '" + new_username + "'")
		if (username_exist.count() > 0):
			badRequest(self, "Username is in used.")
			return

		#check if email has been used
		new_email = str(user_data['email'])
		email_exist = ndb.gql("SELECT * FROM User WHERE email = '" + new_email + "'")
		if (email_exist.count() > 0):
			badRequest(self, "Email is associated with another account")
			return

		#create new account
		new_user = User(first_name = user_data['first_name'], last_name = user_data['last_name'], username = user_data['username'], email = user_data['email'], password = user_data['password'], events = [])
		new_user.put()
		new_user.id = str(new_user.key.urlsafe())
		new_user.put()
		
		#create a self link and return 
		user_dict = new_user.to_dict()
		user_dict['self'] = "/users/" + new_user.id
		self.response.write(json.dumps(user_dict))

	
	### DELETE - delete all users ###
	def delete(self):
		# get all user accounts
		user_query_object = ndb.gql("SELECT * FROM User")

		# for each user account
		# - delete all events associated with the account
		# - add all events to history list
		# - delete account
		if (user_query_object.count() > 0):
			for user_query in user_query_object:
				#get user account
				user_key = ndb.Key(urlsafe = user_query.key.urlsafe())
				user = user_key.get()

				#delete all events associated with this user account
				for e in xrange(len(user.events)):
					#get event
					event_key = ndb.Key(urlsafe = str(user.events[e]['id']))
					event = event_key.get()
					
					#delete event
					event.key.delete()
				
				#delete user
				user.key.delete()

	
	### GET - get all users ###
	def get(self):
		# get all users
		user_query_object = ndb.gql("SELECT * FROM User")
		user_list = {}
		users = []

		# for each user create a self link and add to user list
		if (user_query_object.count() > 0):
			for user_query in user_query_object:
				user = user_query.to_dict()
				user['self'] = "/users/" + user['id']
				users.append(user)

		user_list['users'] = users
		print (user_list)
		# return user list
		self.response.write(json.dumps(user_list))


# Single user handler
# GET - get a user
# DELETE - delete a user
# PUT - update user details
# PATCH - change password
class SingleUserHandler(webapp2.RequestHandler):
	### GET - get a user account ###
	def get(self, *args, **kwargs):
		try:
			# get user and return
			user_key = ndb.Key(urlsafe = args[0])
			user = user_key.get()
			user_dict = user.to_dict()
			user_dict['self'] = "/users/" + user.id
			self.response.write(json.dumps(user_dict))

		except (Exception):
			badRequest(self, "Invalid userID")
			return


	### DELETE - delete a user account ###
	def delete(self, *args, **kwargs):
		try:
			# get user
			user_key = ndb.Key(urlsafe = args[0])
			user = user_key.get()

			#delete all events associated with this user account
			for e in xrange(len(user.events)):
				#get event
				event_key = ndb.Key(urlsafe = str(user.events[e]['id']))
				event = event_key.get()

				#add event to history
				history = EventHistory(name = event.name, date = event.date, time = event.time, description = event.description, all_day = event.all_day, account_id = event.account_id, event_id = event.id)
				history.put()

				#get history id
				history.id = str(history.key.urlsafe())
				history.put()

				#delete event
				event.key.delete()

			#delete user account
			user.key.delete()

		except (Exception):
			badRequest(self, "Invalid userID")
			return

	
	### PUT - update user account (names, email) ###
	def put(self, *args, **kwargs):
		try:
			# get user
			user_key = ndb.Key(urlsafe = args[0])
			user = user_key.get()

			#get new information from request body
			new_data = json.loads(self.request.body)

			#check if new_data is valid
			if (not checkRequestBody(self, new_data)):
				return

			#not allow to update password or events using this method
			if ('password' in new_data) or ('events' in new_data):
				badRequest(self, "Not allowed to update password or events using this method")
				return

			#if updating username check if username is unique
			if ('username' in new_data):
				#query for account with username specified
				user_query_object = ndb.gql("SELECT * FROM User WHERE username = '" + str(new_data['username']) + "'")

				#if query returned with 1 or more objects, username is in used
				#report error and return
				if (user_query_object.count() > 0):
					badRequest(self, "Username is associated with another account")
					return

				#if query returned no result, update username
				user.username = new_data['username']

			#if updating email check if email is associated with other account
			if ('email' in new_data):
				#query for account with email specified
				user_query_object = ndb.gql("SELECT * FROM User WHERE email = '" + str(new_data['email']) + "'")

				#if query return valid result, report error and return
				if (user_query_object.count() > 0):
					badRequest(self, "Email is associated with another account")
					return

				#if query returned no result, update email
				user.email = new_data['email']

			#update all other information
			if ('first_name' in new_data):
				user.first_name = new_data['first_name']

			if ('last_name' in new_data):
				user.last_name = new_data['last_name']

			#save to datastore
			user.put()

			#create self link and return
			user_dict = user.to_dict()
			user_dict['self'] = "/users/" + user.id
			self.response.write(json.dumps(user_dict))

		except (Exception):
			badRequest(self, "Invalid userID")
			return

	
	### PATCH - change password ###
	def patch(self, *args, **kwargs):
		try:
			# get user
			user_key = ndb.Key(urlsafe = args[0])
			user = user_key.get()

			#get new information from request body
			new_data = json.loads(self.request.body)

			#check for valid data
			if (not checkRequestBody(self, new_data)):
				return

			#if password is not in request body, return with error
			if ('password' not in new_data):
				badRequest(self, "Password is required")
				return

			#if other information is provided in request body, return with error
			if ('first_name' in new_data) or ('last_name' in new_data) or ('username' in new_data) or ('email' in new_data) or ('events' in new_data):
				badRequest(self, "Only password changed is allowed using this method")
				return

			#check if new password is the same as old password
			if (user.password == new_data['password']):
				badRequest(self, "New password should be different from old password")
				return

			#no error found, update password
			user.password = new_data['password']
			user.put()

			#create a self link and return
			user_dict = user.to_dict()
			user_dict['self'] = "/users/" + user.id
			self.response.write(json.dumps(user_dict))

		except (Exception):
			badRequest(self, "Invalid userID")
			return


# User and Event Handler
# GET - get all events associated with a user
# DELETE - delete all events associated with a user
class UserEventHandler(webapp2.RequestHandler):
	### GET - get all events associated with this user id ###
	def get(self, *args, **kwargs):
		try:
			# get user
			user_key = ndb.Key(urlsafe = args[0])
			user = user_key.get()

			# create an event list
			event_list = {}
			events = []

			# get events associated with this user
			for e in xrange(len(user.events)):
				#get event and add to event_list
				event_key = ndb.Key(urlsafe = str(user.events[e]['id']))
				event = event_key.get()
				event_dict = event.to_dict()
				event_dict['self'] = "/events/" + event.id
				events.append(event_dict)

			event_list['events'] = events
			print (event_list)

			# return list of events
			self.response.write(json.dumps(event_list))

		except (Exception):
			badRequest(self, "Invalid userID")
			return

	
	### DELETE - delete all events associated with this user id ###
	def delete(self, *args, **kwargs):
		try:
			# get user
			user_key = ndb.Key(urlsafe = args[0])
			user = user_key.get()

			# get events associated with this user
			# add all events to history list
			# remove events from user account
			# delete events
			for e in xrange(len(user.events)):
				#get event
				event_key = ndb.Key(urlsafe = str(user.events[e]['id']))
				event = event_key.get()

				#add event to history 
				history = EventHistory(name = event.name, date = event.date, time = event.time, description = event.description, all_day = event.all_day, account_id = event.account_id, event_id = event.id)
				history.put()

				#get history id
				history.id = str(history.key.urlsafe())
				history.put()

				#delete event
				event.key.delete()

			#set events list to empty
			user.events = []

			#update user
			user.put()

			#create self link
			user_dict = user.to_dict()
			user_dict['self'] = "/users/" + user.id

			#return
			self.response.write(json.dumps(user_dict))

		except (Exception):
			badRequest(self, "Invalid userID")
			return


# User and Event History Handler
# GET - get event history of a user
# DELETE - clear all history of a user
class UserEventHistoryHandler(webapp2.RequestHandler):
	###GET - get a list of previous events from this user account ###
	def get(self, *args, **kwargs):
		try:
			#get user 
			user_key = ndb.Key(urlsafe = args[0])
			user = user_key.get()

			#a list to hold all events
			history_list = {}
			history = []

			#query for list of history for this user
			event_query_objects = ndb.gql("SELECT * FROM EventHistory WHERE account_id = '" + str(user.id) + "'")

			#append each event to history list
			if (event_query_objects.count() > 0):
				for event_query in event_query_objects:
					event_key = ndb.Key(urlsafe = event_query.key.urlsafe())
					event = event_key.get()
					history.append(event.to_dict())

			history_list['history'] = history

			#return history list
			self.response.write(json.dumps(history_list))

		except (Exception):
			badRequest(self, "Invalid userID")
			return


	###DELETE - delete user's event history ###
	def delete(self, *args, **kwargs):
		try:
			#get user
			user_key = ndb.Key(urlsafe = args[0])
			user = user_key.get()

			#query for list of history for this user
			history_query_objects = ndb.gql("SELECT * FROM EventHistory WHERE account_id = '" + str(user.id) + "'")

			#delete each history event
			print (history_query_objects.count())
			if (history_query_objects.count() > 0):
				for history_query in history_query_objects:
					history_key = ndb.Key(urlsafe = history_query.key.urlsafe())
					history = history_key.get()
					history.key.delete()

			return

		except (Exception):
			badRequest(self, "Invalid userID")
			return


# Event History Handler
# DELETE - delete all events in history
# GET - get all events in history
class EventHistoryHandler(webapp2.RequestHandler):
	### DELETE - delete all event's in history list ###
	def delete(self):
		# query for all events
		history_query_objects = ndb.gql("SELECT * FROM EventHistory")

		# delete each events from datastore
		if (history_query_objects.count() > 0):
			for history_query in history_query_objects:
				history_key = ndb.Key(urlsafe = history_query.key.urlsafe())
				history = history_key.get()
				history.key.delete()

		return

	### GET - get all events in history list ###
	def get(self):
		history_list = {}
		histories = []

		history_query_objects = ndb.gql("SELECT * FROM EventHistory")

		if (history_query_objects.count() > 0):
			for history_query in history_query_objects:
				history_key = ndb.Key(urlsafe = history_query.key.urlsafe())
				history = history_key.get()
				histories.append(history.to_dict())
		
		history_list['history'] = histories
		self.response.write(json.dumps(history_list))


# Login Handler
# POST - check if user entered credentials are the same as those in datastore
#	   - post request to prevent credentials shown in URL
class UserLoginHandler(webapp2.RequestHandler):
	### POST - check user entered credentials ###
	def post(self):
		# get credentials from request body
		requestBody = json.loads(self.request.body)

		#check if username and password are in request body
		if ('username' not in requestBody) or ('password' not in requestBody):
			badRequest(self, "Username and password are required")
			return

		#check if request body has valid data
		if (not checkRequestBody(self, requestBody)):
			return

		#no error, query for user account based on username
		user_query_object = ndb.gql("SELECT * FROM User WHERE username = '" + str(requestBody['username']) + "'")

		#if no account found, return with error
		if (user_query_object.count() <= 0):
			badRequest(self, "Invalid username or password")
			return

		#if only one account found, check if password match
		for user_query in user_query_object:
			# get user 
			user_key = ndb.Key(urlsafe = user_query.key.urlsafe())
			user = user_key.get()

			# check password
			if (user.password == str(requestBody['password'])):
				self.response.write(json.dumps(user.to_dict()))

			else:
				badRequest(self, "Invalid username or password")
				return
			

# Handle events for user without an account in datastore (i.e. user logged in using their facebook account)
# GET - get events associated for this user
# DELETE - delete events associated with this user
class EventUserHandler(webapp2.RequestHandler):
	### GET - get events assocaited with this user ###
	def get (self, *args, **kwargs):
		#get account_id
		account_id = str(args[0])

		#get events associated with this account_id
		event_query_objects = ndb.gql("SELECT * FROM Event WHERE account_id = '" + account_id + "'")

		event_list = {}
		events = []

		if (event_query_objects.count() > 0):
			for event_query in event_query_objects:
				event_key = ndb.Key(urlsafe = event_query.key.urlsafe())
				event = event_key.get()
				events.append(event.to_dict())

		event_list['events'] = events
		self.response.write(json.dumps(event_list))

	### DELETE - delete events associated with this user ###
	def delete(self, *args, **kwargs):
		#get account_id
		account_id = str(args[0])

		#get events associated with this account_id
		event_query_objects = ndb.gql("SELECT * FROM Event WHERE account_id = '" + account_id + "'")

		if (event_query_objects.count() > 0):
			for event_query in event_query_objects:
				event_key = ndb.Key(urlsafe = event_query.key.urlsafe())
				event = event_key.get()

				#add event to history 
				history = EventHistory(name = event.name, date = event.date, time = event.time, description = event.description, all_day = event.all_day, account_id = event.account_id, event_id = event.id)
				history.put()

				#get history id
				history.id = str(history.key.urlsafe())
				history.put()

				#delete event
				event.key.delete()


# Handle event history for user without an account in datastore (i.e. user logged in using their facebook account)
# GET - get all past events for this user
# DELETE - delete all past events for this user
class EventHistoryUserHandler(webapp2.RequestHandler):
	### GET - get all past event associated with this account_id
	def get(self, *args, **kwargs):
		#get account_id
		account_id = str(args[0])

		history_list = {}
		histories = []

		#get past events associated with this account_id
		history_query_objects = ndb.gql("SELECT * FROM EventHistory WHERE account_id = '" + account_id + "'")

		if (history_query_objects.count() > 0):
			for history_query in history_query_objects:
				history_key = ndb.Key(urlsafe = history_query.key.urlsafe())
				history = history_key.get()
				histories.append(history.to_dict())

		history_list['history'] = histories
		print (history_list)

		self.response.write(json.dumps(history_list))


	### DELETE - delete all past events associated with this account_id ###
	def delete(self, *args, **kwargs):
		#get account_id
		account_id = str(args[0])

		#get past events associated with this account_id
		history_query_objects = ndb.gql("SELECT * FROM EventHistory WHERE account_id = '" + account_id + "'")

		if (history_query_objects.count() > 0):
			for history_query in history_query_objects:
				history_key = ndb.Key(urlsafe = history_query.key.urlsafe())
				history = history_key.get()
				history.key.delete()

class MainPage(webapp2.RequestHandler):
    def get(self):
        self.response.write("Hello")

app = webapp2.WSGIApplication([
    ('/', MainPage),
    ('/history', EventHistoryHandler),
    ('/history/(.*)', EventHistoryUserHandler),
    ('/events', EventHandler),
    ('/events/users/(.*)', EventUserHandler),
    ('/events/(.*)', SingleEventHandler),
    ('/user', UserLoginHandler),
    ('/users', UserHandler),
    ('/users/(.*)/events', UserEventHandler),
    ('/users/(.*)/events/history', UserEventHistoryHandler),
    ('/users/(.*)', SingleUserHandler),
], debug=True)