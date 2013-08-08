(ns jiksnu.modules.xmpp.filters.domain-filters
  (:use [ciste.filters :only [deffilter]]
        [clojure.core.incubator :only [-?>]]
        jiksnu.actions.domain-actions
        [jiksnu.modules.core.filters :only [parse-page parse-sorting]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain])
  (:import tigase.xml.Element))

;; ping-error

(deffilter #'ping-error :xmpp
  [action request]
  (-> request :from .getDomain
      model.domain/fetch-by-id action))

;; ping-response

(deffilter #'ping-response :xmpp
  [action request]
  (-> request :from .getDomain
      model.domain/fetch-by-id action))

