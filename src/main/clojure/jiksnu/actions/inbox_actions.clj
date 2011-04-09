(ns jiksnu.actions.inbox-actions
  (:use ciste.core
        jiksnu.model)
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.item :as model.item])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(defaction index
  [user]
  (model.item/fetch-activities user))
