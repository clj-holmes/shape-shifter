(ns shape-shifter.transformers.keyword
  (:require
   [clojure.string :as string]))

;; replaces the colon to an empty string, because if it apply 
;; the keyword function in a string which alreay contains a colon 
;; it would return ::string
(defn ->spec [value]
  (-> value
      (string/replace ":" "")
      keyword
      vector
      set))

(comment
  (->spec ":banana"))
