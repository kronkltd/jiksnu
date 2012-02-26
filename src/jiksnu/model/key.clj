(ns jiksnu.model.key
  (:require (jiksnu.model [user :as model.user]))
  (:import jiksnu.model.MagicKeyPair))

(defn get-user
  [key]
  (model.user/fetch-by-id (:user key))
  )
