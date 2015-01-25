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
