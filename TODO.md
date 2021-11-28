# TODO.md

This is a list of items to complete for both this project and the related mui project. It also serves and a kind of
design document.


## Bug Fixes

###Rasto

1. **DONE**: Color for new brushes should be set to the current color of
the parent raster.


##New Features

###Rasto

* Done: Ability to click and hold button down to draw freehand line.

###Mui

1. DONE: Error recovery on bad data entry.
Questions should be re-asked if user enters malformed text.
2. Selection by any method (i.e. mouse over/click, select
command, etc.) should cause selection query to move on.
3. Read/Write objects to local file along with a
load/save pair of commands.

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
