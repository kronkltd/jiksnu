(ns jiksnu.templates.layout
  (:use (ciste [config :only (*environment*)]
               debug)
        closure.templates.core
        (jiksnu [session :only (current-user)]))
  (:require [jiksnu.templates.user :as templates.user]
            [jiksnu.templates.subscriptions :as template.subscriptions]
            [hiccup.core :as hiccup]))

(deftemplate layout
  [response]
  {:body (hiccup/html (:body response))
   :formats (:formats response)
   :authenticated (if-let [user (current-user)]
                    (templates.user/format-data user))
   :development (= *environment* :development)})
