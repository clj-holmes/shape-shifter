(ns shape-shifter.transformers.regex
  (:require
   [clojure.string :as string]
   [shape-shifter.config :as config]))

;; since a regex can have double-quotes with backslashes
;; it is necessary to make sure that only the first and last are removed 
;; because the parser adds it.
(defn ^:private remove-additional-backslack-and-double-quotes [value]
  (let [remove-fn (comp #(subs % 1) string/reverse)]
    (->> value
         (iterate remove-fn)
         (take 3)
         last)))

;; returns a function that will apply 
;; the provided regex to the give value
(defn ^:private create-regex-macro-fn [regex]
  `(fn [x#]
     (->> x# str (re-matches ~regex) nil? not)))

;; if interpret-regex? config is true, it will return a function to match the regex
;; if not it will only return the regex as text.
(defn ->spec [value]
  (if (:interpret-regex? config/*config*)
    (->> value
         remove-additional-backslack-and-double-quotes
         re-pattern
         create-regex-macro-fn)
    (->> value
         (format "#%s")
         vector
         set)))

(comment
  (->spec  "\"banana.*\"")

  (binding [config/*config* {:interpret-regex? true}]
    (->spec  "\"banana.*\"")))
