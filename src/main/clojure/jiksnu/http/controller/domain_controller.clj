(ns jiksnu.http.controller.domain-controller
  (:require [jiksnu.model.domain :as model.domain])
  )

(defn index
  [request]
  (let [domains (model.domain/index)]
    (println "domains: " domains)
    domains
    )
  )

(defn show
  [request]
  (let [{{id "*"} :params} request]
    (println "id: " id)
    (let [domain (model.domain/show id)]
      (println "domain: " domain)
      domain
      )))

(defn check-webfinger
  [domain]
  
  
  )
