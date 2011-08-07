(ns jiksnu.templates.domain
  (:use ciste.debug
        closure.templates.core))

(deftemplate link-to
  [domain]
  (:id (:_id domain)))
