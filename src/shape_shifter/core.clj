(ns shape-shifter.core
  (:gen-class)
  (:require
   [shape-shifter.transform :as transform]))

(defn pattern->spec [pattern]
  (transform/->spec pattern))
