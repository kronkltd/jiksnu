(ns jiksnu.triggers.user-triggers
  (:use jiksnu.actions.user-actions
        jiksnu.helpers.user-helpers)
  (:require [jiksnu.actions.webfinger-actions :as actions.webfinger]))

(defn discover-onesocialweb
  [action params user]
  
  )

(defn discover-webfinger
  [action _ user]
  (let [xrd (fetch-user-meta user)
        links (actions.webfinger/get-links xrd)
        new-user (assoc user :links links)]
    (update new-user)))
