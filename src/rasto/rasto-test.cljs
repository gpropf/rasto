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

(def raster-atom (atom (make-raster [60 40] [600 400] 1 :rst1)))


(defonce cfg {; :off-pixel-color "#F5F5DC"
              :off-pixel-color "#FFFFFF"
              ;; :on-pixel-color "#1e90ff"
              :on-pixel-color "#555555"
              :pixel-color-map {0 "#F5F5DC"
                                1 "#00FF00"
                                2 "#00AA00"
                                3 "#FF00FF"
                                4 "#AA00AA"
                                5 "#00AAFF"
                                6 "#9090AA"
                                7 "#888888"}
              :default-da-screen-width 600
              :default-da-screen-height 400
              :default-rule-frame-screen-width 150
              :default-rule-frame-screen-height 150
              :default-da-width 60
              :default-da-height 40
              :default-download-filename "rules.edn"
              :default-rf-dimensions "5,5"
              :default-starting-rule-frame-index 1
              :default-grid-dimensions "60,40"})


(defn app
  "Creates the app and all its controls.  Everything we use is called
  from here."
  []
  (let []
    [:div {} "RASTO-TEST"
     [raster-view raster-atom cfg]


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
