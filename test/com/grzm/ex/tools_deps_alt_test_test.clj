(ns com.grzm.ex.tools-deps-alt-test-test
  (:require [clojure.test :refer :all]
            [com.grzm.ex.tools-deps-alt-test :refer :all]))

(defn require? [sym]
  (try
    (require sym)
    true
    (catch Exception _
      false)))

(deftest test-requires
  (testing ":dependencies"
    (is (require? 'com.stuartsierra.dependency)))
  (testing ":deps"
    (is (require? 'clojure.java.jdbc)))
  (testing ":extra-deps"
    (is (require? 'clojure.math.combinatorics))))
