(ns jiksnu.transforms.client-transforms
  (:require [jiksnu.util :as util])
  )

(defn set-_id
  [params]
  (if (:_id params)
    params
    (assoc params :_id (util/generate-token))
    )
  )
