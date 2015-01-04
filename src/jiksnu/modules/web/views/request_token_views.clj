(ns jiksnu.modules.web.views.request-token-views
  (:require [ciste.sections.default :refer [link-to]]
            [ciste.views :refer [defview]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.actions.request-token-actions :as actions.request-token]
            [jiksnu.modules.web.sections :refer [pagination-links with-page]]
            [ring.util.response :as response]))

(defview #'actions.request-token/authorize :html
  [request token]
  {:body
   [:div
    [:p "Authorization Complete"]
    [:p "Token: " (:_id token)]
    [:p "Verifier: " (:verifier token)]]})

(defview #'actions.request-token/authorize :viewmodel
  [request domain]
  (let [id (:_id domain)]
    {:body
     {:title "Authorization Complete"}}))

(defview #'actions.request-token/show-authorization-form :html
  [request [user token]]
  {:body
   [:div
    [:p "Authorize"]
    [:p "You are logged in as "]
    [:div {:data-bind "with: currentUser"}
     [:div {:data-model "user"}
      (link-to user)]]
    [:form {:method "post"
            ;; :action "authorize"
            }
     [:input {:type "hidden" :name "oauth_token" :value (:_id token)}]
     [:input {:type "hidden" :name "verifier" :value (:verifier token)}]
     [:button.btn "Allow"]]]})

(defview #'actions.request-token/show-authorization-form :viewmodel
  [request domain]
  (let [id (:_id domain)]
    {:body
     {:title "Show Authorization Form"}}))

(defview #'actions.request-token/get-request-token :text
  [request response]
  {:body
   (->> [["oauth_token"        (:_id response)]
         ["oauth_token_secret" (:secret response)]
         ["oauth_callback_confirmed" "true"]]
        (map #(string/join "=" %))
        (string/join "&"))})
