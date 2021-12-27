(ns ^:figwheel-hooks rasto.core
  (:require
    [reagent.dom :as rd]
    [clojure.string :as str]
    [reagent.core :as reagent :refer [atom]]
    [cljs.pprint :as pp :refer [pprint]]
    [rasto.util :as rut]
    [mui.core :as mc]))


(defrecord Raster [dimensions                               ;width and height in abstract units as a 2-vec: [w h].
                   screen-dimensions                        ;screen width and height in pixels: [sw sh].
                   raw-data                                 ;vector of vectors where each inner vec is a column.
                   id                                       ;unique keyword identifying Raster object.
                   hover-fn                                 ;run this on mouse hover.
                   left-click-fn                            ;run for left click.
                   right-click-fn                           ;run for right click.
                   cell-state-to-color-index-fn             ;translate a cell's state into a color index fn.
                   cell-is-visible-fn                       ;decide whether the cell should be visible or not.
                   brushes                                  ;map of small rasters used as brushes to draw on the larger one.
                   is-brush?                                ;boolean: true if this is a brush of another raster.
                   color                                    ;The current color to use for drawing cells.
                   parent-raster-atom                       ;The raster that contains this one.
                   ])


(defn raw-data-array
  "Returns a raw cell-array cleared to provided value."
  ([[width height] cell-state]
   (raw-data-array [width height] cell-state false))
  ([[width height] cell-state ^boolean transpose?]
   (let [w (if transpose? height width)
         h (if transpose? width height)]
     (vec (take w (repeat
                    (vec (take h (repeat cell-state)))))))))


(defn make-raster
  "Constructor function for the Raster."
  ([[w h] [sw sh] default-value id
    hover-fn left-click-fn right-click-fn
    cell-state-to-color-index-fn cell-is-visible-fn]
   (make-raster [w h] [sw sh] default-value id
                hover-fn left-click-fn right-click-fn
                cell-state-to-color-index-fn cell-is-visible-fn {} false 1 nil))
  ([[w h] [sw sh] default-value id
    hover-fn left-click-fn right-click-fn
    cell-state-to-color-index-fn cell-is-visible-fn brushes is-brush? initial-color parent-raster-atom]
   (->Raster [w h] [sw sh] (raw-data-array [w h] default-value) id
             hover-fn left-click-fn right-click-fn
             cell-state-to-color-index-fn cell-is-visible-fn brushes is-brush? initial-color parent-raster-atom)))


;; A ticket system for Rasto to provide unique id numbers.
(def tickets (atom 0))


(defn get-main-raster
  "A utility function to simplify getting the first raster we create. We're
   assuming that any app that uses this library will have at least one main
   Raster. This was specifically written to facilitate the writing of the
   rasto.example app and perhaps belongs there instead of here."
  []
  (:obj (mc/get-object-from-object-store :rst1)))


(defn take-ticket!
  "Get a ticket and increment the counter."
  []
  (let
    [ticket-num @tickets]
    (swap! tickets inc)))


(defn set-color! [raster-atom c]
  (let [raster @raster-atom]
    (swap! raster-atom assoc :color c)
    (mapv (fn [[brush-id brush]]
            (set-color! brush c)) (:brushes raster))))


(defn new-brush
  "Creates a new Raster set up as a brush."
  [raster-atom [w h] [sw sh]]
  (let [raster @raster-atom
        brush (make-raster [w h] [sw sh] 0
                           (keyword
                             (str (name (:id raster)) "-brush" (take-ticket!)))
                           (:hover-fn @raster-atom)
                           (:left-click-fn @raster-atom)
                           (:right-click-fn @raster-atom)
                           (:cell-state-to-color-index-fn raster)
                           (:cell-is-visible-fn raster) [] true (:color raster) (:id raster))]
    (atom brush)))


(defn delete-brush! [raster-atom brush-id]
  (swap! raster-atom update :brushes dissoc brush-id))


(defn delete-brush2! [brush]
  (let [parent-raster-atom (:parent-raster-atom brush)
        brush-id (:id brush)]
    (swap! parent-raster-atom update :brushes dissoc brush-id)

    ))

(defn set-cell
  "Set the cell at [x y] to cell-state"
  ([raster [x y cell-state]]
   (set-cell raster [x y] cell-state))
  ([raster [x y] cell-state]
   (assoc-in raster [:raw-data x y] cell-state)))


(defn set-cell! [raster-atom [x y cell-state]]
  (reset! raster-atom (set-cell @raster-atom [x y cell-state])))


