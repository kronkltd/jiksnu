(ns jiksnu.modules.as.helpers
  (:require [ciste.config :refer [config]]
            [ciste.core :refer [with-context]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [full-uri index-section show-section]]
            [jiksnu.model.domain :as model.domain]))

(def url-pattern       "%s://%s/main/users/%s")
(def profile-pattern   "%s://%s/api/user/%s/profile")
(def inbox-pattern     "%s://%s/api/user/%s/inbox")
(def outbox-pattern    "%s://%s/api/user/%s/feed")
(def followers-pattern "%s://%s/api/user/%s/followers")
(def following-pattern "%s://%s/api/user/%s/following")
(def favorites-pattern "%s://%s/api/user/%s/favorites")
(def lists-pattern     "%s://%s/api/user/%s/lists/person")

(defn proxy-url
  [_url]
  "https://%s/api/proxy/PROXYID")

(defn parse-object
  [activity]
  (let [object (:object activity)
        object-link (format "https://%s/api/%s/%s" (config :domain)
                            (:type object)
                            (:_id object))
        likes-link (str object-link "/likes")
        replies-link (str object-link "/replies")
        shares-link (str object-link "/shares")]
    {:name (:title activity)
     :id (:id object)
     :type (:type object)
     :objectType (:type object)
     :links {:self {:href object-link}}
     :likes {:url likes-link
             :totalItems 0
             :pump_io {:proxyURL (proxy-url likes-link)}}
     :replies {:url replies-link
               :totalItems 0
               :pump_io {:proxyURL (proxy-url replies-link)}}
     :shares {:url shares-link
              :totalItems 0
              :pump_io {:proxyURL (proxy-url shares-link)}}
     :content (or (:content object)
                  (:content activity))
     :updated (or (:updated object)
                  (:updated activity))
     :published (or (:created object)
                    (:created activity))
     :url (:id object)
     :pump_io {:shared false
               :proxyUrl (proxy-url (:id object))}
     :liked false

     ;; "published" (:published object)
     ;; "updated" (:updated object)
     :tags (map
            (fn [tag]
              {:name tag
               :type "http://activityschema.org/object/hashtag"})
            (:tags activity))}))

(defn format-to
  [_activity]
  [{:id "http://activityschema.org/collection/public"}])

(defn format-cc
  [_activity]
  [])

(defn format-generator
  [_activity]
  ;; TODO: service stuff
  ;; TODO: name of site
  {:displayName "Jiksnu"
   :objectType "service"})

(defn format-links
  [activity]
  (let [links (:links activity)]
    (merge {} links)))

(defn format-collection
  [user page]
  (let [domain (config :domain)
        scheme (if (some-> domain model.domain/fetch-by-id :secure) "https" "http")]
    (with-context [:http :as]
      {:displayName (str "Collections of persons for " (:_id user))
       :objectTypes [(:objectTypes page "collection")]
       :url         (format lists-pattern scheme domain (:username user))
       :links       {:self {:href (format lists-pattern scheme domain (:username user))}}
       :items       (doall (map show-section (:items page)))
       :totalItems  (:totalItems page)
       :author      (show-section user)})))
