(ns jiksnu.core-spec
  (:require purnam.test)
  (:use-macros [purnam.test :only [beforeEach describe it is is-not]]))

(def base-domain "localhost")
(def base-port 8080)
(def base-path (str "http://" base-domain ":" base-port))

(js/describe "foo"
  (fn []
    (js/it "FIX THIS: One Plus One Equals... "
      (fn []
        (let [page (.get js/browser (str base-path "/"))]
          (-> (js/expect (.getTitle js/browser))
              (.toBe "Jiksnu"))
          (is (+ 1 1) 2)
          (is (+ 2 2) 4))))))
