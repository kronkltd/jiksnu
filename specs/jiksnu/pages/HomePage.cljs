(ns jiksnu.pages.HomePage
  (:require [taoensso.timbre :as timbre]))

(defn HomePage
  [])

(set! (.. HomePage -prototype -get)
      #(.get js/browser "/"))

(set! (.. HomePage -prototype -waitForLoaded)
      (fn []
        (.wait
         js/browser
         (fn []
           (timbre/info "Waiting for loaded")
           true))))
