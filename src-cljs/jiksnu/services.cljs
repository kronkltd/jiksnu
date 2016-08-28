(ns jiksnu.services
  (:require jiksnu.app
            [taoensso.timbre :as timbre])
  (:use-macros [gyr.core :only [def.service]]))

(def page-mappings
  {"activities"    "/model/activities"
   "conversations" "/model/conversations"
   "domains"       "/model/domains"
   "feed-sources"  "/model/feed-sources"
   "groups"        "/model/groups"
   "group-memberships" "/model/group-memberships"
   "likes"         "/model/likes"
   "notifications" "/model/notifications"
   "resources"     "/model/resources"
   "streams"       "/model/streams"
   "subscriptions" "/model/subscriptions"
   "users"         "/model/users"})

(def subpage-mappings
  {"Activity"     {"likes"      #(str "/model/activities/"    (.-_id %) "/likes")}
   "Conversation" {"activities" #(str "/model/conversations/" (.-_id %) "/activities")}
   "Group"        {"members"    #(str "/model/groups/"        (.-_id %) "/members")}
   "Stream"       {"activities" #(str "/model/streams/"       (.-_id %) "/activities")}
   "User"         {"activities" #(str "/model/users/"         (.-_id %) "/activities")
                   "following"  #(str "/model/users/"         (.-_id %) "/following")
                   "followers"  #(str "/model/users/"         (.-_id %) "/followers")
                   "groups"     #(str "/model/users/"         (.-_id %) "/groups")
                   "streams"    #(str "/model/users/"         (.-_id %) "/streams")}})

(def.service jiksnu.pageService
  [$q $http]

  (let [service #js {}]
    (set! (.-fetch service)
          (fn [page-name]
            (let [d (.defer $q)]
              (if-let [url (get page-mappings page-name)]
                (-> $http
                    (.get url)
                    (.success #(.resolve d %))
                    (.error #(.reject d)))
                (throw (str "page mapping not defined: " page-name)))
              (.-promise d))))
    service))

(def.service jiksnu.subpageService
  [$q $http]

  (let [service #js {}]
    (set! (.-fetch service)
          (fn [parent page-name]
            (let [type (.getType parent)
                  d (.defer $q)]
              (if-let [mapping-fn (get-in subpage-mappings [type page-name])]
                (let [url (mapping-fn parent)]
                  ;; (timbre/debugf "url: %s" url)
                  (-> $http
                      (.get url)
                      (.success #(.resolve d %))
                      (.error #(.reject d)))
                  (.-promise d))
                (throw (str "Could not find subpage mapping for model "
                            (type parent) " with label " page-name))))))
    service))
