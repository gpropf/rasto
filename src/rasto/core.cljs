(ns ^:figwheel-hooks rasto.core
  (:require
   [reagent.dom :as rd]
   [clojure.string :as str]
   [reagent.core :as reagent :refer [atom]]
   [cljs.pprint :as pp :refer [pprint]]
   [rasto.util :as rut]))


(defrecord Raster [dimensions screen-dimensions raw-data id hover-fn
                   left-click-fn right-click-fn cell-state-to-color-index-fn cell-is-visible-fn])


(defn raw-data-array
  "Returns a raw rule-array cleared to provided value or 0."
  ([[width height] cell-state]
   (raw-data-array [width height] cell-state false))
  ([[width height] cell-state ^boolean transpose?]
   (let [w (if transpose? height width)
         h (if transpose? width height)]
     (vec (take w (repeat
                   (vec (take h (repeat cell-state)))))))))



(defn make-raster [[w h] [sw sh] default-value id hover-fn left-click-fn right-click-fn cell-state-to-color-index-fn cell-is-visible-fn]
  (->Raster [w h] [sw sh] (raw-data-array [w h] default-value) id hover-fn left-click-fn right-click-fn cell-state-to-color-index-fn cell-is-visible-fn)


  )


(defn relative-xy-to-grid-xy
  "For the given raster transform screen coordinates relative to the upper-left corner of its
  svg element into grid coordinates using its internal grid system.  Returns an [int int] vec."
  [[x y] raster]
  (let [[w h] (:dimensions raster)
        [sw sh] (:screen-dimensions raster)

        width-to-screen-width-ratio (/ w sw)
        height-to-screen-height-ratio (/ h sh)]
    (map #(int %) [(* x width-to-screen-width-ratio) (* y height-to-screen-height-ratio)])))

(defn set-pixel [raster [x y] pixel-state]
  (assoc-in raster [:raw-data x y] pixel-state))


#_(defn on-mouse-over-raster
  "Uses some attributes of the raster to decide how to set up the
  svg area to translate mouse clicks to grid locations."
  [raster-atom]
  (fn [mev]
    (let [raster @raster-atom
          [x y] (relative-xy-to-grid-xy
                 (rut/position-relative-to-upper-left
                  mev (rut/key-to-string (:id raster))) raster)
          rfid (.-id (.-target mev))]
      (println "Mousing over " (:id raster) ":" [x y])
      (swap! raster-atom assoc :last-mouse-location [x y]))))

#_(defn on-mouse-click-raster
  ""
  [raster-atom]
  (fn [mev]
    (let [raster @raster-atom
          last-mouse-location (:last-mouse-location raster)]
      (reset! raster-atom (set-pixel raster last-mouse-location 5) )
      (println "Click at: " last-mouse-location)


      )))





(defn list-pixels
  "In English:
   1. Create a row-col indexed map of the pixel values in :rule-array
   2. Filter that map based on whether the pixel states are greater than 0
   3. Use apply/concat to flatten the results as in the example in
      https://clojuredocs.org/clojure.core/concat
   4. Return the results as a vector of 3-vectors."
  [raster]
  (vec (map #(-> % vec)
            (apply concat (map-indexed
                           (fn [col e] (let [value-mapping (map-indexed
                                                            (fn [row ee] [col,row,ee]) e)]
                                         (filter (fn [[_,_,v]]
                                                   ((:cell-is-visible-fn raster) v)) value-mapping)))
                           (:raw-data raster))))))




(defn raster-view-grid
  ""
  [raster]
  (let [[w h] (:dimensions raster)]
    (concat (map
           (fn [y] (str "M 0 " y " L " w " " y))
           (range 0 h))
          (map
           (fn [x] (str "M " x " 0 L " x " " h))
           (range 0 w)))))



(defn raster-view [raster-atom cfg]
  (let [raster @raster-atom
        [w h] (:dimensions raster)
        [sw sh] (:screen-dimensions raster)
        pixels-to-show (list-pixels raster)
        grid-path-key  (rut/genkey "grid-path-key_")]
    [:svg {:id (rut/key-to-string (:id raster))
           :style        {:margin-left "0.5em"}
           :stroke       "darkgrey"
           :stroke-width 0.02
           :fill         "dodgerblue"
           ;:class        "drawing raster"
           :height       sh
           :width        sw
           :viewBox [0 0 w h]
           :on-context-menu ((:right-click-fn raster) raster-atom)
           :on-click ((:left-click-fn raster) raster-atom)
           :on-mouse-move ((:hover-fn raster) raster-atom)
           :preserveAspectRatio "none"}
     [:rect {:key    :bkgd-rect
             :id     :bkgd-rect
             :width w
             :height h
             :fill   "#e6ffff"}]
     [:path {:key          grid-path-key
             :id           grid-path-key
             :d            (raster-view-grid raster)
             :stroke       "lightgrey"
             :stroke-width 0.02}]
     (map (fn [[x y cell-state]]
            (let [pixel-key (rut/key-to-string "cell" [x y])]
              ^{:key (rut/genkey "cell")}
              [:rect {:key    pixel-key
                      :id     pixel-key
                      :x      x
                      :y      y
                      :width  1
                      :height 1
                      :fill   ((:pixel-color-map cfg)
                               ((:cell-state-to-color-index-fn raster) cell-state))}]))
          pixels-to-show)]))
