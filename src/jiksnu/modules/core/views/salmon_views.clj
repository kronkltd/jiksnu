(ns jiksnu.modules.core.views.salmon-views
  (:use [ciste.views :only [defview]]
        [jiksnu.actions.salmon-actions :only [process]]))

(defview #'process :html
  [request _]
  {:template false})

