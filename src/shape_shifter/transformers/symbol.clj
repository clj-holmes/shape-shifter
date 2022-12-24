(ns shape-shifter.transformers.symbol
  (:require
   [shape-shifter.config :as config]
   [shape-shifter.transformers.boolean :as boolean]
   [shape-shifter.transformers.wildcard :as wildcard]))

;; check if it is a boolean
(defn ^:private is-boolean? [value]
  (contains? #{"true" "false"} value))

;; checks if it is a wildcard
(defn ^:private is-wildcard? [value]
  (-> config/*wildcards*
      keys
      set
      (contains? value)))

;; checks if it is a wildcard with an operator 
;; that makes it match any number of strings
(defn ^:private is-wildcard*? [value]
  (let [wildcards* (->> config/*wildcards*
                        keys
                        (map #(str % "&"))
                        set)]
    (contains? wildcards* value)))

(defn ->spec [value]
  (cond
    (is-wildcard*? value) (wildcard/->spec* value)
    (is-wildcard? value) (wildcard/->spec value)
    (is-boolean? value) (boolean/->spec value)
    :else  #{`(symbol ~value)}))

(comment
  (->spec "$string"))
