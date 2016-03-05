(ns jiksnu.core-spec
  (:require [cljs.nodejs :as nodejs])
  (:use-macros [jiksnu.step-helpers :only [step-definitions Given]]))

(def chai (nodejs/require "chai"))
(def chai-as-promised (nodejs/require "chai-as-promised"))
(.use chai chai-as-promised)
(def expect (.expect chai))
(def browser js/browser)
(nodejs/enable-util-print!)
(def -main (fn [] nil))
(set! *main-cli-fn* -main) ;; this is required

(def base-domain "localhost")
(def base-port 8080)
(def base-path (str "http://" base-domain ":" base-port))

(step-definitions

 (Given #"^I am logged in$"
   [callback]
   (println browser)

   (let [page (.get js/browser (str base-path "/"))]
     (-> (expect (.getTitle browser))
         (.toBe "Jiksnu")))

   (.pending callback)))
