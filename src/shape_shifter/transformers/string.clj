(ns shape-shifter.transformers.string
  (:require
   [clojure.string :as string]))

;; since the string came from the parser as "\"string here\""
;; it is necessary to remove the additional double quotes and the backslash
(defn ->spec [value]
  (-> value
      (string/replace #"\"" "")
      vector
      set))

(comment
  (->spec "\"banana\""))
