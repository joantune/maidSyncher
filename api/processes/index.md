---
layout: page
root: "../../"
---

### Processes

The most important resource of a workflow application is the process. This page identifies all the endpoints and methods that allow the manipulation of process resources.


```POST``` in ```/processes```

Data:   

	{ "processTypeId": 1231234 }

Response:   

	{
		"id": 2341312123,
		"title": "Processo de Compra",
		"author": { "id": 24234224, "name": "John Doe" }
	} 