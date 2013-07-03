(ns jiksnu.actions.dialback-actions



  (:use [ciste.config :only [config]]
        [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?> -?>>]]
        [lamina.trace :only [defn-instrumented]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clj-statsd :as s]
            [clj-tigase.element :as element]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.ops :as ops]
            [jiksnu.session :as session]
            [jiksnu.templates :as templates]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.activity-transforms :as transforms.activity]
            [jiksnu.util :as util]
            [lamina.core :as l]
            [monger.collection :as mc])
  (:import javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.User
           org.apache.abdera.model.Entry
           org.apache.abdera.model.Element)





  )

(def index*    (templates/make-indexer 'jiksnu.model.dialback :sort-clause {:date 1}))

(defaction index
  [& options]
  (apply index* options))

(defn-instrumented prepare-create
  [activity]
  (-> activity
      transforms/set-_id

      ))

(defaction create
  [params]
  (let [item (model.dialback/create item)]
    (model.dialback/fetch-by-id (:_id item))))

;; (defaction delete
;;   [activity]
;;   (let [actor-id (session/current-user-id)
;;         author (:author activity)]
;;     (if (or (session/is-admin?) (= actor-id author))
;;       (model.activity/delete activity)
;;       ;; TODO: better exception type
;;       (throw+ {:type :authorization
;;                :msg "You are not authorized to delete that activity"}))))

(definitializer
  (util/require-module "dialback"))
