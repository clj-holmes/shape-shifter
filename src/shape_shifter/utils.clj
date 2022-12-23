(ns shape-shifter.utils
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as string]))

(defn ^:private random-char []
  (-> 26 rand (+ 65) char))

(defn build-hashmap-function [[key value]]
  (let [fn-name (->> key
                     first
                     name
                     (format "check-key-%s-value")
                     symbol)]
    `(fn ~fn-name [x#]
       (s/valid? ~value (get-in x# ~key)))))

(defn apply-on-macro [macro values]
  (->> values
       (reduce #(cons %2 %1) (list macro))
       reverse))

(defn random-keyword [size]
  (->> random-char
       repeatedly
       (take size)
       (apply str)
       string/lower-case
       keyword))

