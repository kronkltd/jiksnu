(ns jiksnu.modules.admin.actions.client-actions
  (:require [ciste.core :refer [defaction]]
            [jiksnu.actions.client-actions :as actions.client]))

(defaction index
  []
  (actions.client/index))

