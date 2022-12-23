(ns shape-shifter.transform
  (:require
   [clojure.spec.alpha :as s]
   [parcera.core :as parcera]
   [shape-shifter.sci :as sci]
   [shape-shifter.transformers.keyword :as transformer.keyword]
   [shape-shifter.transformers.number :as transformer.number]
   [shape-shifter.transformers.regex :as transformer.regex]
   [shape-shifter.transformers.string :as transformer.string]
   [shape-shifter.transformers.symbol :as transformer.symbol]
   [shape-shifter.utils :as utils]))

(defmulti ^:private transform (fn [[key _]]  key))

(defn ^:private recursively-transform [coll-of-specs value]
  (let [random-key-name (utils/random-keyword 5)
        transformed-value (transform value)]
    (if transformed-value
      (conj coll-of-specs random-key-name transformed-value)
      coll-of-specs)))

(defn ^:private pattern->collection [patterns kind]
  (let [result (->> patterns
                    (reduce recursively-transform [])
                    (utils/apply-on-macro `s/cat))]
    `(->> ~result
          (s/and ~kind)
          s/spec)))

(defmethod ^:private transform :map [[_ & values]]
  (let [result (->> values
                    (filter (comp #(not= :whitespace %) first))
                    (map transform)
                    (partition 2)
                    (map utils/build-hashmap-function)
                    first)]
    `(-> ~result s/and s/spec)))

(defmethod ^:private transform :list [[_ & values]]
  (pattern->collection values `list?))

(defmethod ^:private transform :vector [[_ & values]]
  (pattern->collection values `vector?))

(defmethod ^:private transform :set [[_ & values]]
  (pattern->collection values `set?))

(defmethod ^:private transform :symbol [[_ value]]
  (transformer.symbol/->spec value))

(defmethod ^:private transform :number [[_ value]]
  (transformer.number/->spec value))

(defmethod ^:private transform :string [[_ value]]
  (transformer.string/->spec value))

(defmethod ^:private transform :keyword [[_ value]]
  (transformer.keyword/->spec value))

(defmethod ^:private transform :regex [[_ value]]
  (transformer.regex/->spec value))

(defmethod ^:private transform :whitespace [[_ _]] nil)

(defn ->spec [pattern]
  (let [parsed-code (-> pattern
                        (parcera/ast :unhide :literals)
                        (nth 1))]
    (-> parsed-code transform str sci/eval-string)))
