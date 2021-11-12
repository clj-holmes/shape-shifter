(ns shape-shifter.core
  (:gen-class)
  (:require [clojure.edn :as edn]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [parcera.core :as parcera]
            [sci.core :as sci]
            [sci.impl.vars :as vars]
            [shape-shifter.utils :as utils]))

(def sns (vars/->SciNamespace 'clojure.spec.alpha nil))

(def spec-namespace
  {'def (sci/copy-var s/def sns)
   'valid? (sci/copy-var s/valid? sns)
   'gen (sci/copy-var s/gen sns)
   'cat (sci/copy-var s/cat sns)
   'cat-impl (sci/copy-var s/cat-impl sns)
   'and (sci/copy-var s/and sns)
   'and-spec-impl (sci/copy-var s/and-spec-impl sns)
   '* (sci/copy-var s/* sns)
   'rep-impl (sci/copy-var s/rep-impl sns)
   'or (sci/copy-var s/or sns)
   'coll-of (sci/copy-var s/coll-of sns)
   'regex? (sci/copy-var s/regex? sns)
   'spec (sci/copy-var s/spec sns)
   'spec-impl (sci/copy-var s/spec-impl sns)})

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

(defn build-hash-map-fn [[key value]]
  (let [function-name (->> key first name (format "check-key-%s-value") symbol)]
    `(fn ~function-name [x#]
       (s/valid? ~value (get-in x# ~key)))))

(defmethod ^:private transform :map [[_ & values]]
  (->> values
       (filter (comp #(not= :whitespace %) first))
       (map transform)
       (partition 2)
       (map build-hash-map-fn)
       (cons `s/and)
       list
       (cons `s/spec)))

(defmethod ^:private transform :whitespace [[_ _]] nil)

(defn pattern->spec
  [pattern]
  (let [ast (parcera/ast pattern :unhide :literals)]
    (-> ast rest first transform str (sci/eval-string {:namespaces {'clojure.spec.alpha spec-namespace}}))))

(comment
  (-> "[$keyword& $string&]"
      pattern->spec
      (s/valid? [:jose :maria "banana" "uva"])))