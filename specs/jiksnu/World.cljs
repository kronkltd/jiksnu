(ns jiksnu.World
  (:require [cljs.nodejs :as nodejs]))

(def chai (nodejs/require "chai"))
(def chai-as-promised (nodejs/require "chai-as-promised"))
(def util (nodejs/require "util"))
(.use chai chai-as-promised)
(nodejs/enable-util-print!)
(def -main (fn [] nil))
(set! *main-cli-fn* -main) ;; this is required

(def base-domain "localhost")
(def base-port 8080)
(def base-path (str "http://" base-domain ":" base-port))

(def $ js/$)
(def by js/by)
(def element js/element)
(def expect (.-expect chai))
(def browser js/browser)
(def protractor js/protractor)

(defn by-model
  [model-name]
  (element (.model by model-name)))

(defprotocol Page
  (get [this]))
