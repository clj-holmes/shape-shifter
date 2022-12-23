(ns shape-shifter.utils-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :refer :all]
   [shape-shifter.utils :as utils]))

(deftest random-keyword
  (testing "when the random keyword has the exactly expected size"
    (let [spec (s/and keyword?
                      #(-> % name count (= 10)))]
      (is (s/valid? spec (utils/random-keyword 10))))))

(deftest apply-on-macro
  (testing "implement of clojure.core/apply for macros"
    (let [function (eval (utils/apply-on-macro `s/cat [:item string?]))]
      (s/valid? function ["banana"]))))
