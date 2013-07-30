(ns jiksnu.modules.xmpp.views.stream-views
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

(defview #'public-timeline :xmpp
  [request {:keys [items] :as page}]
  (tigase/result-packet request (index-section items page)))

(defview #'user-timeline :xmpp
  [request [user  activities]]
  (tigase/result-packet request (index-section activities)))