(defn set-cells! [raster-atom ints]
  (let [len-ints (count ints)
        r (mod len-ints 3)
        num-ints-to-vectorize (- len-ints r)
        ints-to-vectorize (take num-ints-to-vectorize ints)
        vec3s (map vec (partition 3 ints-to-vectorize))
        ]
    (doseq [v vec3s]
      (set-cell! raster-atom v))))




(mc/register-application-defined-type
  :Brush
  {:new
           {:fn   (fn [arg-map]
                    (let [w (get-in arg-map [:w :val])
                          h (get-in arg-map [:h :val])
                          parent-raster-atom (get-main-raster)
                          brush (new-brush parent-raster-atom
                                           [w h]
                                           [100 100])]
                      #_(swap! parent-raster-atom update :brushes conj brush)
                      (swap! parent-raster-atom update :brushes conj {(:id @brush) brush})
                      (mc/add-object-to-object-store brush :Brush (:id @brush) (:id @parent-raster-atom))
                      (println "NEW BRUSH: " brush)
                      (println "ARG-MAP in applied fn: " arg-map)
                      (println "Creating new brush, width: " w ", height: " h)
                      (swap! mc/mui-state assoc :return-to-normal true)))
            :args
                  {:w
                   {:prompt "Width of new brush?"
                    :type   :int}
                   :h
                   {:prompt "Height of new brush?"
                    :type   :int}}
            :help {:msg "b\t: Make new brush."}}
   :delete {:fn   (fn [arg-map]
                    (let [;;brush-atom (get-in arg-map [:selected-object :val])
                          brush-atom-id (get-in arg-map [:selected-obj-id :val])
                          brush-atom-map (mc/get-object-from-object-store :Brush brush-atom-id)
                          brush-parent-atom-id (:parent-obj-id brush-atom-map)
                          brush-parent-atom (:obj (mc/get-object-from-object-store brush-parent-atom-id))
                          ;;cmd-txtarea (. js/document getElementById "command-window")  ;; FIXME - it can't find the field!
                          ]
                      (println "ARG-MAP in delete fn: " arg-map)
                      (println (str "Would be deleting the Brush: " brush-atom-id))
                      (println (str "Brush has parent with id: " brush-parent-atom-id))
                      (swap! brush-parent-atom update :brushes dissoc brush-atom-id)
                      #_(mc/println-fld cmd-txtarea (str "Would be deleting the Brush: " brush-atom-id))

                      #_(mc/println-fld cmd-txtarea "FOOOOOOOOOOOOOOOOOOOOO")
                      ))
            :args {}
            :help {}

            }})


(def rasto-cmd-maps {:key-sym-keystroke-map {:c [67 false false false false]

                                             :p [80 false false false false]
                                             }
                     :cmd-func-map          {:c
                                             {:fn               (fn [arg-map]
                                                                  (let [c (get-in arg-map [:c :val])
                                                                        parent-raster-atom
                                                                        (get-main-raster)]
                                                                    (set-color! parent-raster-atom c)
                                                                    (println "NEW COLOR: " c)))
                                              :args
                                                                {:c
                                                                 {:prompt "New color value (1-9)?"
                                                                  :type   :int}}
                                              :help             {:msg "c\t: Change working color."}
                                              :active-in-states (set [:normal])}
                                             :p
                                             {:fn               (fn [arg-map]
                                                                  (let [pxls (get-in arg-map [:pxls :val])]
                                                                    (set-cells! (get-main-raster) pxls)
                                                                    (println "Integers for cells as text: " pxls)))
                                              :args             {:pxls
                                                                 {:prompt "Enter a cell value or values"
                                                                  :type   :int-list}}
                                              :help             {:msg "p\t: Place pixel(s)"}
                                              :active-in-states (set [:normal])}
                                             }})


