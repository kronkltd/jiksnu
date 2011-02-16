(ns jiksnu.factory
  (:use [jiksnu.config :only (config)]
        jiksnu.model
        jiksnu.model.activity)
  (:require [karras.sugar :as sugar])
  (:import jiksnu.model.Activity
           jiksnu.model.Subscription
           jiksnu.model.User))

(def #^:dynamic *counters* (ref {}))

(defn get-counter
  [type]
  (get @*counters* type 0))

(defn inc-counter
  [type]
  (dosync
   (alter
    *counters*
    (fn [m]
      (assoc m type (inc (get m type 0)))))))

(defn next-counter
  [type]
  (get (inc-counter type) type))

(defmulti fseq (fn [type & _] type))

(defmulti factory (fn [type & _] type))

(defmacro deffactory
  [type opts & body]
  `(defmethod jiksnu.factory/factory ~type
     [_# & opts#]
     (merge (new ~type)
            (into {}
                  (map
                   (fn [[k# v#]]
                     [k# (if (ifn? v#)
                           (v#) v#)])
                   ~opts))
            (first opts#))))

(defmacro defseq
  [type let-form result]
  `(let [type# ~type]
     (defmethod jiksnu.factory/fseq type#
       [type#]
       (let [~let-form [(next-counter type#)]]
         ~result))))

(defseq :id
  [n]
  n)

(defseq :word
  [n]
  (str "word" n))

(deffactory Activity
  {:_id #'new-id
   :title (fseq :word)
   :summary (fseq :word)
   :published #'sugar/date
   :updated #'sugar/date
   :public true})

(deffactory User
  (let [password (fseq :word)]
    {:username (fseq :word)
     :domain (-> (config) :domain)
     :name (fseq :word)
     :first-name (fseq :word)
     :last-name (fseq :word)
     :password password
     :confirm-password password}))

(deffactory Subscription
  {:to (fseq :word)
   :from (fseq :word)
   :created #'sugar/date})

;; (defmethod factory Activity
;;   [_ opts]
;;   (merge (Activity.)
;;          opts))
