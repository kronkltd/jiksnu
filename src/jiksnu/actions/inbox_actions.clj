(ns jiksnu.actions.inbox-actions
  (:require [ciste.core :refer [defaction]]
            [ciste.model :as cm]
            [jiksnu.model.item :as model.item]))

(defaction index
  [user]
  (cm/implement)
  #_(model.item/fetch-activities user))
