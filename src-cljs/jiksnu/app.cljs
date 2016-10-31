(ns jiksnu.app
  (:require [jiksnu.helpers :as helpers]))

(defonce plugins (atom #{}))

(defn configure-raven-plugin
  []
  (when-let [dsn js/sentryDSNClient]
    (-> js/Raven
        (.config dsn)
        (.addPlugin js/Raven.Plugins.Angular)
        (.install))
    (swap! plugins conj "ngRaven")))

(defn initialize-plugins!
  []
  (apply swap! plugins conj helpers/initial-plugins)
  (configure-raven-plugin))

(defn get-plugins
  []
  (clj->js @plugins))

(defn initialize-module!
  []
  (initialize-plugins!)
  (js/angular.module "jiksnu" (get-plugins)))

(initialize-module!)
