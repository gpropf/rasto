(ns ^:figwheel-hooks rasto.core
  (:require
   [reagent.dom :as rd]
   [clojure.string :as str]
   [reagent.core :as reagent :refer [atom]]
   [cljs.pprint :as pp :refer [pprint]]
   [rasto.util :as rut]))


(defrecord Raster [dimensions screen-dimensions raw-data id])


(defn raw-data-array
  "Returns a raw rule-array cleared to provided value or 0."
  ([[width height] cell-state]
   (raw-data-array [width height] cell-state false))
  ([[width height] cell-state ^boolean transpose?]
   (let [w (if transpose? height width)
         h (if transpose? width height)]
     (vec (take w (repeat
                   (vec (take h (repeat cell-state)))))))))



(defn make-raster [[w h] [sw sh] default-value id]
  (->Raster [w h] [sw sh] (raw-data-array [w h] default-value) id)


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


(defn on-mouse-over-raster
  "Uses some attributes of the raster to decide how to set up the
  svg area to translate mouse clicks to grid locations."
  [raster]
  (fn [mev]
    (let [[x y] (relative-xy-to-grid-xy
                 (rut/position-relative-to-upper-left
                  mev (rut/key-to-string (:id raster))) raster)
          rfid (.-id (.-target mev))]
      (println "Mousing over " (:id raster) ":" rfid ":" [x y])
      #_(swap! app-state assoc :last-mouse-location [rfid [x y]]))))



(defn raster-view [raster-atom]
  (let [raster @raster-atom
        [w h] (:dimensions raster)
        [sw sh] (:screen-dimensions raster)]
    [:svg {:id (rut/key-to-string (:id raster))
           :style        {:margin-left "0.5em"}
           :stroke       "darkgrey"
           :stroke-width 0.02
           :fill         "dodgerblue"
           ;:class        "drawing raster"
           :height       sh
           :width        sw
           :viewBox [0 0 w h]
           :on-context-menu nil #_(fn [ev]
                                    (.preventDefault ev)
                                    (delete-raster! (:id raster)) false)
           :on-click nil #_(onclick-raster raster)
           :on-mouse-move (on-mouse-over-raster raster)
           :preserveAspectRatio "none"}
     [:rect {:key    :bkgd-rect
             :id     :bkgd-rect
             :width w
             :height h
             :fill   "grey"}]

     #_[:svg
        [:rect {:key    bkgd-rect-key
                :id     bkgd-rect-key
                :width  (:width raster)
                :height (:height raster)
                :fill   ((:pixel-color-map cfg) 0)}]
        [:path {:key          grid-path-key
                :id           grid-path-key
                :d            (raster-grid raster)
                :stroke       "blue"
                :stroke-width 0.02}]
        (map (fn [[x y pixel-state]]
               (let [pixel-key (rut/key-to-string "pixel" [x y])]
                 ^{:key (rut/genkey "fffff")}
                 [:rect {:key    pixel-key
                         :id     pixel-key
                         :x      x
                         :y      y
                         :width  1
                         :height 1
                         :fill   ((:pixel-color-map cfg) pixel-state)}]))
             pixels-to-show)
        #_(when @murf/debug
            (let [mask-pixels-to-show
                  (:updated-pixel-list (:da_1 (:rules @app-state)))
                  mask-pixel-count (count mask-pixels-to-show)]
              (map (fn [[x y _] i]
                     (let [pixel-key
                           (rut/key-to-string "-mask-pixel" [x [i y]])]
                       [:rect {:key    pixel-key
                               :id     pixel-key
                               :opacity 0.7
                               :x      x
                               :y      y
                               :width  1
                               :height 1
                               :fill   "99bb99"}]))
                   mask-pixels-to-show (range mask-pixel-count))))]])

  #_(pprint (:dimensions raster))
  #_(pprint (:raw-data raster)))


#_(defn raster-view ;old rule-frame-view func with the name changed.
  "Produces the SVG area in which the RF will be displayed."
  ([raster]
   (raster-view raster true))
  ([raster ^boolean display-name?]
   (raster-view raster display-name? 1))
  ([raster ^boolean display-name? tab-index]
   ^{:key (rut/genkey (:id raster))}
   (when raster
     (let
      [pixels-to-show (murf/list-pixels-rf raster)
       bkgd-rect-key  (rut/genkey "bkgd-rect-key_")
       grid-path-key  (rut/genkey "grid-path-key_")]
       [:div
        {:tab-index tab-index
         :key (:id raster)
         ;:on-key-down
         #_(fn [event]
           (let [pixel-state (- (int (.-keyCode event)) 48)
                 [rfid [x y]] (:last-mouse-location @app-state)
                 tentative-raster ((keyword rfid) (:rules @app-state))
                 raster (if (nil? tentative-raster)
                              (:da_1 (:rules @app-state))
                              tentative-raster)]
             (set-pixel! raster [x y] pixel-state)))
         :style {:position "relative"
                 :height (str (+ (:screen-height raster) 30) "px")
                 :float "left"
                 :margin-top "2em"}}
        [:svg {:id (rut/key-to-string (:id raster))
               :style        {:margin-left "0.5em"}
               :stroke       "darkgrey"
               :stroke-width 0.02
               :fill         "dodgerblue"
               :class        "drawing raster"
               :height       (:screen-height raster)
               :width        (:screen-width raster)
               :viewBox [0 0 (:width raster) (:height raster)]
               :on-context-menu (fn [ev]
                                  (.preventDefault ev)
                                  (delete-raster! (:id raster)) false)
               :on-click (onclick-raster raster)
               :on-mouse-move (on-mouse-over-raster raster)
               :preserveAspectRatio "none"}
         [:svg
          [:rect {:key    bkgd-rect-key
                  :id     bkgd-rect-key
                  :width  (:width raster)
                  :height (:height raster)
                  :fill   ((:pixel-color-map cfg) 0)}]
          [:path {:key          grid-path-key
                  :id           grid-path-key
                  :d            (raster-grid raster)
                  :stroke       "blue"
                  :stroke-width 0.02}]
          (map (fn [[x y pixel-state]]
                 (let [pixel-key (rut/key-to-string "pixel" [x y])]
                   ^{:key (rut/genkey "fffff")}
                   [:rect {:key    pixel-key
                           :id     pixel-key
                           :x      x
                           :y      y
                           :width  1
                           :height 1
                           :fill   ((:pixel-color-map cfg) pixel-state)}]))
               pixels-to-show)
          (when @murf/debug
            (let [mask-pixels-to-show
                  (:updated-pixel-list (:da_1 (:rules @app-state)))
                  mask-pixel-count (count mask-pixels-to-show)]
              (map (fn [[x y _] i]
                     (let [pixel-key
                           (rut/key-to-string "-mask-pixel" [x [i y]])]
                                     [:rect {:key    pixel-key
                                             :id     pixel-key
                                             :opacity 0.7
                                             :x      x
                                             :y      y
                                             :width  1
                                             :height 1
                                             :fill   "99bb99"}]))
                   mask-pixels-to-show (range mask-pixel-count))))]]
        (when display-name? [rf-button-bar raster app-state])]))))
