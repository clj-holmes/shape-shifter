(ns shape-shifter.utils
  (:require [clojure.string :as string]))

(defn ^:private random-char []
  (-> 26 rand (+ 65) char))

(defn random-keyword [size]
  (->> random-char
       repeatedly
       (take size)
       (apply str)
       string/lower-case
       keyword))