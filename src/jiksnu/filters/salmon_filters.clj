(ns jiksnu.filters.salmon-filters
  (:use [ciste.debug :only [spy]]
        [ciste.filters :only [deffilter]]
        jiksnu.actions.salmon-actions)
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; salmon
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'process :http
  [action request]
  (let [envelope (-> request :body stream->envelope)
        user (-> request :params :id model.user/fetch-by-id)]
    (try (action user envelope)
         (catch RuntimeException ex
           (log/error (.getMessage ex))))))

