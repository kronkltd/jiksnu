(ns jiksnu.views.feed-source-views
  (:use (ciste [config :only [config]]
               [debug :only [spy]]
               [views :only [defview]])
        ciste.sections.default
        jiksnu.actions.feed-source-actions)
  (:require (jiksnu.model [feed-source :as model.feed-source]
                          [user :as model.user])))

(defview #'process-updates :html
  [request params]
  {:body params
   :template false})

