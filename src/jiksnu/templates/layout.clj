(ns jiksnu.templates.layout
  (:use (ciste [config :only [*environment*]]
               [debug :only [spy]])
        (closure.templates [core :only [deftemplate]])
        (jiksnu [session :only [current-user]]))
  (:require (jiksnu.actions [subscription-actions :as actions.subscription]
                            [user-actions :as actions.user])
            (jiksnu.model [subscription :as model.subscription]
                          [user :as model.user])
            [hiccup.core :as hiccup]))

(deftemplate layout
  [response]
  (merge
   {:body (hiccup/html (:body response))
    :flash (:flash response)
    :formats (:formats response)
    :development (= (spy @*environment*) :development)}
   (if-let [user (current-user)]
     {:authenticated (model.user/format-data user)
      :subscriptions (->> user actions.subscription/subscriptions
                          second (map model.subscription/format-data) spy)
      :subscribers (->> user actions.subscription/subscribers
                        second (map model.subscription/format-data) spy)})))
