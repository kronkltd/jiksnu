(ns jiksnu.modules.as.sections.activity-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [full-uri show-section]])
  (:require [ciste.config :refer [config]]
            [clojure.tools.logging :as log]
            [jiksnu.model.activity :as model.activity])
  (:import jiksnu.model.Activity))

(defn proxy-url
  [url]
  "https://%s/api/proxy/PROXYID"
  )

;; show-section

(defsection show-section [Activity :json]
  [activity & _]
  (merge {:actor (show-section (model.activity/get-author (log/spy :info activity)))
          :content (:content activity)
          :id (:id activity)
          :localId (:_id activity)
          :source (:source activity)
          :object (let [object (:object activity)
                        object-link (format "https://%s/api/%s/%s" (config :domain)
                                            (:type object)
                                            (:_id object)
                                            )
                        likes-link (str object-link "/likes")
                        replies-link (str object-link "/replies")
                        shares-link (str object-link "/shares")
                        ]
                    {:name (:title activity)
                     :id (:id object)
                     :type (:type object)
                     :objectType (:type object)
                     :links {
                             :self {
                                    :href object-link
                                    }
                             }
                     :likes {
                             :url likes-link
                             :totalItems 0
                             :pump_io {
                                       :proxyURL (proxy-url likes-link)
                                       }

                             }
                     :replies {
                             :url replies-link
                             :totalItems 0
                             :pump_io {
                                       :proxyURL (proxy-url replies-link)
                                       }


                               }
                     :shares {
                             :url shares-link
                             :totalItems 0
                             :pump_io {
                                       :proxyURL (proxy-url shares-link)
                                       }


                               }
                     :content (or (:content object)
                                  (:content activity)
                                  )
                     :updated (or (:updated object)
                                  (:updated activity))
                     :published (or (:created object)
                                  (:created activity))
                     :url (:id object)
                     :pump_io {
                               :shared false
                               :proxyUrl (proxy-url (:id object))
                               }
                     :liked false

                     :tags (map
                            (fn [tag]
                              {:name tag
                               :type "http://activityschema.org/object/hashtag"})
                            (:tags activity))
                     ;; "published" (:published object)
                     ;; "updated" (:updated object)
                     })

          :published (:published activity)

          :updated (:updated activity)
          :verb (:verb activity)
          :title (:title activity)
          :url (full-uri activity)}
         (when (:links activity)
           ;; TODO: Some of these links don't make sense in the
           ;; context of an AS stream
           {:links (:links activity)})
         (when (:conversation-uris activity)
           {:context {:conversations (first (:conversation-uris activity))}})
         (if-let [geo (:geo activity)]
           {:location {:type "place"
                       :latitude (:latitude geo)
                       :longitude (:longitude geo)}})))

