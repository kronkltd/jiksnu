(ns jiksnu.templates.activity
  (:use ciste.debug
        closure.templates.core
        jiksnu.session)
  (:require [ciste.sections.default :as sd]
            [jiksnu.model.user :as model.user]
            [jiksnu.templates.user :as template.user])
  (:import com.ocpsoft.pretty.time.PrettyTime))

(defn format-data
  [activity]
  {:id (str (:_id activity))
   :authors (map (comp template.user/format-data model.user/fetch-by-id)
                 (:authors activity))
   :object-type (-> activity :object :object-type)
   :local (:local activity)
   :public (:public activity)
   :content (or (-> activity :object :content)
                (-> activity :content)
                (-> activity :title))
   :title (or (-> activity :object :content)
              (:content activity)
              (:title activity))
   :lat (str (:lat activity))
   :long (str (:long activity))
   :tags []
   :uri (:uri activity)
   :published (str (:published activity))
   :published-formatted (.format (PrettyTime.) (:published activity))
   :buttonable true
   :comment-count (Integer. 0) #_(get-comment-count activity)
   :comments []})

(deftemplate show
  [activity]
  (format-data activity))

(deftemplate index-block
  [activities]
  {:activities (map format-data activities)})
