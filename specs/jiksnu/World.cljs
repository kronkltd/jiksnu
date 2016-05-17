(ns jiksnu.World
  (:require [cljs.nodejs :as nodejs]
            [jiksnu.PageObjectMap :refer [pages]]))

(def chai (nodejs/require "chai"))
(def chai-as-promised (nodejs/require "chai-as-promised"))
(def util (nodejs/require "util"))
(.use chai chai-as-promised)
(nodejs/enable-util-print!)
(def -main (fn [] nil))
(set! *main-cli-fn* -main) ;; this is required

(def base-domain "jiksnu-integration.docker")
(def base-port 80)
(def base-path (str "http://" base-domain ":" base-port))

(def element js/element)
(def expect (.-expect chai))
(def browser js/browser)
(def protractor js/protractor)

(let [World (.-World (nodejs/require "cukefarm"))]
  (set! (.-pageObjectMap (.-prototype World)) pages)
  (set! (.-World (.-exports js/module)) World))
