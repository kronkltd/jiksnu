(ns jiksnu.views.feed-source-views
  (:use (ciste [config :only [config]]
               [debug :only [spy]]
               [views :only [defview]])
        ciste.sections.default
        jiksnu.actions.feed-source-actions)
  (:require (jiksnu.model [feed-source :as model.feed-source]
                          [user :as model.user])
            (ring.util [response :as response])))

(defview #'process-updates :html
  [request params]
  {:body params
   :template false})

(defview #'remove-subscription :html
  [request params]
  (-> (response/redirect-after-post "/admin/feed-sources")
        (assoc :template false)))
