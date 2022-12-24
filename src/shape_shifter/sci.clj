(ns shape-shifter.sci
  (:require
   [sci.core :as sci]))

;; creates a namespace object named clojure.spec.alpha inside sci.
;; copy all public functions from clojure.spec.alpha to our sci-environment.
(def ^:private spec-ns-copy
  (let [ns-object (sci/create-ns 'clojure.spec.alpha)]
    (-> 'clojure.spec.alpha
        ns-publics
        (update-vals #(sci/copy-var* % ns-object)))))

;; receive a string and transform it into code 
;; uses the spec-ns-copy to resolve functions from the original ns
(defn eval-string [string]
  (let [opts {:namespaces {'clojure.spec.alpha spec-ns-copy}}]
    (sci/eval-string string opts)))

(comment
  (eval-string "(clojure.spec.alpha/valid? string? \"banana\")"))
