(ns jiksnu.modules.web.transforms.user-transforms
  (:require [clojure.tools.logging :as log]))

(defn salmon-link
  [user]
  #_(named-url "user salmon" {:id (:_id user)}))


(defn set-url
  [user]
  (if (:url user)
    user
    (assoc user :url "")))

