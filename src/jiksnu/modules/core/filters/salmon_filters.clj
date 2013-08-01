(ns jiksnu.filters.salmon-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.salmon-actions
        [slingshot.slingshot :only [try+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user]
            [lamina.trace :as trace]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; salmon
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'process :http
  [action request]
  (let [envelope (-> request :body stream->envelope)
        user (-> request :params :id model.user/fetch-by-id)]
    (try+ (action user envelope)
          (catch RuntimeException ex
            (trace/trace "errors:handled" ex)))))

