(ns jiksnu.modules.as.views.subscription-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section show-section uri]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.subscription-actions :as actions.subscription]))

(defview #'actions.subscription/get-subscriptions :as
  [request [user {:keys [items] :as response}]]
  {:template false
   :body {:items (index-section items response)}})


(defview #'actions.subscription/get-subscribers :as
  [request [user {:keys [items] :as response}]]
  {:template false
   :body {:items (index-section items response)}})


