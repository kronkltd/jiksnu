(ns jiksnu.factory
  (:use [ciste.config :only [config]]
        [clj-factory.core :only [defseq deffactory fseq factory]])
  (:require [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [inflections.core :as inf]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]

            ))

(defn domain-id
  []
  (:_id (actions.domain/create (factory :domain))))

(defn activity-id
  [] (:_id (actions.activity/create (factory :activity))))

(defn conversation-id
  [] (:_id (actions.conversation/create (factory :conversation))))

(defn source-id
  [] (:_id (actions.feed-source/create (factory :feed-source))))

(defn user-id
  []
  (:_id (actions.user/create (factory :local-user))))

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

(defseq :secret-key
  [n]
  "c06531d7c862cad32a3c5356f1195b8965ae4ffc38c5e559f962ed1acb765831")

(defseq :verify-token
  [n]
  "3c53c56289b940c416296690f95abb45")

(defseq :lease-seconds
  [n]
  2000)

(defseq :word
  [n]
  (str "foo" n))

(defseq :path
  [n]
  (str "/resources/" n))

(defn make-uri
  [domain & [path]]
  (let [path (or path (fseq :path))]
   (str "http://" domain path)))

(defseq :uri
  [n]
  (make-uri (fseq :domain) (str "/" n)))

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

(defseq :content
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
        username (fseq :username)]
    {:username username
     :domain #'domain-id
     :email (fseq :email)
     :name display-name
     ;; :update-source #'source-id
     :display-name display-name
     :first-name first-name
     :last-name last-name}))

(deffactory :local-user
  (-> (factory :user {:domain (config :domain)})
      (assoc :local true)))

(deffactory :conversation
  {:url (fseq :uri)})

(deffactory :resource
  {:url (fseq :uri)})

(deffactory :activity
  {:title (fseq :title)
   :content (fseq :content)
   :url (fseq :uri)
   :author #'user-id
   :verb "post"})

(deffactory :subscription
  {:to #'user-id
   :local true
   :from #'user-id
   :created #'time/now})

(deffactory :feed-source
  {:topic (fseq :uri)
   :hub (fseq :uri)})

(deffactory :feed-subscription
  {:url (fseq :uri)
   :callback (fseq :uri)})

(deffactory :group
  {:nickname (fseq :group-name)})

(deffactory :like
  {:user #'user-id
   :activity #'activity-id})
