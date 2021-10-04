(ns shape-shifter.core
  (:require [clojure.edn :as edn]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [parcera.core :as parcera]
            [shape-shifter.utils :as utils]))

(def ^:private wildcards
  {"$fn"      `(s/cat :function #(= 'fn %)
                      :args (s/coll-of symbol? :kind vector?)
                      :body any?)
   "$symbol"  `symbol?
   "$string"  `string?
   "$char"    `char?
   "$keyword" `keyword?
   "$map"     `map?
   "$number"  `number?
   "$list"    `list?
   "$vector"  `vector?
   "$regex"   `s/regex?})

(defmulti ^:private transform (fn [[key _]]  key))

(defn ^:private recursively-transform [coll-of-specs value]
  (let [random-key-name (utils/random-keyword 5)
        transformed-value (transform value)]
    (if transformed-value
      (conj coll-of-specs random-key-name transformed-value)
      coll-of-specs)))

(defn ^:private pattern->collection [patterns kind]
  (->> patterns
       (reduce recursively-transform [])
       (cons `s/cat)
       list
       (concat `(s/and ~kind))
       list
       (cons `s/spec)))

(defmethod ^:private transform :list [[_ & values]]
  (pattern->collection values `list?))

(defmethod ^:private transform :vector [[_ & values]]
  (pattern->collection values `vector?))

(defmethod ^:private transform :number [[_ value]]
  `#{(edn/read-string ~value)})

(defmethod ^:private transform :symbol [[_ value]]
  (or (get wildcards value)
      `#{(symbol ~value)}))

(defmethod ^:private transform :string [[_ value]]
  `#{~(string/replace value #"\"" "")})

(defmethod ^:private transform :regex [[_ value]]
  `#{~(format "#%s" value)})

(defmethod ^:private transform :whitespace [[_ _]] nil)

(defn pattern->spec [pattern]
  (-> pattern
      (parcera/ast :unhide :literals)
      rest
      first
      transform
      eval))

(comment
  (-> "(+ 1 1)" pattern->spec (s/valid? '(+ 1 1))))