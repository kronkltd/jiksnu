(ns jiksnu.view
  (:use ciste.view
        [jiksnu.config :only (config)]))

(defmethod full-uri :default
  [record & options]
  (str "http://" (-> (config) :domain)
       (apply uri record options)))

