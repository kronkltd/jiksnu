(ns jiksnu.modules.json.sections.user-sections
  (:use  [ciste.sections :only [defsection]]
         [ciste.sections.default :only [title uri show-section edit-button index-line
                                        show-section-minimal update-button index-block
                                        index-section]]
         [clojure.core.incubator :only [-?>]]
         [inflections.core :only [camelize]]
         [jiksnu.ko :only [*dynamic*]]
         [jiksnu.modules.core.sections :only [admin-index-block admin-index-line admin-index-section
                                              admin-show-section]]
         [jiksnu.modules.web.sections :only [dropdown-menu pagination-links]]
         [jiksnu.session :only [current-user is-admin?]]
         [slingshot.slingshot :only [try+]])
  (:require [ciste.core :refer [with-format]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [hiccup.form :as f]
            [jiksnu.namespace :as ns]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.key :as model.key]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [lamina.trace :as trace]
       )
  (:import jiksnu.model.Domain
           jiksnu.model.FeedSource
           jiksnu.model.Key
           jiksnu.model.User))

(defsection show-section [User :twitter]
  [item & _]
  {:name (:name item)
   :id (:_id item)
   :screen_name (:username item)
   :url (:id item)
   :profile_image_url (:avatarUrl item)
   :protected false})

(defsection show-section [User :json]
  [item & _]
  (with-format :model (log/spy :info (show-section item))))
