---
layout: page
---

## Overview

MaidSyncher is a  tool that is used to bidirectionally sync artifacts (tasks/subtasks/projects/etc) between an ActiveCollab instance, and the GitHub issues of an organization.

<a id="setup">  </a>
## Configuration and usage


### General instructions

Checkout the depended modules:

	git submodule update

Install egit:

	cd egit-github/org.eclipse.egit.github.core
	mvn -f pom-jar.xml install
	
Install the several modules easily with the aggregator pom:

	cd ../../
	mvn clean install

If all goes well, all the jars/war of different modules will be compilled and installed in the local maven repository.

### Setting up the GitHub credentials.

If one wants to get a list of the current authorizations, to choose a suitable one from the existing list, one can the list with:

	curl --user username:password https://api.github.com/authorizations

Where the username and password are from a user that has access to the repositories to be synched. 

A special user only setup for this purpose is recommended, in order to clearly distinguish between what was made automatically or manually by another user.

If no suitable (with the scope of `repo`) authorizations are found, one can create it with the following JSON that can be found in the `githubAuthorization.json`:

```
{
  "scopes": ["repo"],
  "note": "MaidSyncher access"
}
```	

and evoking the command:

	curl -X POST -d @gitHubAuthorization.json --user username:password https://api.github.com/authorizations

When successfull, an output JSON like the following:

```
{
  "id": 3152912,
  "url": "https://api.github.com/authorizations/3152912",
  "app": {
    "name": "MaidSyncher access (API)",
    "url": "http://developer.github.com/v3/oauth/#oauth-authorizations-api",
    "client_id": "997e0d7f84e77ee87b7c"
  },
  "token": "9dbs3b6490ca05000bb59c168e275ff9b0c86b50",
  "note": "MaidSyncher access",
  "note_url": null,
  "created_at": "2013-07-25T16:33:13Z",
  "updated_at": "2013-07-25T16:33:13Z",
  "scopes": [
    "repo"
  ]
}
```
will be returned, from which, the `token` item will be used to configure the `syncher-main` and `syncher-web` modules.

More details can be found in the [GitHub's developer page](http://developer.github.com/v3/oauth/).  

### Setting up the ActiveCollab credentials

As explained in more detail [here](https://www.activecollab.com/docs/manuals/developers-version-3/api/authentication). The credentials token can be attained on the ActiveCollab interface.

A special user only setup for this purpose is recommended, in order to clearly distinguish between what was made automatically or manually by another user.

### Configuration of the `syncher-main` & `syncher-web` modules:

To configure both modules, goto the `./src/main/resources` directory either under `syncher-main` or `syncher-web`, and setup the `configuration.properties` and `fenix-framework.properties` using their samples for instructions (that are available under the same directory with the `.sample` ending) and the GitHub and ActiveCollab tokens that should have been previously setup. 

Also, logger verbosity can be fine tuned using the [Logback's](http://logback.qos.ch/) `logback.xml` configuration file. More info can be found [here](http://logback.qos.ch/manual/configuration.html).
 
### Running the standalone `syncher-main` synch app

This module refers to the 'main' standalone executable app that will synch the ActiveCollab and GitHub instances.

After configuring the .properties files as already described, you can simply run:

	mvn exec:java -Dexec.mainClass="pt.ist.Main"

if you already have compiled the project, or 

	mvn clean package exec:java -Dexec.mainClass="pt.ist.Main"

to compile it and run the synching commands.

**NOTE:** running this app will change your ActiveCollab and GitHub instances. In a good way :) (at least hopefully! no, really, read the fine manual first on the expected behavior, plus, take into account that things might not be fully working as intended, if possible, run on test instances first).

### Running the `syncher-web` synch webapp

This module refers to the webapp that will both synch the ActiveCollab and GitHub instances, as well as display a web interface where more info about those synchronizations can be obtained.

After configuring the .properties files as already described, you can simply run:

	mvn jetty:start

if you already have compiled the project, or 

	mvn clean package jetty:start

to compile it and run the synching commands.

**NOTE:** running this app will change your ActiveCollab and GitHub instances. In a good way :) (at least hopefully! no, really, read the fine manual first on the expected behavior, plus, take into account that things might not be fully working as intended, if possible, run on test instances first).
 




