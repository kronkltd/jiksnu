(ns jiksnu.modules.web.views.feed-source-views
  (:require [ciste.views :refer [defview]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.modules.web.sections :refer [redirect]]))

(defview #'actions.feed-source/process-updates :html
  [request params]
  {:body params
   :template false})

(defview #'actions.feed-source/unsubscribe :html
  [request params]
  (redirect "/main/feed-sources"))

(defview #'actions.feed-source/update :html
  [request params]
  (redirect "/main/feed-sources"))