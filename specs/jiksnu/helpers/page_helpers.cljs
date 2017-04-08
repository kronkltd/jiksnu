(ns jiksnu.helpers.page-helpers
  (:require [cljs.nodejs :as nodejs]))

(def chai (nodejs/require "chai"))
(def chai-as-promised (nodejs/require "chai-as-promised"))
(def util (nodejs/require "util"))
(.use chai chai-as-promised)
(nodejs/enable-util-print!)
(def -main (fn [] nil))
(set! *main-cli-fn* -main) ;; this is required

(def base-domain "jiksnu-dev")
(def base-port 8080)
(def base-path (str "http://" base-domain (when (not= base-port 80) (str ":" base-port))))

(def element js/element)
(def expect (.-expect chai))
(def browser js/browser)
(def protractor js/protractor)

(defn get-app-data
  "Retrieve the application data"
  []
  (.executeAsyncScript
   js/browser
   (fn [callback]
     (.. js/app fetchStatus
         (then (fn [data]
                 (js/console.log "data" data)
                 (callback (.-data js/app))))))))

(defn get-username
  "Retrieve the logged in username from then app service"
  []
  (js/console.log "get-username")
  (-> (get-app-data)
      (.then (fn [data]
               (let [username (.-user data)]
                 (js/console.log "Username: %s" username)
                 username)))))

(defn seconds [n] (* n 1000))

(defn by-model
  [model-name]
  (js/element (.model js/by model-name)))

(defn by-css
  [selector]
  (.css js/by selector))

(defn current-page
  []
  (.getLocationAbsUrl js/browser))
