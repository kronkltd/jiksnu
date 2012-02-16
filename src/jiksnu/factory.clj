(ns jiksnu.factory
  (:use (ciste [debug :only [spy]])
        clj-factory.core)
  (:require (jiksnu [abdera :as abdera])
            (jiksnu.model [user :as model.user])
            (karras [sugar :as sugar]))
  (:import jiksnu.model.Activity
           jiksnu.model.Domain
           jiksnu.model.Subscription
           jiksnu.model.User))

(defseq :id
  [n]
  n)

(defseq :username
  [n]
  (str "user" n))

(defseq :password
  [n]
  (str "hunter" n))

(defseq :domain
  [n]
  (str "subdomain" n ".example.com"))

(defseq :uri
  [n]
  (str "http://" (fseq :domain) "/" n))

(defseq :word
  [n]
  (str "word" n))

(deffactory Domain
  {:_id (fseq :domain)
   :local false})

(deffactory :domain
  (factory Domain))

(deffactory Subscription
  {:to (fseq :word)
   :from (fseq :word)
   :created #'sugar/date})

(deffactory User
  (let [password (fseq :word)]
    {:username (fseq :word)
     :domain (fseq :domain)
     :name (fseq :word)
     :first-name (fseq :word)
     :last-name (fseq :word)
     :password password
     :confirm-password password}))

(deffactory :user
  (factory User))

(deffactory Activity
  {:id #'abdera/new-id
   :title (fseq :word)
   :summary (fseq :word)
   :author (:_id (model.user/create (factory :user)))
   :published #'sugar/date
   :verb "post"
   :object-type "note"
   :updated #'sugar/date
   :public true})

(deffactory :activity
  (factory Activity))
