(ns cljs.test-test
  (:require
   [cljs.test :refer [deftest is]]
   [clojure.string :as str]
   [sci.configs.cljs.test :as t]
   [sci.core :as sci]))

(def ctx (sci/init {:namespaces {'cljs.test t/cljs-test-namespace}}))

(deftest function?-test
  (is (true? (sci/eval-string* ctx "
(require '[cljs.test :as t])
(t/function? 'inc)"))))

(deftest deftest-test
  (let [output (atom "")]
    (sci/binding [sci/print-fn (fn [s]
                                 (swap! output str s))]
      (sci/eval-string* ctx "
(ns foo)
(require '[cljs.test :as t :refer [deftest is testing]])
(deftest foo
  (is (= 1 1)))
(cljs.test/run-tests 'foo)"))
    (is (str/includes? @output "1 assertions"))
    (is (str/includes? @output "0 failures"))
    (is (str/includes? @output "0 errors"))))

(deftest test-vars-test
  (let [output (atom "")]
    (sci/binding [sci/print-fn (fn [s]
                                 (swap! output str s))]
      (is (= [:each-before :each-after]
             (sci/eval-string* ctx "
(ns foo)
(require '[cljs.test :as t :refer [deftest is testing]])

(def state (atom []))

(t/use-fixtures :each
    {:before
     (fn []
       (swap! state conj :each-before))
     :after
     (fn []
       (swap! state conj :each-after))})

(deftest foo
  (is (= 1 1)))

(t/test-vars [#'foo])

@state"))))))
