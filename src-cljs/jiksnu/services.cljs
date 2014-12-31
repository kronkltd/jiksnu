(ns jiksnu.services
  (:require jiksnu.app)
  (:use-macros [gyr.core :only [def.service]]
               [purnam.core :only [? ?> ! !> obj arr]]))

(def.service jiksnu.userService
  [DSCacheFactory $q $http notify]
  (DSCacheFactory "users" (obj :capacity 1000))
  (let [cache (.get DSCacheFactory "users")
        s (obj)]
    (! s.fetch (fn [id]
                 (.log js/console (str "id: \"" id "\""))
                 (let [url (str "/users/" id ".json")
                       d (.defer $q)]
                   (.log js/console "cache miss" id)
                   (.put cache id d)
                   (-> $http
                       (.get url)
                       (.success
                        (fn [data]
                          (.log js/console "setting id: " id)
                          (.resolve d data))))
                   (.-promise d))))
    (! s.get (fn [id]
               (let [d (.defer $q)]
                 (if (and id (not= id ""))
                   (let [p (if-let [d-prime (.get cache id)]
                             (do
                               (.log js/console "cache hit" id)
                              (.-promise d-prime))
                             (.fetch s id))]
                     (.then p
                            #(.resolve d %)
                            #(.reject d)))
                   (.reject d "nil id"))
                 (.-promise d))))
    s))
