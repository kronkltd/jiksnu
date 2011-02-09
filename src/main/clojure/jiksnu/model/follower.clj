(ns jiksnu.model.follower
  (:use jiksnu.model)
  (:require [karras.entity :as entity]
            [jiksnu.model.subscription :as subscription.model]))

(defn index
  [user & options]
  (subscription.model/index :to (:_id user)))
