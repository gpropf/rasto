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
* **[ON HOLD]** Add checks to make sure all commands have a keystroke mapping.
* Draw with brushes.

###Mui

 * Read objects from local file along with a load command. The write
   behavior is already implemented but may need work.
 * Ability to write characters into textareas. This is useful for writing tests.
 * **[DONE]** There should only be one selected object at a time for 
   the whole Mui system, not one for each type as at present.
 * **[DONE]** :brushes in Raster should not be a vector but a map,  with
    brushes indexed by :id
 * **[ON HOLD]** Selection by any method (i.e. mouse over/click, select
   command, etc.) should cause selection query to move on. 
 
 * **[ON HOLD]** Function to look at arglists of metadata
   and create a command map with prompts for each arg.
 * **[ON HOLD]** Need a way to edit the prompts and types of args for
   functions.
 * **[ON HOLD]** Need to be able to save/load functions and types for
   each app.


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
