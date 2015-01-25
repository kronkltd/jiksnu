(ns jiksnu.services
  (:require jiksnu.app)
  (:use-macros [gyr.core :only [def.service]]
               [purnam.core :only [? ?> ! !> obj arr]]))

(defn- cache-get
  [service cache $q]
  (fn [id]
    (let [d (.defer $q)]
      (if (and id (not= id ""))
        (let [p (if-let [d-prime (.get cache id)]
                  (.-promise d-prime)
                  (.fetch service id))]
          (.then p
                 #(.resolve d %)
                 #(.reject d)))
        (.reject d "nil id"))
      (.-promise d))))

(defn- cache-fetch
  [cache $q $http get-url]
  (fn [id]
    (let [d (.defer $q)]
      (.put cache id d)
      (-> $http
          (.get (get-url id))
          (.success
           (fn [data]
             (.resolve d data))))
      (.-promise d))))

(def.service jiksnu.activityService
  [DSCacheFactory $q $http]
  (let [cache-name "activities"
        get-url #(str "/api/activities/" %)]
    (DSCacheFactory cache-name (obj :capacity 1000))
    (let [cache (.get DSCacheFactory cache-name)
          service (obj)]
      (! service.fetch (cache-fetch cache $q $http get-url))
      (! service.get (cache-get service cache $q))
      service)))

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
