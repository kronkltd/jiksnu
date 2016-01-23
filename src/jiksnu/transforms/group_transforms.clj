(ns jiksnu.transforms.group-transforms
  (:require [slingshot.slingshot :refer [throw+]]))

(defn set-members
  [params]
  (if (:members params)
    params
    (assoc params :members [])))

(defn set-admins
  [params]
  (if (:admins params)
    params
    (assoc params :admins [])))
