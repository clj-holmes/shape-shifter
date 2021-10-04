(ns shape-shifter.core-test
  (:require [clojure.test :refer :all]
            [shape-shifter.core :as core]
            [clojure.spec.alpha :as s]))

(deftest pattern->spec
  (testing "when it is a simple type match"
    (let [spec (core/pattern->spec "1")]
      (is (s/valid? spec 1))))

  (testing "when the pattern is broken"
    (let [spec (core/pattern->spec "(+ 1 1)")]
      (is (s/valid? spec '(+ 1 1)))))

  (testing "when the patterns has a wildcard"
    (let [spec (core/pattern->spec "$number")]
      (is (s/valid? spec 1))))

  (testing "when the pattern has nested lists"
    (let [spec (core/pattern->spec "(+ 1 ($symbol 10 $number))")]
      (is (s/valid? spec '(+ 1 (- 10 1))))))

  (testing "when the pattern has nested vectors"
    (let [spec (core/pattern->spec "[[[1]]]")]
      (is (s/valid? spec [[[1]]]))))

  (testing "when there is a custom wildcard"
    (let [spec (binding [core/*wildcards* (merge core/*wildcards* {"$banana" #{"banana"}})]
                 (core/pattern->spec "$banana"))]
      (is (s/valid? spec "banana")))))