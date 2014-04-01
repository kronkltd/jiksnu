(ns jiksnu.modules.xmpp.views.activity-views
  (:use [ciste.views :only [defview]]
        jiksnu.actions.activity-actions
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
            [ring.util.response :as response]))

;; remote-create

(defview #'remote-create :xmpp
  [request _]
  nil)

