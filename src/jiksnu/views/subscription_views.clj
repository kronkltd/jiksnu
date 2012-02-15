(ns jiksnu.views.subscription-views
  (:use (ciste [debug :only [spy]]
               [views :only [defview]])
        ciste.sections.default
        jiksnu.actions.subscription-actions)
  (:require (clj-tigase [core :as tigase])
            (jiksnu.helpers [subscription-helpers :as helpers.subscription]
                            [user-helpers :as helpers.user])
            (jiksnu.model [subscription :as model.subscription])
            (jiksnu.sections [subscription-sections :as sections.subscription])))

(defview #'delete :html
  [request _]
  {:status 302
   :template false
   :headers {"Location" "/admin/subscriptions"}})

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

(defview #'subscribe :html
  [request subscription]
  {:status 302
   :template false
   :headers {"Location" "/"}})

(defview #'get-subscribers :html
  [request [user subscribers]]
  {:title "Subscribers"
   :body (sections.subscription/subscribers-index subscribers)})

(defview #'get-subscriptions :html
  [request [user subscriptions]]
  {:title "Subscriptions"
   :formats [{:href (str (uri user) "/subscriptions.atom")
              :label "Atom"
              :type "application/atom+xml"}
             {:href (str (uri user) "/subscriptions.json")
              :label "JSON"
              :type "application/json"}]
   :body (sections.subscription/subscriptions-index subscriptions)})

(defview #'unsubscribe :html
  [request subscription]
  {:status 302
   :template false
   :headers {"Location" "/"}})







(defview #'remote-subscribe-confirm :xmpp
  [request _]
  nil)

(defview #'subscribe :xmpp
  [request subscription]
  (tigase/result-packet
   request (helpers.subscription/subscription-response-element subscription)))

(defview #'subscribed :xmpp
  [request subscription]
  (tigase/result-packet
   request (helpers.subscription/subscriptions-response [subscription])))

(defview #'get-subscribers :xmpp
  [request subscribers]
  (tigase/result-packet
   request (helpers.subscription/subscribers-response subscribers)))

(defview #'get-subscriptions :xmpp
  [request [user subscriptions]]
  (tigase/result-packet
   request (helpers.subscription/subscriptions-response subscriptions)))

(defview #'unsubscribe :xmpp
  [request subscription]
  {:to (-> subscription model.subscription/get-target tigase/make-jid)
   :from (-> subscription model.subscription/get-actor tigase/make-jid)
   :type :result
   :body (helpers.subscription/subscriptions-response [subscription])
   :id (:id request)})
