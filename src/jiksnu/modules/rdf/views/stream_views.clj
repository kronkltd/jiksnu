(ns jiksnu.modules.rdf.views.stream-views
  (:use [ciste.config :only [config]]
        [ciste.core :only [with-format]]
        [ciste.views :only [apply-view defview]]
        [ciste.sections.default :only [index-section show-section]]
        [clj-stacktrace.repl :only [pst+]]
        jiksnu.actions.stream-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [bind-to with-page pagination-links with-sub-page]]
        [jiksnu.session :only [current-user]])
  (:require [clj-tigase.core :as tigase]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model :as model]
            [jiksnu.namespace :as ns])
  (:import jiksnu.model.Conversation))

(defview #'public-timeline :n3
  [request {:keys [items] :as page}]
  {:body
   (with-format :rdf
     (doall (index-section items page)))
   :template :false})

(defview #'public-timeline :rdf
  [request {:keys [items] :as page}]
  {:body (index-section (log/spy :info items) page)
   :template :false})

(defview #'user-timeline :rdf
  [request [user activities-map]]
  (when user
    {:body (->> (when-let [activities (seq (:items activities-map))]
                  (index-section activities))
                (concat (show-section user))
                doall)
    :template :false}))

(defview #'user-timeline :n3
  [request [user activities-map]]
  (when user
    {:body (->> (when-let [activities (seq (:items activities-map))]
                  (index-section activities))
                (concat (show-section user))
                doall
                (with-format :rdf))
     :template false}))

