(ns jiksnu.views.stream-views
  (:use [ciste.config :only [config]]
        [ciste.core :only [with-format]]
        [ciste.views :only [apply-view defview]]
        ciste.sections.default
        [clj-stacktrace.repl :only [pst+]]
        jiksnu.actions.stream-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [bind-to format-page-info with-page pagination-links with-sub-page]]
        [jiksnu.session :only [current-user]])
  (:require [clj-tigase.core :as tigase]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model :as model]
            [jiksnu.namespace :as ns]
            [jiksnu.sections.activity-sections :as sections.activity])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation))

(defview #'public-timeline :as
  [request {:keys [items] :as page}]
  {:body
   ;; TODO: I know that doesn't actually work.
   ;; TODO: assign the generator in the formatter
   {:generator "Jiksnu ${VERSION}"
    :title "Public Timeline"
    :totalItems (:totalRecords page)
    :items
    (let [activity-page (actions.activity/fetch-by-conversations
                         (map :_id items))]
      (index-section (:items activity-page) activity-page))}})

(defview #'user-timeline :as
  [request [user {:keys [items] :as page}]]
  {:body
   {:title (str (title user) " Timeline")
    :items
    (index-section items page)}})

