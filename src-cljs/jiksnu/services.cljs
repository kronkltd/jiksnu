(ns jiksnu.services
  (:require jiksnu.app
            [taoensso.timbre :as timbre])
  (:use-macros [gyr.core :only [def.service]]))

(def page-mappings
  {
   "activities"    "/model/activities"
   "conversations" "/model/conversations"
   "domains"       "/model/domains"
   "feed-sources"  "/model/feed-sources"
   "groups"        "/model/groups"
   "resources"     "/model/resources"
   "streams"       "/model/streams"
   "users"         "/model/users"
   })

(def subpage-mappings
  {"activities" (fn [parent] (str "/model/conversations/" (.-_id parent) "/activities"))
   "following"  (fn [parent] (str "/model/users/" (.-_id parent) "/following"))
   "followers"  (fn [parent] (str "/model/users/" (.-_id parent) "/followers"))
   "groups"     (fn [parent] (str "/model/users/" (.-_id parent) "/groups"))
   "streams"    (fn [parent] (str "/model/users/" (.-_id parent) "/streams"))})

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
            (let [d (.defer $q)]
              (if-let [mapping-fn (get subpage-mappings page-name)]
                (let [url (mapping-fn parent)]
                  #_(timbre/debug "url" url parent)
                  (-> $http
                      (.get url)
                      (.success #(.resolve d %))
                      (.error #(.reject d)))
                  (.-promise d))
                (throw (str "Could not find subpage mapping for model "
                            (type parent) " with label " page-name))))))
    service))
