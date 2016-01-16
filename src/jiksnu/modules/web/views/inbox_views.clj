(ns jiksnu.modules.web.views.inbox-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-block]]
            [jiksnu.actions.inbox-actions :as actions.inbox]))

(defview #'actions.inbox/index :html
  [request activities]
  {:body (index-block activities)})
