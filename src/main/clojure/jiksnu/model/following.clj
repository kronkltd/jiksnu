(ns jiksnu.model.following
  (:use jiksnu.model)
  (:require [karras.entity :as entity]
            [jiksnu.model.subscription :as subscription.model]))

(defn index
  [& options]
  (subscription.model/index :from nil))
