(ns jiksnu.view
  (:use ciste.sections
        ciste.view
        [ciste.config :only (config)]))

(defsection full-uri :default
  [record & options]
  (str "http://" (-> (config) :domain)
       (apply uri record options)))

