(ns jiksnu.templates.activity
  (:use ciste.debug
        closure.templates.core
        [clj-gravatar.core :only (gravatar-image)]
        jiksnu.session)
  (:require [ciste.sections.default :as sd]
            [jiksnu.model.user :as model.user])
  (:import com.ocpsoft.pretty.time.PrettyTime))

(defn format-data
  [activity]
  {:id (str (:_id activity))
   :authors (map
             (fn [id]
               ;; FIXME: Move this to user
               (let [user (model.user/fetch-by-id id)]
                 {:id (str id)
                  :name (:username user)
                  :url (str "/users/" id)
                  :display-name
                  (or (:display-name user)
                      (str (:first-name user) " " (:last-name user)))
                  :imgsrc (or (:avatar-url user)
                              (and (:email user)
                                   (gravatar-image (:email user)))
                              (gravatar-image (:jid user)))}))
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
