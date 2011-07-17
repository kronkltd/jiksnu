(ns jiksnu.sections.webfinger-sections
  (:use ciste.config))

(defn salmon-link
  [user]
  (str
   "http://"
   (config :domain)
   "/main/salmon/user/"
   (:_id user)))

