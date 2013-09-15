(ns jiksnu.transforms.request-token-transforms
  (:require [jiksnu.util :as util])
  )

(defn set-token
  [params]
  (if (:token params)
    params
    (let [token (util/generate-token)]
      (assoc params :token token)
      )
    )
  )

(defn set-authenticated
  [params]
  ;; TODO: needs to test for key presence, but this is okay
  (if (:authenticated params)
    params
    (assoc params :authenticated false)))

(defn set-used
  [params]
  ;; TODO: needs to test for key presence, but this is okay
  (if (:used params)
    params
    (assoc params :used false)))
