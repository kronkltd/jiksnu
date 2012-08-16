(ns jiksnu.views.feed-source-views
  (:use [ciste.config :only [config]]
        [ciste.views :only [defview]]
        ciste.sections.default
        jiksnu.actions.feed-source-actions)
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]
            [ring.util.response :as response]))

(defview #'fetch-updates :html
  [request params]
  (-> (response/redirect-after-post "/admin/feed-sources")
        (assoc :template false)))

(defview #'process-updates :html
  [request params]
  {:body params
   :template false})

(defview #'remove-subscription :html
  [request params]
  (-> (response/redirect-after-post "/admin/feed-sources")
        (assoc :template false)))
