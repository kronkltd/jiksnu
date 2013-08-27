(ns jiksnu.modules.admin.actions.request-token-actions
  (:require [ciste.core :refer [defaction]]
            [jiksnu.actions.request-token-actions :as actions.request-token]))

(defaction index
  []
  (actions.request-token/index))
