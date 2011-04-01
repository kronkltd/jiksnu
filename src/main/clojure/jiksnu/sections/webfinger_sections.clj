(ns jiksnu.sections.webfinger-sections
  (:use ciste.config))

(defn salmon-link
  [user]
  (str
   "http://"
   (:domain (config))
   "/main/salmon/user/"
   (:_id user)))

