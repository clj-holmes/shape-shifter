(ns shape-shifter.core
  (:require [clojure.edn :as edn]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [parcera.core :as parcera]
            [shape-shifter.utils :as utils]))

(def ^:dynamic *config* {:interpret-regex? false})

(def ^:dynamic *wildcards*
  {"$"              `any?
   "$fn"            `(s/cat :function #(= 'fn %)
                            :args (s/coll-of symbol? :kind vector?)
                            :body any?)
   "$macro-keyword" `qualified-keyword?
   "$symbol"        `symbol?
   "$string"        `string?
   "$set"           `set?
   "$char"          `char?
   "$keyword"       `keyword?
   "$map"           `map?
   "$number"        `number?
   "$list"          `list?
   "$vector"        `vector?
   "$regex"         `s/regex?})

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

(defn ^:private any-number-of-wildcard [symbol]
  (if-let [wildcard (some-> #"\$\w*&"
                            (re-matches symbol)
                            (string/replace "&" ""))]
    `(s/* ~(get *wildcards* wildcard))))

(defn ^:private ->boolean [value]
  (cond
    (= "true" value) `true?
    (= "false" value) `false?))

(defmethod ^:private transform :list [[_ & values]]
  (pattern->collection values `list?))

(defmethod ^:private transform :vector [[_ & values]]
  (pattern->collection values `vector?))

(defmethod ^:private transform :set [[_ & values]]
  (pattern->collection values `set?))

(defmethod ^:private transform :map [[_ & _]]
  nil)

(defmethod ^:private transform :symbol [[_ value]]
  (or (any-number-of-wildcard value)
      (->boolean value)
      (get *wildcards* value)
      `#{(symbol ~value)}))

(defmethod ^:private transform :number [[_ value]]
  `#{(edn/read-string ~value)})

(defmethod ^:private transform :string [[_ value]]
  `#{~(string/replace value #"\"" "")})

(defmethod ^:private transform :keyword [[_ value]]
  `#{~(keyword (string/replace value ":" ""))})

(defmethod ^:private transform :regex [[_ value]]
  (prn value)
  (if (:interpret-regex? *config*)
    (let [regex (-> value
                    (subs 1)
                    (string/reverse)
                    (subs 1)
                    (string/reverse)
                    re-pattern)]
      `(fn [x#]
         (->> x# str (re-matches ~regex) nil? not)))
    `#{~(format "#%s" value)}))

(defmethod ^:private transform :whitespace [[_ _]] nil)

(defn pattern->spec
  [pattern]
  (let [ast (parcera/ast pattern :unhide :literals)]
    (-> ast rest first transform eval)))

(comment
  (-> "[$keyword& $string&]"
      pattern->spec
      (s/valid? [:jose :maria "banana" "uva"])))