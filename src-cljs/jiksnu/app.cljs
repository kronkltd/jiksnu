(ns jiksnu.app
  (:require [jiksnu.registry :as registry]))

(defonce plugins (atom #{}))
(defonce models  (atom {}))

(defn configure-raven-plugin
  []
  (when-let [dsn (some-> js/window .-sentryDSNClient)]
    (-> js/Raven
        (.config dsn)
        (.addPlugin js/Raven.Plugins.Angular)
        (.install))
    (swap! plugins conj "ngRaven")))

(defn initialize-plugins!
  []
  (apply swap! plugins conj registry/initial-plugins)
  (configure-raven-plugin))

(defn get-plugins
  []
  (clj->js @plugins))

(defn initialize-module!
  []
  (initialize-plugins!)
  (js/angular.module "jiksnu" (get-plugins)))

(defonce jiksnu (initialize-module!))
