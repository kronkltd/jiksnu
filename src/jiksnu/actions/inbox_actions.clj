(ns jiksnu.actions.inbox-actions
  (:require [ciste.core :refer [defaction]]
            [jiksnu.model.item :as model.item]))

(defaction index
  [user]
  #_(model.item/fetch-activities user))
