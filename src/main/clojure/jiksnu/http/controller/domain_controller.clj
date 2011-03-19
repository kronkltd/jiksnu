(ns jiksnu.http.controller.domain-controller
  (:require [jiksnu.model.domain :as model.domain])
  )

(defn create
  [request]
  (let [{{domain "domain"} :params} request]
    (model.domain/create {:_id domain})
    )
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


(defn discover
  [request]
  (let [{{id "id"} :params} request]
    (let [domain (model.domain/show id)]
      (let [xrd (fetch (str "http://" id "/.well-known/host-meta"))]
        (let [links (get-links xrd)]
          (model.domain/update (assoc domain :links links))
          )
        ))

    )
  )
