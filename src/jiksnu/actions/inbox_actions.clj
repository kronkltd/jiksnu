(ns jiksnu.actions.inbox-actions
  (:require [jiksnu.model.item :as model.item]))

(defn index
  [user]
  #_(model.item/fetch-activities user))
