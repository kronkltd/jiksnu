(ns jiksnu.modules.web.filters.salmon-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.salmon-actions :as actions.salmon]
            [jiksnu.model.user :as model.user]
            [lamina.trace :as trace]
            [slingshot.slingshot :refer [try+]]))

(deffilter #'actions.salmon/process :http
  [action request]
  (let [envelope (-> request :body actions.salmon/stream->envelope)
        user (-> request :params :id model.user/fetch-by-id)]
    (try+ (action user envelope)
          (catch RuntimeException ex
            (trace/trace "errors:handled" ex)))))

