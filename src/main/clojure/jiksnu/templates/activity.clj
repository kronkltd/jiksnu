(ns jiksnu.templates.activity
  (:use ciste.debug
        closure.templates.core
        [clj-gravatar.core :only (gravatar-image)]
        jiksnu.session)
  (:require [ciste.sections.default :as sd]
            [jiksnu.model.user :as model.user])
  (:import com.ocpsoft.pretty.time.PrettyTime))

(deftemplate show
  [activity]
  {:id (str (:_id activity))
   :authors (map
             (fn [id]
               (let [user (model.user/fetch-by-id id)]
                 {:_id (str id)
                  :name (:name user)
                  :imgsrc (or (:avatar-url user)
                              (and (:email user)
                                   (gravatar-image (:email user)))
                              (gravatar-image (:jid user)))}))
             (:authors activity))
   :objecttype (-> activity :object :object-type)
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
   :publishedformatted (.format (PrettyTime.) (:published activity))
   :buttonable true
   :commentcount (Integer. 0) #_(get-comment-count activity)
   :comments []})
