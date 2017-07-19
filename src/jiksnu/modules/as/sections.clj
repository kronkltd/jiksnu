(ns jiksnu.modules.as.sections
  (:require [ciste.config :refer [config]]
            [ciste.core :refer [with-context]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [full-uri index-section show-section]]
            [jiksnu.modules.as.helpers :as as.helpers]
            [jiksnu.modules.core.model.activity :as model.activity]
            [jiksnu.modules.core.model.domain :as model.domain]
            [jiksnu.modules.core.model.user :as model.user])
  (:import jiksnu.modules.core.model.Activity
           jiksnu.modules.core.model.User))

(defsection index-section [Activity :as]
  [activities page]
  (let [items (:items page)]
    {:objectTypes ["activity"]
     :items (doall (map
                    (fn [item]
                      (show-section item page))
                    items))
     :totalItems (:totalItems page)}))

(defsection show-section [Activity :as]
  [activity & _]
  (merge {:verb (:verb activity)
          :object (as.helpers/parse-object activity)
          :to (as.helpers/format-to activity)
          :cc (as.helpers/format-cc activity)
          :actor (show-section (model.activity/get-author activity))
          :generator (as.helpers/format-generator activity)

          :updated (:updated activity)
          :links (as.helpers/format-links activity)
          :url (full-uri activity)
          :published (:published activity)
          :content (:content activity)
          ;; NB: Id is a uri
          :id (:id activity)
          :text (:content activity)
          :localId (:_id activity)
          :source (:source activity)}
         (when (:conversation-uris activity)
           {:context {:conversations (first (:conversation-uris activity))}})
         (if-let [geo (:geo activity)]
           {:location {:type "place"
                       :latitude (:latitude geo)
                       :longitude (:longitude geo)}})))

(defsection show-section [User :as]
  [user & options]
  (let [{:keys [avatar-url domain username]} user
        scheme (if (some-> domain model.domain/fetch-by-id :secure) "https" "http")
        avatar-url (or avatar-url (model.user/image-link user))
        url           (format as.helpers/url-pattern       scheme domain username)
        profile-url   (format as.helpers/profile-pattern   scheme domain username)
        inbox-url     (format as.helpers/inbox-pattern     scheme domain username)
        outbox-url    (format as.helpers/outbox-pattern    scheme domain username)
        followers-url (format as.helpers/followers-pattern scheme domain username)
        following-url (format as.helpers/following-pattern scheme domain username)
        favorites-url (format as.helpers/favorites-pattern scheme domain username)
        list-url      (format as.helpers/lists-pattern     scheme domain username)]
    (array-map
     :preferredUsername username
     :url url
     :displayName (:displayName user username)
     :links {:self            {:href profile-url}
             :activity-inbox  {:href inbox-url}
             :activity-outbox {:href outbox-url}}
     :objectType "person"
     :updated (:updated user)
     :published (:created user)
     :followers {:url followers-url :totalItems 0}
     :following {:url following-url :totalItems 0}
     :favorites {:url favorites-url :totalItems 0}
     :lists     {:url list-url      :totalItems 0}
     :image     {:url    (str avatar-url ".jpg")
                 :width  96
                 :height 96}
     :id (:_id user)
     ;; TODO: How are these determined?
     :liked false
     :summary "INSERT SUMMARY HERE"
     :pump_io {:shared false
               :followed false}
     :type "person")))
