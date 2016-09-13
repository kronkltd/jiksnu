(ns jiksnu.transforms.album-transforms
  (:require [jiksnu.session :as session]))

(defn set-owner
  [album]
  (if-let [user (or (:owner album) (session/current-user-id))]
    (assoc album :owner user)))
