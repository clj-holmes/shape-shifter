(ns shape-shifter.utils-test
  (:require [clojure.test :refer :all])
  (:require [shape-shifter.utils :as utils]
            [clojure.spec.alpha :as s]))

(deftest random-keyword
  (testing "when the random keyword has the exactly expected size"
    (let [spec (s/and keyword?
                      #(-> % name count (= 10)))]
      (is (s/valid? spec (utils/random-keyword 10))))))