# rasto
A 2d grid (raster) control written in ClojureScript using Clojurescript cli tools.

This is a refactoring of some of the code from my Pixel Reactor app
(you can find it on my website). I'm essentially abstracting out the
code that allows you to manipulate a grid of cells. I thought this
might come in handy for other grid based apps as well.

## Usage

Currently the only thing to do to use it is to make sure the libs it
uses are loaded and also that a version of jQuery (1.9.1 or better) is
loaded above where the compiled JS for this is loaded. I know about
things Jayq but just didn't feel like getting involved in externs and
other stuff like that. My use of jQuery is pretty basic, limited to
things like getting web page elements.

## Building

The project is set up to be built with the Clojure CLI tools rather
than Leiningen or Boot. The following will build the project and start
a figwheel REPL.  `clj -M:dev`. You can make a production build with
`clj -M:prod`. This simply builds the JS target code. It does not
start a REPL or Figwheel.
