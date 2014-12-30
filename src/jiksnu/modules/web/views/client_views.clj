(ns jiksnu.module.web.views.client-views
  (:require [ciste.config :refer [config]]
            [ciste.views :refer [defview]]
            [clj-time.coerce :as coerce]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.namespace :as ns]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.web.sections.user-sections :as sections.user]))

(defview #'actions.client/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))
