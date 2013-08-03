(ns jiksnu.modules.as.sections.activity-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [full-uri show-section]])
  (:require [jiksnu.model.activity :as model.activity])
  (:import jiksnu.model.Activity))

;; show-section

(defsection show-section [Activity :as]
  [activity & _]
  (merge {:actor (show-section (model.activity/get-author activity))
          :content (:content activity)
          :id (:id activity)
          :local-id (:_id activity)
          :object (let [object (:object activity)]
                    {:name (:title activity)
                     :id (:id object)
                     :type (:type object)
                     :content (:content object)
                     :url (:id object)
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

