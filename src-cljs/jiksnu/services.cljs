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
                  (do
                    ;; (.log js/console "cache hit" id)
                    (.-promise d-prime))
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
      ;; (.log js/console "cache miss" id)
      (.put cache id d)
      (-> $http
          (.get (get-url id))
          (.success
           (fn [data]
             ;; (.log js/console "setting id: " id)
             (.resolve d data))))
      (.-promise d))))

(def.service jiksnu.activityService
  [DSCacheFactory $q $http]
  (let [cache-name "activities"
        get-url #(str "/activities/" %)]
    (DSCacheFactory cache-name (obj :capacity 1000))
    (let [cache (.get DSCacheFactory cache-name)
          service (obj)]
      (! service.fetch (cache-fetch cache $q $http get-url))
      (! service.get (cache-get service cache $q))
      service)))

(def.service jiksnu.userService
  [DSCacheFactory $q $http]
  (let [cache-name "users"
        get-url #(str "/users/" % ".json")]
    (DSCacheFactory cache-name (obj :capacity 1000))
    (let [cache (.get DSCacheFactory cache-name)
          service (obj)]
      (! service.fetch (cache-fetch cache $q $http get-url))
      (! service.get (cache-get service cache $q))
      service)))

(def page-mappings
  {
   "conversations" "/main/conversations"
   "domains"       "/main/domains"
   "feed-sources"  "/main/feed-sources"
   "groups"        "/main/groups"
   "resources" "/main/resources"
   "users" "/main/users"
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
               (.success
                (fn [data]
                  (.resolve d data)))
               (.error
                (fn []
                  (.reject d))))
           (.-promise d))))
    service))
