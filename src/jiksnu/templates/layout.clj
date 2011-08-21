(ns jiksnu.templates.layout
  (:use (ciste [config :only (*environment*)]
               debug)
        (closure.templates [core :only (deftemplate)])
        (jiksnu [session :only (current-user)]))
  (:require (jiksnu.model [user :as model.user])
            [hiccup.core :as hiccup]))

(deftemplate layout
  [response]
  {:body (hiccup/html (:body response))
   :formats (:formats response)
   :authenticated (if-let [user (current-user)]
                    (model.user/format-data user))
   :development (= *environment* :development)})
