---
layout: page
root: "../../"
---

### Queues

Queues are also another important resource as they support work allocation among the users of the workflow system.

#### Create a new Queue

To create a new queue, you should invoke a ```POST``` to ```/api/workflow/queues``` and provide a JSON containing the name and members of the queue. An example of such JSON is provided below.

Request Data:   

	{ 
		"name": "Example Queue",
		"members": [ 1231234, 3423425, 234423 ]
	}

Response Data:   

	{
		"id": 234131234233,
		"title": "Example Queue",
		"members": [
			{ "id": 1231234, "name": "Obi Wan Kenobi" },
			{ "id": 3423425, "name": "Admiral Gial Ackbar" },
			{ "id": 2344231, "name": "Jabba the Hut" }
		]
	}

#### Update an existing Queue

To edit an existing queue, you should invoke a ```PUT``` to ```/api/workflow/queues/{queueId}``` and provide a JSON containing the value you wish to update. An example to change the name of the previous queue is provided below.


```PUT``` to ```/api/workflow/queues/234131234233```

Request Data:   

	{
		"name": "Star Wars Queue"
	}

Response Data:


	{
		"id": 234131234233,
		"title": "Star Wars Queue",
		"members": [
			{ "id": 1231234, "name": "Obi Wan Kenobi" },
			{ "id": 3423425, "name": "Admiral Gial Ackbar" },
			{ "id": 2344231, "name": "Jabba the Hut" }
		]
	}


#### Remove a user from a Queue

The API provides an endpoint to desassociate a user from an existing queue. A simple ```DELETE``` to ```/api/workflow/queues/{queueId}/members/{memberId}}```


For example, Admiral Gial Ackbar is suspecting that being in the Star Wars queue is a trap, and we wants to cancel its membership. To do so, we should invoke the following endpoint:

```DELETE``` to ```/api/workflow/queues/234131234233/members/3423425```

Response Data:   

	{
		"id": 234131234233,
		"title": "Example Queue",
		"members": [
			{ "id": 1231234, "name": "Obi Wan Kenobi" },
			{ "id": 3423425, "name": "Admiral Gial Ackbar" }
		]
	}