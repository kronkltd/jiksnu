(ns jiksnu.modules.xmpp.views.domain-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section
                                       show-section]]
        [jiksnu.actions.domain-actions :only [create delete discover find-or-create
                                              index show ping ping-response
                                              ping-error]]
        [jiksnu.ko :only [*dynamic*]])
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            )
  (:import jiksnu.model.Domain
           jiksnu.model.User))

;; ping

(defview #'ping :xmpp
  [request domain]
  (model.domain/ping-request domain))

;; ping-error

(defview #'ping-error :xmpp
  [request _]
  (cm/implement))

;; ping-response

(defview #'ping-response :xmpp
  [request _domain]
  (cm/implement)
  #_{:status 303
     :template false
     :headers {"Location" (named-path "index domains")}})

