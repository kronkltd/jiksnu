(ns jiksnu.templates.activity
  (:use ciste.debug
        closure.templates.core
        jiksnu.session)
  (:require [ciste.sections.default :as sd]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.templates.user :as template.user])
  (:import com.ocpsoft.pretty.time.PrettyTime))

(defn format-data
  [activity]
  (let [comments (map format-data (model.activity/get-comments activity))
        actor (current-user)]
    {:id (str (:_id activity))
     :author (-> activity :author model.user/fetch-by-id
                 template.user/format-data)
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
     :authenticated (template.user/format-data actor)
     :tags []
     :uri (:uri activity)
     :recipients []
     :published (str (:published activity))
     :published-formatted (.format (PrettyTime.) (:published activity))
     :buttonable (and actor
                      (or (:admin actor)
                          (some #(= % (:authors activity)) actor)))
     :comment-count (str (count comments))
     :comments comments}))

(deftemplate show
  [activity]
  (format-data activity))

(deftemplate index-block
  [activities]
  {:activities (map format-data activities)})
