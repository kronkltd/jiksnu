(ns jiksnu.modules.as.sections.activity-sections
  (:require [ciste.config :refer [config]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [full-uri index-section show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.model.activity :as model.activity])
  (:import jiksnu.model.Activity))

(defn proxy-url
  [url]
  "https://%s/api/proxy/PROXYID"
  )

(defsection index-section [Activity :as]
  [activities page]
  (let [items (:items page)]
    {
     :objectTypes ["activity"]
     :items (doall (map
                    (fn [item]
                      (show-section item page))
                    items))
     :totalItems (:totalItems page)}))

;; show-section

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

     :tags (map
            (fn [tag]
              {:name tag
               :type "http://activityschema.org/object/hashtag"})
            (:tags activity))
     ;; "published" (:published object)
     ;; "updated" (:updated object)
     }

    )

  )

(defn format-to
  [activity]
  [{
     :id "http://activityschema.org/collection/public"
     }]
  )

(defn format-cc
  [activity]
  [

     ]
  )

(defn format-generator
  [activity]
  {
   :displayName "Jiksnu" ;; TODO: name of site
   :objectType "service"
   ;; TODO: service stuff
   }
  )

(defn format-links
  [activity]
  (let [links (:links activity)]
    (merge {} links)))

(defsection show-section [Activity :as]
  [activity & _]
  (merge {
          :verb (:verb activity)
          :object (parse-object activity)
          :to (format-to activity)
          :cc (format-cc activity)
          :actor (show-section (model.activity/get-author activity))
          :generator (format-generator activity)

          :updated (:updated activity)
          :links (format-links activity)
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

