(ns jiksnu.core-spec
  (:require [cljs.nodejs :as nodejs]))

(nodejs/enable-util-print!)
(def -main (fn [] nil))
(set! *main-cli-fn* -main) ;; this is required

(def base-domain "localhost")
(def base-port 8080)
(def base-path (str "http://" base-domain ":" base-port))

(set! (.-exports js/module)
      (fn []
        (this-as this
                 (.Given this #"^I am logged in$"
                         (fn [callback]
                           (.pending callback)))

                 (js/console.log this))))
