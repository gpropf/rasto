(ns rasto.util
  (:require
   [clojure.string :as str]
   #_[cljs.pprint :as pp :refer [pprint]]
   #_[mulife.rule-frame :as murf :refer [make-rule-frame
                                            use-onclick-to-get-location?
                                            rule-frame-grid]]))


(enable-console-print!)

(defn v2-sub [[a b & _] [c d & _]] [(- a c) (- b d)])

(defn v2-add [[a b & _] [c d & _]] [(+ a c) (+ b d)])

(defn pixel-add [[a b p] [c d]] [(+ a c) (+ b d) p])


(defn blank-or-nil?
  "Return true if the argument is an empty structure (i.e. empty string) or nil"
  [a]
  (or (nil? a) (= 0 (count a))))


(defn not-blank-or-nil?
  "Opposite of blank-or-nil?"
  [a]
  (not (blank-or-nil? a)))


(defn comma-delimited-list-to-ints
  "Take a comma delimited string and parses it into int values, returning them in a vector"
  [cdl]
  (vec (map #(int %) (str/split cdl #","))))



(defn genkey
  "Multi-arity function to create keywords either from strings verbatim or manufacture
   them from a stub string using gensym to guarantee global uniqueness."
  ([stub-string]
   (genkey stub-string false))
  ([stub-string use-stub-only?]
   (let [key-string   (if (true? use-stub-only?)
                        stub-string (gensym stub-string))]
     (keyword key-string))))

(defn key-to-string
  "This function takes a key which may be one of several types of thing or nil
   along with optional [x y] coords and makes a nicely formatted string out of
   it suitable for use as an id for a RF pixel."
  ([k]
   (cond
     (nil? k) "NIL"
     (keyword? k) (name k)
     (symbol? k) (str k)
     :else (str k)))
  ([k [x y]]
   (str (key-to-string k) ":" x "," y)))

(defn position-relative-to-upper-left
  "Takes a mouse event, derives its target element and tells you where the event
   was relative to the upper-left corner of the element."
  [mev element-id]
  (let [comp (.get (js/jQuery (str "#" element-id)) 0)
        bcr  (. comp getBoundingClientRect)
        mx   (. mev -clientX)
        my   (. mev -clientY)
        ax   (. bcr -x)
        ay   (. bcr -y)
        rx   (- mx ax)
        ry   (- my ay)]
    (vec (list rx ry))))

(defn toggleable-ks-icon-button
  "This is a work-around to deal with the fact that KS's JS code
  replaces your icon tag with SVG.  Reagent doesn't know about this
  though and then can't find the tag to change the icon when you need
  to do that.  My solution is to have the span *containing* the icon
  tag toggle its display style between 'none' and 'block' as the state
  changes.  Then we include spans for *both* states, only one of which
  is visible at a given time.  The effect seems to work at least in
  Chrome."
  [icon-pair hint-pair toggle-fn size icon-family state-key state-atom id]
  (let [button-state (state-key @state-atom)]
    [:button {:id       id
              :type     "button"
              :on-click #(do
                           (swap! state-atom update-in [state-key] not)
                           (toggle-fn))
              :class    size
              :alt      (hint-pair button-state)
              :title      (hint-pair button-state)}
     [:span (if (not (state-key @state-atom)) {:style {:display "block"}} {:style {:display "none"}})
      [:svg {:class (str icon-family " fa-" (get icon-pair button-state))}]]
     [:span (if (state-key @state-atom) {:style {:display "block"}} {:style {:display "none"}})
      [:svg {:class (str icon-family " fa-" (get icon-pair (not button-state)))}]]]))

#_(defn to-json
  "Wrapper around js/JSON.stringify.  If you convert CLJS objects using the stringify method
   directly you get a lot of garbage in the the JSON."
  [clj-object]
  (-> clj-object clj->js js/JSON.stringify))

(defn list-display
  "Just what it sounds like, give it a heading and a list and it will make an <ol> from them."
  [heading lst columns]
  [:div {:class (str "col_" columns)}
   [:h5 heading]
   [:i"Starred items have high importance according to number of stars."]
   [:ol
    (map (fn [i] [:li {:key (gensym "todo-list")} i]) lst)]])

(defn vectors-match?
  "Uses a procedural approach to comparing vectors.  Bails out and returns false
  as soon as it finds an inequality."
  [v1 v2]
  (let [n (count v1)]
    (loop [i 0]
      (if (< i n)
        (if (= (nth v1 i) (nth v2 i))
          (recur (inc i))
          false)
        true))))
