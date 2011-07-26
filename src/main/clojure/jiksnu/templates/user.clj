(ns jiksnu.templates.user
  (:use closure.templates.core))

(deftemplate show-minimal
  [user]
  {:user user})
