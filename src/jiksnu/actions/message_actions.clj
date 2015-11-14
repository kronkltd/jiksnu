(ns jiksnu.actions.message-actions
  (:require [ciste.core :refer [defaction]]))

(defaction inbox-page
  [user]
  [user []])

(defaction outbox-page
  [user]
  [user []])
