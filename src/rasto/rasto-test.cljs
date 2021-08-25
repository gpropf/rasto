(ns rasto.rasto-test
  "This module basically exists only as an entry point for the
  code. It's essentially 'int main ()' and shouldn't really do much
  itself. We call the function that creates the GUI from here and this
  kicks everything else off."
  (:require
   [rasto.core :as rcore :refer [make-raster raster-view]]
   [reagent.dom :as rd]
   [clojure.string :as str]
   [reagent.core :as reagent :refer [atom]]
   [cljs.pprint :as pp :refer [pprint]]))

(enable-console-print!)


(defn app
  "Creates the app and all its controls.  Everything we use is called
  from here."
  []
  (let [raster-atom (atom (make-raster [4 3] [200 150] "cell" :rst1))
        ]
    [:div {} "RASTO-TEST"
     [raster-view raster-atom]


     ]))



(defn render-app
  "Call into Reagent to attach the app to a particular JS DOM id and
  make it visible."
  []
  (rd/render
   [app]
   (js/document.getElementById "app")))


(render-app)


(defn ^:after-load re-render []
  (render-app))





(defonce start-up (render-app))
