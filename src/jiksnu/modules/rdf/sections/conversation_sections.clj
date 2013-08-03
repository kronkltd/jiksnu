(ns jiksnu.modules.rdf.sections.conversation-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [delete-button full-uri uri title index-line
                                       index-block index-line index-section link-to
                                       show-section update-button]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [action-link actions-section admin-index-block admin-index-line
                                bind-to control-line display-property display-timestamp
                                dropdown-menu dump-data pagination-links with-page with-sub-page]])
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.namespace :as ns]
            [jiksnu.modules.rdf.util :as rdf]
            jiksnu.modules.rdf.sections.conversation-sections
            [jiksnu.sections.user-sections :as sections.user]
            [jiksnu.session :as session]
            [plaza.rdf.core :as plaza])
  (:import jiksnu.model.Conversation))

(defsection index-block [Conversation :rdf]
  [items & [response & _]]
  (apply concat (map #(index-line % response) items)))

(defsection show-section [Conversation :rdf]
  [item & [page]]
  (plaza/with-rdf-ns ""
    (let [uri (full-uri item)]
      (rdf/with-subject uri
        [
         [[ns/rdf :type] [ns/sioc "Conversation"]]
         [[ns/dc :updated] (plaza/date (.toDate (:updated item)))]
         ]))))
