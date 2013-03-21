# MaidSyncher

This tool is used to bidirectionally sync tasks between an ActiveCollab instance, and the GitHub issues of an organization.

 
## Conversions:

#### Note: WORK IN PROGRESS, some of the conversions stated below might not be accurate ATM

Seen that the ActiveCollab (AC) universe, is richer in terms of representation of issues (e.g. AC has SubTasks, LoggedTime, etc) some 'conversions' have to occur to represent/manipulate those artefacts on the GitHub side. 
The conversions are as follows:


GitHub        | <-->         | ActiveCollab  |  |
| ------------- |------------| -----		    |----  |
| Issue      	| <--> 			       | Task 			 |  | 
| Issue      	| <-- 			|  SubTask		       |  |
| Comment 		| -->			| LogedTime		| @loggedTime[nmbr] |
| Comment		| <--			| LoggedTime	| 
| <strike>Issue	 | <strike>	-/-></strike> |	<strike>SubTask</strike> | Not yet supported |
| <strike> Assignee </strike>|<strike><-/-> </strike>	|<strike> Assignee</strike> | Not yet supported |
| <strike>Comment</strike>| <strike><-/-></strike> |<strike>	Other Assignees </strike>| Not yet supported


##License
Refer to the LICENSE and COPYRIGHT files

##Structure
 - ACGHSync - the project used as basis for the ACollab REST API
 - egit-github - the [egit](https://github.com/eclipse/egit-github) project - used for communication with GH
  - maid-syncher - the core logic of the maidSyncher
  - syncher-main - project with the runnable Main, other ways of calling the MaidSyncher might be used in the future e.g. webapp (thus levaraging the GH web hooks functionality)

##Installation and configuration
(why would you want this? this is work in progress, but at your own risk:)
###Setup egit-github:
Install the SNAPSHOT of the egit-github library (seen that we are using a SNAPSHOT version of it [probably we could do with a release as well..]) 

``` 
cd egit-github/org.eclipse.egit.github.core
mvn -f pom-jar.xml clean install
```
### Setup and run MaidSyncher:

Use the `configuration.properties.sample` as basis, edit it and copy it under the same folder as `configuration.properties`

``` 
cd maid-syncher
mvn clean install
```

and then run the main (at your own risk:)

``` 
cd syncher-main
mvn clean package exec:java -Dexec.mainClass=pt.ist.Main -DskipTests 
```

