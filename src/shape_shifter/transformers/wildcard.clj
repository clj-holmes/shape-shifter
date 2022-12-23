(ns shape-shifter.transformers.wildcard
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as string]
   [shape-shifter.config :as config]))

(defn ->spec* [value]
  (let [value (string/replace value "&" "")]
    `(s/* ~(get config/*wildcards* value))))

(defn ->spec [value]
  (if (= (last value) \&)
    `(s/* ~(get config/*wildcards* value))
    (get config/*wildcards* value)))
