(ns jiksnu.transforms.request-token-transforms
  (:require [jiksnu.model.request-token :as model.request-token])
  )

(defn set-token
  [params]
  (if (:token params)
    params
    (let [token (model.request-token/generate-token)]
      (assoc params :token token)
      )
    )
  )
