(ns jiksnu.modules.web.actions.template-actions
  (:require [ciste.core :refer [with-serialization with-format]]
            [ciste.filters :refer [deffilter]]
            [ciste.sections.default :refer [index-section show-section]]
            [ciste.views :refer [defview]]
            [jiksnu.modules.core.sections :refer [admin-index-section]]
            [jiksnu.modules.web.sections.layout-sections :as sections.layout]
            [hiccup.core :as h])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation
           jiksnu.model.Domain
           jiksnu.model.FeedSource
           jiksnu.model.FeedSubscription
           jiksnu.model.Group
           jiksnu.model.GroupMembership
           jiksnu.model.Resource
           jiksnu.model.User))


(defn index-activities         [] {})
(defn index-conversations      [] {})
(defn index-domains            [] {})
(defn index-feed-sources       [] {})
(defn index-feed-subscriptions [] {})
(defn index-groups             [] {})
(defn index-group-members      [] {})
(defn index-resources          [] {})
(defn index-users              [] {})
(defn left-nav                 [] {})
(defn public-timeline          [] {})
(defn show-activity            [] {})
(defn show-domain              [] {})
(defn show-user                [] {})


(deffilter #'index-activities :http
  [action request]
  (action))

(deffilter #'index-conversations :http
  [action request]
  (action))

(deffilter #'index-domains :http
  [action request]
  (action))

;; (deffilter #'index-feeds :http
;;   [action request]
;;   (action))

(deffilter #'index-feed-sources :http
  [action request]
  (action))

(deffilter #'index-feed-subscriptions :http
  [action request]
  (action))

(deffilter #'index-groups :http
  [action request]
  (action))

(deffilter #'index-group-members :http
  [action request]
  (action))

(deffilter #'index-resources :http
  [action request]
  (action))

(deffilter #'index-users :http
  [action request]
  (action))

(deffilter #'left-nav :http
  [action request]
  (action))

(deffilter #'public-timeline :http
  [action request]
  (action))

(deffilter #'show-activity :http
  [action request]
  (action))

(deffilter #'show-domain :http
  [action request]
  (action))






(defview #'index-activities :html
  [_ _]
  {:template false
   :body
   (index-section [(Activity.)] {})})

(defview #'index-conversations :html
  [_ _]
  {:template false
   :body
   (index-section [(Conversation.)] {})})

(defview #'index-domains :html
  [_ _]
  {:template false
   :body
   (index-section [(Domain.)] {})})


;; (defview #'index-feeds :html
;;   [_ _]
;;   {:template false
;;    :body
;;    (index-section [(Feed.)] {})})

(defview #'index-feed-sources :html
  [_ _]
  {:template false
   :body
   (index-section [(FeedSource.)] {})})

(defview #'index-feed-subscriptions :html
  [_ _]
  {:template false
   :body
   (index-section [(FeedSubscription.)] {})})

(defview #'index-groups :html
  [_ _]
  {:template false
   :body
   (index-section [(Group.)] {})})

(defview #'index-group-members :html
  [_ _]
  {:template false
   :body
   (index-section [(GroupMembership.)] {})})

(defview #'index-resources :html
  [_ _]
  {:template false
   :body
   (index-section [(Resource.)] {})})

(defview #'index-users :html
  [_ _]
  {:template false
   :body
   (index-section [(User.)] {})})

(defview #'left-nav :html
  [_ _]
  {:template false
   :body (sections.layout/side-navigation)})

(defview #'public-timeline :html
  [_ _]
  {:template false
   :body
   (index-section [(Conversation.)] {})})

(defview #'show-activity :html
  [_ _]
  {:template false
   :body
   (show-section (Activity.) {})})

(defview #'show-domain :html
  [_ _]
  {:template false
   :body
   (show-section (Domain.) {})})


(defn left-column
  [_]
  (h/html (sections.layout/left-column-section)))

(defn right-column
  [_]
  (h/html (sections.layout/right-column-section)))

(defn admin-activity
  [_]
  (h/html (admin-index-section [(Activity.)] {})))

(defn admin-conversations
  [_]
  (with-serialization  :http
    (with-format :html
      (h/html (admin-index-section [(Conversation.)] {})))))
