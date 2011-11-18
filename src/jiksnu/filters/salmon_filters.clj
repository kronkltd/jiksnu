(ns jiksnu.filters.salmon-filters
  (:use (ciste [debug :only [spy]]
               [filters :only [deffilter]])
        jiksnu.actions.salmon-actions)
  (:require (jiksnu.model [user :as model.user])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; salmon
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'process :http
  [action request]
  (let [envelope (-> request :body stream->envelope)
        user (-> request :params :id model.user/fetch-by-id)]
   (action user envelope)))

