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
