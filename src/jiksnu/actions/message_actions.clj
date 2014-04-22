(ns jiksnu.actions.message-actions
  (:use [ciste.core :only [defaction]])
  (:require [ciste.model :as cm]))

(defaction inbox-page
  [user]
  (cm/implement
      [user []]))

(defaction outbox-page
  [user]
  (cm/implement
      [user []]))
