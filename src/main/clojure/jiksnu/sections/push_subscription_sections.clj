(ns jiksnu.sections.push-subscription-sections
  (:use ciste.debug
        ciste.view
        ciste.html
        ciste.sections
        jiksnu.abdera
        jiksnu.model
        jiksnu.helpers.push-subscription-helpers
        jiksnu.helpers.user-helpers
        jiksnu.namespace
        jiksnu.sections.user-sections
        jiksnu.session
        jiksnu.xmpp.element
        jiksnu.view
        [karras.entity :only (make)])
  (:require [hiccup.form-helpers :as f]
            [jiksnu.model.push-subscription :as model.push-subscription]
            [jiksnu.model.user :as model.user])
  (:import javax.xml.namespace.QName
           jiksnu.model.PushSubscription
           tigase.xml.Element))

(defsection index-block [PushSubscription :html]
  [subscriptions & _]
  [:table
   (map index-line subscriptions)])

(defsection index-line [PushSubscription :html]
  [subscription & _]
  [:tr
   [:td "foo"]])

(defsection index-section [PushSubscription :html]
  [subscriptions & _]
  [:div
   [:h3 "Subscriptions"]
   (index-block subscriptions)])