(defn relative-xy-to-grid-xy
  "For the given raster, transform screen coordinates relative to the
  upper-left corner of its svg element into grid coordinates using its
  internal grid system. Returns an [int int] vec."
  [[x y] raster]
  (let [[w h] (:dimensions raster)
        [sw sh] (:screen-dimensions raster)
        width-to-screen-width-ratio (/ w sw)
        height-to-screen-height-ratio (/ h sh)]
    (map #(int %) [(* x width-to-screen-width-ratio) (* y height-to-screen-height-ratio)])))










(defn list-cells
  "Pseudo-code:

   1. Create a row-col indexed map of the cell values in :raw-data

   2. Filter that map based on whether the :cell-is-visible-fn returns
      true for that cell

   3. Use apply/concat to flatten the results as in the example in
      https://clojuredocs.org/clojure.core/concat

  4. Return the results as a vector of 3-vectors [x y cell-state]."
  [raster]
  (vec (map #(-> % vec)
            (apply concat (map-indexed
                            (fn [col e]
                              (let [value-mapping
                                    (map-indexed
                                      (fn [row ee] [col, row, ee]) e)]
                                (filter (fn [[_, _, v]]
                                          ((:cell-is-visible-fn raster) v))
                                        value-mapping)))
                            (:raw-data raster))))))


(defn raster-view-grid
  "Makes the grid of lines that divide the Raster into cells."
  [raster]
  (let [[w h] (:dimensions raster)]
    (concat (map
              (fn [y] (str "M 0 " y " L " w " " y))
              (range 0 h))
            (map
              (fn [x] (str "M " x " 0 L " x " " h))
              (range 0 w)))))


(defn raster-view
  "Provides a visual representation of the Raster and basic
  interactivity with it to allow the user to modify the contents. The
  left-click-fn and other mouse action functions are a bit
  tricky. Instead of being mouse event handlers they must be functions
  that accept a raster-atom and return a mouse event handler. This is
  to allow the fields of our Raster object to play a role in the
  behavior of the mouse event handlers even though they are single
  valued functions with no room for arbitrary extra args like our
  raster-atom. See the rasto-test code for examples of how these
  work."
  [raster-atom app-cfg app-cmd-map]
  (let [raster @raster-atom
        [w h] (:dimensions raster)
        [sw sh] (:screen-dimensions raster)
        cells-to-show (list-cells raster)
        grid-path-key (rut/genkey "grid-path-key_")
        brushes (:brushes raster)
        is-brush? (:is-brush? raster)]
    [:div {:style {:float "left"}}
     ; (println "rasto/core - CMDS1: ")
     ; (pprint app-cfg)
     ; {:c [67 false false false false]}
     (when (false? is-brush?)
       [mc/mui-gui (:mui-cfg app-cfg) (merge rasto-cmd-maps app-cmd-map)])
     (when (not-empty brushes)
       [:div {:id "brushes"} (map (fn [[brush-id brush-raster-atom]]
                                    ^{:key (rut/genkey "brush")} [raster-view brush-raster-atom app-cfg])
                                  brushes)])
     [:svg {:id                  (rut/key-to-string (:id raster))
            :style               {:margin-left "0.5em" :border "medium solid green"}
            :stroke              "darkgrey"
            :stroke-width        0.02
            :fill                "dodgerblue"
            ;:class        "drawing raster"
            :height              sh
            :width               sw
            :viewBox             [0 0 w h]
            :on-context-menu     ((:right-click-fn raster) raster-atom)
            :on-click            ((:left-click-fn raster) raster-atom)
            :on-mouse-move       ((:hover-fn raster) raster-atom)
            :on-mouse-down       (if (not (:is-brush? raster)) ((:mouse-down-fn raster) raster-atom) nil)
            :on-mouse-up         (if (not (:is-brush? raster)) ((:mouse-up-fn raster) raster-atom) nil)
            :preserveAspectRatio "none"}
      [:rect {:key    :bkgd-rect
              :id     :bkgd-rect
              :width  w
              :height h
              :fill   "#ffe"}]
      [:path {:key          grid-path-key
              :id           grid-path-key
              :d            (raster-view-grid raster)
              :stroke       "lightgrey"
              :stroke-width 0.02}]
      (let [[mx my] (:last-mouse-location raster)
            mouse-cell-key (rut/key-to-string "mouse-cell" [mx my])]
        [:rect {:id           mouse-cell-key
                :x            mx
                :key          mouse-cell-key
                :y            my
                :width        1
                :height       1
                :fill         "none"
                :stroke       "green"
                :stroke-width 0.03}])
      (map (fn [[x y cell-state]]
             (let [cell-key (rut/key-to-string "cell" [x y])]
               ^{:key (rut/genkey "cell")}
               [:rect {:id     cell-key
                       :x      x
                       :key    cell-key
                       :y      y
                       :width  1
                       :height 1
                       :fill   ((:cell-color-map app-cfg)
                                ((:cell-state-to-color-index-fn raster) cell-state))}]))
           cells-to-show)]]))
