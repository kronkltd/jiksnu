(ns jiksnu.app.loader
  (:require [jiksnu.registry :as registry]))

(defn configure-raven-plugin
  [plugins]
  (if-let [dsn (some-> js/window .-sentryDSNClient)]
    (do (-> js/Raven
         (.config dsn)
         (.addPlugin js/Raven.Plugins.Angular)
         (.install))
        (conj plugins "ngRaven"))
    plugins))

(defn initialize-plugins!
  []
  (-> registry/initial-plugins
      configure-raven-plugin))

(defn initialize-module!
  []
  (let [plugins (clj->js (initialize-plugins!))]
    (js/angular.module "jiksnu" plugins)))
