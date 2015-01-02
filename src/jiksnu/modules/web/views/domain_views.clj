(ns jiksnu.modules.web.views.domain-views
  (:require [ciste.views :refer [defview]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.modules.web.sections :refer [redirect]]))

(defview #'actions.domain/create :html
  [request domain]
  (redirect "/main/domains"
            "Domain has been created"))

(defview #'actions.domain/delete :html
  [request domain]
  (redirect "/main/domains"
            "Domain has been deleted"))

(defview #'actions.domain/discover :html
  [request domain]
  (redirect "/main/domains"
            "Discovering domain"))

;; TODO: is this actually ever called as a route?
(defview #'actions.domain/find-or-create :html
  [request domain]
  (redirect "/main/domains"))
