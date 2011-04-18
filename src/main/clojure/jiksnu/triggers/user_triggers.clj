(ns jiksnu.triggers.user-triggers
  (:use ciste.triggers
        jiksnu.actions.user-actions
        jiksnu.helpers.user-helpers)
  (:require [jiksnu.actions.webfinger-actions :as actions.webfinger]
            [jiksnu.model.domain :as model.domain]))

(defn discover-user
  [action _ user]
  (let [domain (model.domain/show (:domain user))]
    (if (:xmpp domain)
      (request-vcard! user)
      (update-usermeta user))))

(add-trigger! #'discover #'discover-user)
