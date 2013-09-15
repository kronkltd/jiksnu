(ns jiksnu.transforms.client-transforms
  (:require [jiksnu.util :as util]))

(defn set-_id
  [params]
  (if (:_id params)
    params
    (assoc params :_id (util/generate-token))))

(defn set-type
  [params]
  (if (:type params)
    (if (#{"web" "native"} type)
      params
      (throw+ "invalid type")
      )
    (assoc params :type "web")))

(defn set-secret
  [params]
  (if (:secret params)
    params
    (assoc params :secret (util/generate-token 32))))

(defn set-expiry
  [params]
  (if (:secret-expires params)
    params
    (assoc params :secret-expires 0)))

(defn set-webfinger
  [params]
  (if (or (:host params)
          (:webfinger params))
    params
    (throw+ "Client must have a host or a webfinger")
    )
  )
