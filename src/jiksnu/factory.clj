(ns jiksnu.factory
  (:use [ciste.config :only [config]]
        [clj-factory.core :only [defseq deffactory fseq factory]])
  (:require [clj-time.core :as time]
            [inflections.core :as inf]
            [jiksnu.abdera :as abdera]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation
           jiksnu.model.Domain
           jiksnu.model.FeedSource
           jiksnu.model.FeedSubscription
           jiksnu.model.Like
           jiksnu.model.Group
           jiksnu.model.Subscription
           jiksnu.model.User))

(defseq :id
  [n]
  n)

(defseq :username
  [n]
  (str "user-" n))

(defseq :name
  [n]
  (rand-nth
   ["Alice" "Bob" "Carol" "Daniel"
    "Dave" "Eve" "Frank" "Tony" "George"
    "Jesus" "Ed" "Britta" "Abed" "Troy" "Jeff"
    "Pierce" "Shirley" "Annie" "Bill" "Leto" "Jean-Luc"
    "James" "Oscar" "Michael" "John" "Agent"]))

(defseq :surname
  [n]
  (rand-nth
   ["Smith" "Miller" "Johnson"
    "Doe" "McDuck" "Nebuchadnezzar" "Renfer"
    "Skywalker" "Bluth" "Adama" "Dumbledore"
    "Attreides"]))

(defseq :display-name
  [n]
  (str (fseq :name) " " (fseq :surname)))

(defseq :password
  [n]
  (str "hunter" n))

(defseq :domain
  [n]
  (str "subdomain" n ".example.local"))

(defseq :uri
  [n]
  (str "http://" (fseq :domain) "/" n))

(defseq :noun
  [n]
  (rand-nth
   [(str (fseq :name) " " (fseq :surname))
    "Butt"
    "Paul Anka"
    "Ubuntu"
    "Monkey"
    "Group"
    "Titanic"
    ""
    "Tardis"
    "Justin Bieber"
    (fseq :title)]))

(defseq :preamble
  [n]
  (rand-nth
   ["Users of "
    "Fans of "
    "The International Union to Destroy "
    "Employees of "
    "Ex-lovers of "
    "The Sacred order of "
    ""
    "The Heralds of the Imminent Arrival of "]))

(defseq :adjective
  [n]
  (rand-nth
   ["The Second Coming of "
    "The Purple "
    "My Monkey, "
    "Robot "
    "The Glorious "
    "Undead "
    "Zombie "
    ""
    "Ninja "
    "The Unflappable "
    "The Unsinkable "
    "The Underestimated "
    "The Official "]))

(defseq :group-name
  [n]
  (str (fseq :preamble)
       (fseq :adjective)
       (fseq :noun)))

(defseq :email
  [n]
  (str (fseq :username)
       "@" (fseq :domain)))

(defseq :title
  [n]
  (str (inf/ordinalize n) " post!"))

(defseq :summary
  [n]
  (str (inf/ordinalize n) " post!"))

(defseq :word
  [n]
  (str "word" n))

(defseq :bio
  [n]
  (fseq :word))

(defseq :location
  [n]
  "Anytown, USA")

(deffactory Domain
  {:_id (fseq :domain)
   :local false})

(deffactory :domain
  (factory Domain))

(deffactory User
  (let [password (fseq :password)
        first-name (fseq :name)
        last-name (fseq :surname)
        display-name (str first-name " " last-name)
        username (fseq :username)
        domain (fseq :domain)]
    {:username username
     :domain domain
     :id (str "acct:" username "@" domain)
     :local false
     :name display-name
     :display-name display-name
     :first-name first-name
     :last-name last-name}))

(deffactory :local-user
  (-> (factory User {:domain (config :domain)})
      (assoc :local true)))

(deffactory :user
  (factory User))

(deffactory Conversation
  {:items []
   :local true})

(deffactory :conversation
  (factory Conversation))

(deffactory Activity
  {
   ;; :id #'abdera/new-id
   :title (fseq :title)
   :summary (fseq :summary)
   :author (:_id (model.user/create (factory :user)))
   :published #'time/now
   :verb "post"
   :object-type "note"
   :updated #'time/now
   :public true})

(deffactory :activity
  (factory Activity))

(deffactory Subscription
  {:to (:_id (model.user/create (factory :local-user)))
   :local true
   :from (:_id (model.user/create (factory :local-user)))
   :created #'time/now})

(deffactory :subscription
  (factory Subscription))

(deffactory FeedSource
  {:topic (fseq :uri)})

(deffactory :feed-source
  (factory FeedSource))

(deffactory FeedSubscription
  {:topic (fseq :uri)})

(deffactory :feed-subscription
  (factory FeedSubscription))

(deffactory Group
  {:nickname (fseq :group-name)})

(deffactory :group
  (factory Group))

(deffactory Like
  {:user (:_id (model.user/create (factory :user)))
   :activity (:_id (model.activity/create (factory :activity)))})

(deffactory :like
  (factory Like))
