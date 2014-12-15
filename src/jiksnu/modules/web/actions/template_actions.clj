(ns jiksnu.modules.web.actions.template-actions
  (:require [ciste.filters :refer [deffilter]]
            [ciste.sections.default :refer [index-section show-section]]
            [ciste.views :refer [defview]]
            )
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation
           jiksnu.model.User))

(defn public-timeline
  []
  {}
  )

(deffilter #'public-timeline :http
  [action request]
  (action))

(defview #'public-timeline :html
  [_ _]
  {:template false
   :body
   (index-section [(Conversation.)] {})}
  )

(defn show-activity
  []
  {}
  )

(deffilter #'show-activity :http
  [action request]
  (action))

(defview #'show-activity :html
  [_ _]
  {:template false
   :body
   (show-section (Activity.) {})}
  )

(defn index-users
  []
  {}
  )

(deffilter #'index-users :http
  [action request]
  (action))

(defview #'index-users :html
  [_ _]
  {:template false
   :body
   (index-section [(User.)] {})}
  )
