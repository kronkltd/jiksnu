(ns jiksnu.actions.inbox-actions
  (:use [ciste.core :only [defaction]])
  (:require [jiksnu.model.item :as model.item]))

(defaction index
  [user]
  #_(model.item/fetch-activities user))
