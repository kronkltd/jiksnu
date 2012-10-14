(ns jiksnu.views.subscription-views
  (:use [ciste.views :only [defview]]
        ciste.sections.default
        jiksnu.actions.subscription-actions)
  (:require [clj-tigase.core :as tigase]
            [clojure.tools.logging :as log]
            [jiksnu.helpers.subscription-helpers :as helpers.subscription]
            [jiksnu.helpers.user-helpers :as helpers.user]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.sections.subscription-sections :as sections.subscription]))

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


(defview #'get-subscribers :html
  [request [user {:keys [items] :as response}]]
  {:title "Subscribers"
   :body (sections.subscription/subscribers-section items response)})

(defview #'get-subscribers :viewmodel
  [request [user {:keys [items] :as page}]]
  {:body {:user (show-section user)
          :subscriptions (index-section items page)}})

(defview #'get-subscribers :xmpp
  [request [user {:keys [items] :as response}]]
  (tigase/result-packet
   request (helpers.subscription/subscribers-response items response)))


(defview #'get-subscriptions :as
  [request [user {:keys [items] :as response}]]
  {:template false
   :body {:items (index-section items response)}})

(defview #'get-subscriptions :html
  [request [user {:keys [items] :as response}]]
  {:title "Subscriptions"
   :formats (subscription-formats user)
   :body [:div
          (when (seq items)
            (sections.subscription/subscriptions-section items (log/spy response)))]})

(defview #'get-subscriptions :json
  [request [user {:keys [items] :as response}]]
  {:body (sections.subscription/subscriptions-section items response)})

(defview #'get-subscriptions :viewmodel
  [request [user {:keys [items] :as page}]]
  {:body {:user (show-section user)
          :subscriptions (index-section items page)}})

(defview #'get-subscriptions :xmpp
  [request [user {:keys [items] :as response}]]
  (tigase/result-packet
   request (helpers.subscription/subscriptions-response items response)))


(defview #'ostatus :html
  [request arg]
  {:body ""
   :template false})


(defview #'ostatussub :html
  [request arg]
  {:body (sections.subscription/ostatus-sub-form)})


(defview #'ostatussub-submit :html
  [request subscription]
  {:status 303
   :headers {"Location" "/"}
   :flash "The request has been sent"
   :template false})


(defview #'remote-subscribe-confirm :xmpp
  [request _]
  nil)


(defview #'subscribe :html
  [request subscription]
  {:status 302
   :template false
   :headers {"Location" "/"}})

(defview #'subscribe :xmpp
  [request subscription]
  (tigase/result-packet
   request (helpers.subscription/subscription-response-element subscription)))


(defview #'subscribed :xmpp
  [request subscription]
  (tigase/result-packet
   request (helpers.subscription/subscriptions-response [subscription])))



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
