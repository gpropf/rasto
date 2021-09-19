(ns rasto.mui
  "mui: Minimalist User Interface. The idea here is that one should not
  spend time developing a complex gui for an app unless you are either
  done with the core functionality or going to have non-technical
  people using it. Guis are enormously expensive to create and serve
  little purpose if the user base is just yourself and a handful of
  others. Here most commands are going to be just a single letter and
  are processed immediately, rather than after pressing 'Enter'. The
  MUI, however will display the commands you've typed and will also
  prompt you for any arguments that may be needed. I imagine this
  prompting will occur in one window and your command input (along
  with arguments) will appear in another. As such MUI lends itself to
  the creation of macros and programs automatically. To duplicate what
  you've done, just copy and paste the commands you've typed. This can
  be saved as a program and replayed. This is one advantage over GUIs
  where replicating a series of actions exactly can be quite difficult
  or even impossible."
  (:require
   [reagent.dom :as rd]
   [clojure.string :as str]
   [reagent.core :as reagent :refer [atom]]
   [cljs.pprint :as pp :refer [pprint]]
   [rasto.util :as rut]))


(def commands {"b"
               {:args
                {:w
                 {:prompt "Width of new brush?"}
                 :h
                 {:prompt "Width of new brush?"}}}})






(defn mui-gui [raster cfg]
  [:div
   [:textarea  {:style {:height "auto"
                        :margin-bottom "5px"
                        :float "right"}
                :id "command-window"
                :rows "4"
                :cols "80"
                :class ""
                :default-value ""
                :on-key-press (fn [event] (println (.-key event)))
                }]
   [:div ;output frame
    ]
   [:div ;maybe status bar or something
    ]])
