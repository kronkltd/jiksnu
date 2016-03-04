(ns jiksnu.core-spec
  (:require [cljs.nodejs :as nodejs])
  (:use-macros [jiksnu.step-helpers :only [step-definitions Given When Then And]]))

(def chai (nodejs/require "chai"))
(def chai-as-promised (nodejs/require "chai-as-promised"))
(.use chai chai-as-promised)
(nodejs/enable-util-print!)
(def -main (fn [] nil))
(set! *main-cli-fn* -main) ;; this is required

(def base-domain "localhost")
(def base-port 8080)
(def base-path (str "http://" base-domain ":" base-port))

(def expect (.-expect chai))
(def browser js/browser)
(def element js/element)
(def by js/by)

(defn by-model
  [model-name]
  (element (.model by model-name)))

(step-definitions

 (this-as this (.setDefaultTimeout this (* 60 1000)))

 (Given #"^I am not logged in$"
   [next]
   (let [page (.get browser "/main/login")
         form (by-model "username")]
     (.wait browser (fn [] (.isPresent (by-model "username"))))
     (-> (expect (.getInnerHtml form))
         .-to .-eventually .-exist)

     (-> (expect (.getTitle browser))
         .-to .-eventually (.equal "Jiksnu")
         .-and (.notify next))
     nil)))
