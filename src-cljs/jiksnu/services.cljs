(ns jiksnu.services
  (:require jiksnu.app)
  (:use-macros [gyr.core :only [def.service]]
               [purnam.core :only [? ?> ! !> obj arr]]))

(def page-mappings
  {
   "conversations" "/api/conversations"
   "domains"       "/api/domains"
   "feed-sources"  "/api/feed-sources"
   "groups"        "/api/groups"
   "resources"     "/api/resources"
   "users"         "/api/users"
   })

(def subpage-mappings
  {"following" (fn [parent]
                 (str "/api/users/" (.-_id parent) "/following"))
   "followers" (fn [parent]
                 (str "/api/users/" (.-_id parent) "/followers"))})

(def.service jiksnu.pageService
  [$q $http]

  (let [service (obj)]
    (! service.fetch
       (fn [page-name]
         (let [d (.defer $q)
               url (get page-mappings page-name)]
           (-> $http
               (.get url)
               (.success #(.resolve d %))
               (.error #(.reject d)))
           (.-promise d))))
    service))

(def.service jiksnu.subpageService
  [$q $http]

  (let [service (obj)]
    (! service.fetch
       (fn [parent page-name]
         (let [d (.defer $q)
               url ((get subpage-mappings page-name) parent)]
           (.log js/console "url" url)
           (-> $http
               (.get url)
               (.success #(.resolve d %))
               (.error #(.reject d)))
           (.-promise d))))
    service))
