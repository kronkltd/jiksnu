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
                   (notify "cache miss")
                   (-> $http
                       (.get url)
                       (.success
                        (fn [data]
                          (notify (str "setting id: " id))
                          (.put cache id data)
                          (.resolve d data))))
                   (.-promise d))))
    (! s.get (fn [id]
               (let [d (.defer $q)]
                 (if (and id (not= id ""))
                   (if-let [o (.get cache id)]
                     (do
                       (notify "cache hit")
                       (.resolve d o))
                     (-> s
                         (.fetch id)
                         (.then (fn [o] (.resolve d o)))))
                   (.reject d "nil id"))
                 (.-promise d))))
    s))
