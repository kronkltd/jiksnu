(ns jiksnu.views.subscription-views
  (:use (ciste [debug :only (spy)]
               sections
               [views :only (defview)])
        ciste.sections.default
        jiksnu.actions.subscription-actions
        (jiksnu model session view))
  (:require (clj-tigase [core :as tigase])
            (jiksnu.helpers [subscription-helpers :as helpers.subscription]
                            [user-helpers :as helpers.user])
            (jiksnu.templates [subscriptions :as templates.subscriptions])))

(defview #'delete :html
  [request _]
  {:status 302
   :template false
   :headers {"Location" "/admin/subscriptions"}})

(defview #'index :html
  [request subscriptions]
  {:body (templates.subscriptions/index-section subscriptions)})

(defview #'ostatus :html
  [request arg]
  {:body ""
   :template false})

(defview #'ostatussub :html
  [request arg]
  {:body (templates.subscriptions/ostatus-sub)})

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

(defview #'subscribers :html
  [request [user subscribers]]
  {:body (templates.subscriptions/subscribers-index subscribers)})

(defview #'subscriptions :html
  [request [user subscriptions]]
  {:title "Subscriptions"
   :formats [{:href (str (uri user) "/subscriptions.json")
              :label "JSON"
              :type "application/json"}]
   :body
   (templates.subscriptions/subscriptions-index subscriptions)})

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

(defview #'subscribers :xmpp
  [request subscribers]
  (tigase/result-packet
   request (helpers.subscription/subscribers-response subscribers)))

(defview #'subscriptions :xmpp
  [request [user subscriptions]]
  (tigase/result-packet
   request (helpers.subscription/subscriptions-response subscriptions)))

(defview #'unsubscribe :xmpp
  [request subscription]
  (tigase/result-packet
   request (helpers.subscription/subscriptions-response [subscription])))
