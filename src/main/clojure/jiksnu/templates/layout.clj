(ns jiksnu.templates.layout
  (:use (ciste [config :only (*environment*)]
               [debug :only (spy)])
        closure.templates.core
        (jiksnu [session :only (current-user)]))
  (:require [jiksnu.templates.user :as templates.user]
            [hiccup.core :as hiccup]))

(deftemplate layout
  [response]
  {:body (hiccup/html (:body response))
   :authenticated (spy (if-let [user (current-user)]
                     (templates.user/format-data user)))
   :development (= *environment* :development)})
