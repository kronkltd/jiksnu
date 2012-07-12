(ns jiksnu.factory
  (:use [ciste.config :only [config]]
        [clj-factory.core :only [defseq deffactory fseq factory]])
  (:require [clj-time.core :as time]
            [inflections.core :as inf]
            [jiksnu.abdera :as abdera]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]))

(defn activity-id
  [] (:_id (model.activity/create (factory :activity))))

(defn user-id
  []
  (:_id (model.user/create (factory :local-user))))

(defseq :id [n] n)

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

(deffactory :domain
  {:_id (fseq :domain)
   :local false})

(deffactory :user
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
  (-> (factory :user {:domain (config :domain)})
      (assoc :local true)))

(deffactory :conversation
  {:items nil
   :local true})

(deffactory :activity
  {
   ;; :id #'abdera/new-id
   :title (fseq :title)
   :summary (fseq :summary)
   :author #'user-id
   :local true
   :published #'time/now
   :verb "post"
   :object-type "note"
   :updated #'time/now
   :public true})

(deffactory :subscription
  {:to #'user-id
   :local true
   :from #'user-id
   :created #'time/now})

(deffactory :feed-source
  {:topic (fseq :uri)})

(deffactory :feed-subscription
  {:topic (fseq :uri)})

(deffactory :group
  {:nickname (fseq :group-name)})

(deffactory :like
  {:user (:_id (model.user/create (factory :user)))
   :activity #'activity-id})
