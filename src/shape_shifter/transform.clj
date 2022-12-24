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

;; LIST
(defmethod ^:private transform :list [[_ & values]]
  (pattern->collection values `list?))

;; VECTOR
(defmethod ^:private transform :vector [[_ & values]]
  (pattern->collection values `vector?))

;; SET
(defmethod ^:private transform :set [[_ & values]]
  (pattern->collection values `set?))

;; SYMBOL
(defmethod ^:private transform :symbol [[_ value]]
  (transformer.symbol/->spec value))

;; NUMBER
(defmethod ^:private transform :number [[_ value]]
  (transformer.number/->spec value))

;; STRING
(defmethod ^:private transform :string [[_ value]]
  (transformer.string/->spec value))

;; KEYWORD
(defmethod ^:private transform :keyword [[_ value]]
  (transformer.keyword/->spec value))

;; REGEX
(defmethod ^:private transform :regex [[_ value]]
  (transformer.regex/->spec value))

;; WHITESPACE
(defmethod ^:private transform :whitespace [[_ _]] nil)

;; HASHMAP
(defmethod ^:private transform :map [[_ & values]]
  (let [result (->> values
                    (filter (comp #(not= :whitespace %) first))
                    (map transform)
                    (partition 2)
                    (map utils/build-hashmap-function))
        and-spec (utils/apply-on-macro `s/and result)]
    `(s/spec ~and-spec)))


(defn ->spec [pattern]
  (let [parsed-code (-> pattern
                        (parcera/ast :unhide :literals)
                        (nth 1))]
    (-> parsed-code transform str sci/eval-string)))
