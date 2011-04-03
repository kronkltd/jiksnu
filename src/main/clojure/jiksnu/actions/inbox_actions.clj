(ns jiksnu.actions.inbox-actions
  (:use jiksnu.model)
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.item :as model.item]
            [jiksnu.model.user :as model.user] )
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(defn index
  [request]
  (let [user (model.user/show ((:params request) "username"))]
    (model.item/fetch-activities user)))
