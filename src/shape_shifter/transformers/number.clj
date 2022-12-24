(ns shape-shifter.transformers.number
  (:require
   [clojure.edn :as edn]))

(defn ->spec [value]
  #{(edn/read-string value)})

(comment
  (->spec "1000"))
