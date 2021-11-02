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
   [cljs.pprint :as pp :refer [pprint]]
   [rasto.util :as rut]))

(enable-console-print!)

(def default-cell-state 100)


(defonce
  rasto-test-cfg {; :off-cell-color "#F5F5DC"
                  :off-cell-color "#FFFFFF"
                  ;; :on-cell-color "#1e90ff"
                  :on-cell-color "#555555"
                  :cell-color-map {0 "#F5F5DC"
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
                  :default-grid-dimensions "60,40"
                  :mui-cfg {:command-window {:style {:height "auto"
                                                     :margin-bottom "5px"
                                                     :float "right"}
                                             :id "command-window"
                                             :rows "8"
                                             :cols "60"
                                             :class ""
                                             ;:default-value ""
                                             }
                            :app-cmds {}}})


(defn hover-fn
  "This the function we feed to the Raster for hover events."
  [raster-atom]
  (fn [mev]
    (let [raster @raster-atom
          [x y] (rcore/relative-xy-to-grid-xy
                 (rut/position-relative-to-upper-left
                  mev (rut/key-to-string (:id raster))) raster)
          ]
      #_(println "Mousing over " (:id raster) ":" [x y])
      (swap! raster-atom assoc :last-mouse-location [x y]))))


(defn left-click-fn
  "Left mouse click fn"
  [raster-atom]
  (fn [mev]
    (let [raster @raster-atom
          last-mouse-location (:last-mouse-location raster)]
      (reset! raster-atom (rcore/set-cell raster last-mouse-location 101))
      (println "Click at: " last-mouse-location))))


(defn right-click-fn
  "Right mouse click fn."
  [raster-atom]
  (fn [mev] (println "Right click placeholder fn triggered for
  id " (:id @raster-atom) " at " (:last-mouse-location @raster-atom))))


(defn cell-state-to-color-index-fn
  "This is a simple example of how the translation from cell state to
  color index works. It just subtracts the default cell value to
  produce a color index."
  [cell-state]
  (- cell-state default-cell-state))


(defn cell-is-visible-fn
  "This is a simple example of how we decide if a cell is visible. I
  this case we just return true if the color index is > 0."
  [cell-state]
  (> (cell-state-to-color-index-fn cell-state) 0) )


(defonce raster-atom (atom (make-raster
                        [60 40] [800 600] default-cell-state :rst1
                        hover-fn left-click-fn right-click-fn
                        cell-state-to-color-index-fn cell-is-visible-fn)))


(defn app
  "Creates the app and all its controls.  Everything we use is called
  from here."
  []
  [:div {}
   [raster-view raster-atom rasto-test-cfg]])


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
