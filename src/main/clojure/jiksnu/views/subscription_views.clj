(ns jiksnu.views.subscription-views
  (:use (ciste debug
               [html :only (dump dump*)]
               sections
               ciste.views)
        ciste.sections.default
        clj-tigase.core
        jiksnu.actions.subscription-actions
        (jiksnu.helpers subscription-helpers
                        user-helpers)
        (jiksnu model namespace session view))
  (:require [hiccup.form-helpers :as f]
            (jiksnu.model [subscription :as model.subscription]
                          [user :as model.user]))
  (:import jiksnu.model.Subscription
           tigase.xml.Element
           java.text.SimpleDateFormat))

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
  {:body
   [:div
    (dump* request)
    (dump* arg)]})

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
  [request subscribers]
  {:body [:div "subscribers"]})

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
  (result-packet request (subscription-response-element subscription)))

(defview #'subscribed :xmpp
  [request subscription]
  (result-packet request (subscriptions-response [subscription])))

(defview #'subscribers :xmpp
  [request subscribers]
  (result-packet request (subscribers-response subscribers)))

(defview #'subscriptions :xmpp
  [request [user subscriptions]]
  (result-packet request (subscriptions-response subscriptions)))

(defview #'unsubscribe :xmpp
  [request subscription]
  (result-packet request (subscriptions-response [subscription])))
