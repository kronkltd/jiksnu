(ns jiksnu.views.feed-source-views
  (:use [ciste.config :only [config]]
        [ciste.debug :only [spy]]
        [ciste.views :only [defview]]
        ciste.sections.default
        jiksnu.actions.feed-source-actions)
  (:require [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]
            [ring.util.response :as response]))

(defview #'process-updates :html
  [request params]
  {:body params
   :template false})

(defview #'remove-subscription :html
  [request params]
  (-> (response/redirect-after-post "/admin/feed-sources")
        (assoc :template false)))
