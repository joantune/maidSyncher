 - [X] - Reopen an existing **ACTask**
 - [X] - Assign an existing **ACTask** to a given repository;
 - [ ] - Closing an existing **GHIssue**;
  - [ ] - Closing an existing **GHIssue** - outside of the default Project - it removes the TaskCategory, but does not close the ACTask - it detects the state update though;
 - [ ] - After the creation of an ACTaskCategory, and a GHRepository, while opening tasks on that same repository (?), a 'conflict' is detected, because the createdById of the ACTaskCategory isn't updated when creating it; (And of course the openIssues when opening an issue on the GHRepository)  - not very relevant though 
 - [ ] - Creating a GHRepository, creates the ACProject, but then, subsequent calls, detect the creation of the ACProject (I think that then the Label is created, but that's ok [confirm]))


Issues:

- ACTask - CreatedById and UpdatedById [and body I think only when creating ?] is always 'changing' when 'creating'/'updating' (probably the fields differ when we are editing the ACTask);
- When creating a GHRepository, there are conflicts that are generated afterwards on the ACTaskCategory - because of the createdById - minor

 - CategoryId of an ACTask is always changing ?!?!
 