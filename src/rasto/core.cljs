(ns ^:figwheel-hooks rasto.core
  (:require
   [reagent.dom :as rd]
   [clojure.string :as str]
   [reagent.core :as reagent :refer [atom]]
   [cljs.pprint :as pp :refer [pprint]]))


(defrecord Raster [dimensions screen-dimensions raw-data])


(defn raw-data-array
  "Returns a raw rule-array cleared to provided value or 0."
  ([[width height] cell-state]
   (raw-data-array [width height] cell-state false))
  ([[width height] cell-state ^boolean transpose?]
   (let [w (if transpose? height width)
         h (if transpose? width height)]
     (vec (take w (repeat
                   (vec (take h (repeat cell-state)))))))))



(defn make-raster [[w h] [sw sh] default-value ]
  (->Raster [w h] [sw sh] (raw-data-array [w h] default-value))


  )


(defn raster-view [raster]
  (pprint (:dimensions raster))
  (pprint (:raw-data raster)))


#_(defn rule-frame-view
  "Produces the SVG area in which the RF will be displayed."
  ([rule-frame]
   (rule-frame-view rule-frame true))
  ([rule-frame ^boolean display-name?]
   (rule-frame-view rule-frame display-name? 1))
  ([rule-frame ^boolean display-name? tab-index]
   ^{:key (mut/genkey (:id rule-frame))}
   (when rule-frame
     (let
      [pixels-to-show (murf/list-pixels-rf rule-frame)
       bkgd-rect-key  (mut/genkey "bkgd-rect-key_")
       grid-path-key  (mut/genkey "grid-path-key_")]
       [:div
        {:tab-index tab-index
         :key (:id rule-frame)
         ;:on-key-down
         #_(fn [event]
           (let [pixel-state (- (int (.-keyCode event)) 48)
                 [rfid [x y]] (:last-mouse-location @app-state)
                 tentative-rule-frame ((keyword rfid) (:rules @app-state))
                 rule-frame (if (nil? tentative-rule-frame)
                              (:da_1 (:rules @app-state))
                              tentative-rule-frame)]
             (set-pixel! rule-frame [x y] pixel-state)))
         :style {:position "relative"
                 :height (str (+ (:screen-height rule-frame) 30) "px")
                 :float "left"
                 :margin-top "2em"}}
        [:svg {:id (mut/key-to-string (:id rule-frame))
               :style        {:margin-left "0.5em"}
               :stroke       "darkgrey"
               :stroke-width 0.02
               :fill         "dodgerblue"
               :class        "drawing rule-frame"
               :height       (:screen-height rule-frame)
               :width        (:screen-width rule-frame)
               :viewBox [0 0 (:width rule-frame) (:height rule-frame)]
               :on-context-menu (fn [ev]
                                  (.preventDefault ev)
                                  (delete-rule-frame! (:id rule-frame)) false)
               :on-click (onclick-rule-frame rule-frame)
               :on-mouse-move (on-mouse-over-rule-frame rule-frame)
               :preserveAspectRatio "none"}
         [:svg
          [:rect {:key    bkgd-rect-key
                  :id     bkgd-rect-key
                  :width  (:width rule-frame)
                  :height (:height rule-frame)
                  :fill   ((:pixel-color-map cfg) 0)}]
          [:path {:key          grid-path-key
                  :id           grid-path-key
                  :d            (rule-frame-grid rule-frame)
                  :stroke       "blue"
                  :stroke-width 0.02}]
          (map (fn [[x y pixel-state]]
                 (let [pixel-key (mut/key-to-string "pixel" [x y])]
                   ^{:key (mut/genkey "fffff")}
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
                           (mut/key-to-string "-mask-pixel" [x [i y]])]
                                     [:rect {:key    pixel-key
                                             :id     pixel-key
                                             :opacity 0.7
                                             :x      x
                                             :y      y
                                             :width  1
                                             :height 1
                                             :fill   "99bb99"}]))
                   mask-pixels-to-show (range mask-pixel-count))))]]
        (when display-name? [rf-button-bar rule-frame app-state])]))))
