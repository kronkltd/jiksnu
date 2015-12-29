(ns jiksnu.transforms.access-token-transforms
  (:require [jiksnu.util :as util]))

(defn set-_id
  [params]
  (if (:_id params)
    params
    (let [token (util/generate-token 16)]
      (assoc params :_id token))))
