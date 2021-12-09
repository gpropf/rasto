# TODO.md

This is a list of items to complete for both this project and the related mui project. It also serves and a kind of
design document.


## Bug Fixes

###Rasto




##New Features

###Rasto

* Replay history to recreate objects.
* Command for adding cells.
* Integrate the inverse mapping function into the cmd-maps.
* Add checks to make sure all commands have a keystroke mapping.
* Draw with brushes.

###Mui

* Selection by any method (i.e. mouse over/click, select
command, etc.) should cause selection query to move on. 
* Read/Write objects to local file along with a
load/save pair of commands.
  * Needs work. Need to develop file format and load/save funcs.

##Design Level Items

###Mui

* _Everything_ including prompt text and defaults for which
parameters to ask the user for vs just using default
parameter value is configured from template files loaded at runtime.


* Selection can be by three means:
  1. Answer the query about which thing to select.
  2. Click on the object to be selected.
  3. Click on the object's representation in Structure View.

* Formal definition of query chaining process and operators.
