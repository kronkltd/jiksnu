(ns jiksnu.modules.xmpp.views.activity-views
  (:use [ciste.config :only [config]]
        [ciste.views :only [defview]]
        ciste.sections.default
        [clojurewerkz.route-one.core :only [named-path]]
        jiksnu.actions.activity-actions
        [jiksnu.ko :only [*dynamic*]]
        )
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]
            [jiksnu.modules.xmpp.element :as xmpp.element]
            [ring.util.response :as response])
  (:import jiksnu.model.Activity))

;; remote-create

(defview #'remote-create :xmpp
  [request _]
  nil)

