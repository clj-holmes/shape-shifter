(ns shape-shifter.transformers.boolean
  (:require
   [clojure.edn :as edn]))

(defn ->spec [value]
  (if (edn/read-string value)
    `true?
    `false?))
