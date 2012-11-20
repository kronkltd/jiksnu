(ns jiksnu.views.subscription-views
  (:use [ciste.views :only [defview]]
        ciste.sections.default
        jiksnu.actions.subscription-actions
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [bind-to format-page-info with-page pagination-links]])
  (:require [clj-tigase.core :as tigase]
            [clojure.tools.logging :as log]
            [jiksnu.helpers.subscription-helpers :as helpers.subscription]
            [jiksnu.helpers.user-helpers :as helpers.user]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.sections.subscription-sections :as sections.subscription])
  (:import jiksnu.model.Subscription))

(defn subscription-formats
  [user]
  [{:href (str (uri user) "/subscriptions.atom")
    :label "Atom"
    :type "application/atom+xml"}
   {:href (str (uri user) "/subscriptions.as")
    :label "Activity Streams"
    :type "application/atom+xml"}
   {:href (str (uri user) "/subscriptions.json")
    :label "JSON"
    :type "application/json"}])


(defview #'delete :html
  [request _]
  {:status 302
   :template false
   :headers {"Location" "/admin/subscriptions"}})

;; get-subscribers

(defview #'get-subscribers :html
  [request [user {:keys [items] :as page}]]
  {:title "Subscribers"
   :body
   (let [subscriptions (if *dynamic* [(Subscription.)] items)]
     (with-page "default"
       (pagination-links page)
       (bind-to "items"
         (sections.subscription/subscribers-section items page))))})

(defview #'get-subscribers :viewmodel
  [request [user {:keys [items] :as page}]]
  {:body {:user (show-section user)
          :pages {:default (format-page-info page)}}})

(defview #'get-subscribers :xmpp
  [request [user {:keys [items] :as response}]]
  (tigase/result-packet
   request (helpers.subscription/subscribers-response items response)))

;; get-subscriptions

(defview #'get-subscriptions :as
  [request [user {:keys [items] :as response}]]
  {:template false
   :body {:items (index-section items response)}})

(defview #'get-subscriptions :html
  [request [user {:keys [items] :as response}]]
  {:title "Subscriptions"
   :formats (subscription-formats user)
   :body
   (if [items (if *dynamic* [(Subscription.)] items)]
     (with-page "default"
       (pagination-links response)
       (bind-to "items"
         (sections.subscription/subscriptions-section items response))))})

(defview #'get-subscriptions :json
  [request [user {:keys [items] :as response}]]
  {:body (sections.subscription/subscriptions-section items response)})

(defview #'get-subscriptions :viewmodel
  [request [user {:keys [items] :as page}]]
  {:body
   {:targetUser (:_id user)
    :pages {:default (format-page-info page)}}})

(defview #'get-subscriptions :xmpp
  [request [user {:keys [items] :as response}]]
  (tigase/result-packet
   request (helpers.subscription/subscriptions-response items response)))

;; ostatus

(defview #'ostatus :html
  [request arg]
  {:body ""
   :template false})

;; ostatussub

(defview #'ostatussub :html
  [request arg]
  {:body (sections.subscription/ostatus-sub-form)})

(defview #'ostatussub :viewmodel
  [request _]
  {:body
   {:title "Subscribe"}})

;; ostatussub-submit

(defview #'ostatussub-submit :html
  [request subscription]
  {:status 303
   :headers {"Location" "/"}
   :flash "The request has been sent"
   :template false})

;; remote-subscribe-confirm

(defview #'remote-subscribe-confirm :xmpp
  [request _]
  nil)

;; show

(defview #'show :model
  [request item]
  {:body (show-section item)})

;; subscribe

(defview #'subscribe :html
  [request subscription]
  {:status 302
   :template false
   :headers {"Location" "/"}})

(defview #'subscribe :xmpp
  [request subscription]
  (tigase/result-packet
   request (helpers.subscription/subscription-response-element subscription)))

;; subscribed

(defview #'subscribed :xmpp
  [request subscription]
  (tigase/result-packet
   request (helpers.subscription/subscriptions-response [subscription])))

;; unsubscribe

(defview #'unsubscribe :html
  [request subscription]
  {:status 302
   :template false
   :headers {"Location" "/"}})

(defview #'unsubscribe :xmpp
  [request subscription]
  {:to (-> subscription model.subscription/get-target tigase/make-jid)
   :from (-> subscription model.subscription/get-actor tigase/make-jid)
   :type :result
   :body (helpers.subscription/subscriptions-response [subscription])
   :id (:id request)})
