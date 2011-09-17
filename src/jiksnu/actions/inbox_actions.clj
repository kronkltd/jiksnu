(ns jiksnu.actions.inbox-actions
  (:use (ciste [core :only (defaction)])
        jiksnu.model)
  (:require (jiksnu.model [item :as model.item])))

(defaction index
  [user]
  (model.item/fetch-activities user))
