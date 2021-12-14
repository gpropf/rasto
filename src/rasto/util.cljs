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

(defn cell-add
  "Adds a 3-vec to a 2-vec by just passing the third value through and
  summing the first two."
  [[a b p] [c d]] [(+ a c) (+ b d) p])


(defn comma-delimited-list-to-ints
  "Take a comma delimited string and parses it into int values,
  returning them in a vector"
  [cdl]
  (vec (map #(int %) (str/split cdl #","))))


(defn genkey
  "Multi-arity function to create keywords either from strings
  verbatim or manufacture them from a stub string using gensym to
  guarantee global uniqueness."
  ([stub-string]
   (genkey stub-string false))
  ([stub-string use-stub-only?]
   (let [key-string   (if (true? use-stub-only?)
                        stub-string (gensym stub-string))]
     (keyword key-string))))


(defn key-to-string
  "This function takes a key which may be one of several types of thing or nil
   along with optional [x y] co-ords and makes a nicely formatted string out of
   it suitable for use as an id for a RF cell."
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
