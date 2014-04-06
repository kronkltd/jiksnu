(ns jiksnu.modules.core.views.dialback-views
  (:require [ciste.views :refer [defview]]
            [jiksnu.actions.dialback-actions :as actions.dialback]))

(defview #'actions.dialback/confirm :html
  [request activity]
  {:body ""
   :template false})
