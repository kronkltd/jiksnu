(ns jiksnu.modules.core.factory
  (:require [ciste.config :refer [config]]
            [clj-factory.core :refer [defseq deffactory defrecordfactory
                                      fseq factory]]
            [clj-time.core :as time]
            [clojure.spec :as s]
            [inflections.core :as inf]
            [jiksnu.modules.core.actions.activity-actions :as actions.activity]
            [jiksnu.modules.core.actions.conversation-actions :as actions.conversation]
            [jiksnu.modules.core.actions.domain-actions :as actions.domain]
            [jiksnu.modules.core.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.modules.core.actions.user-actions :as actions.user]
            [jiksnu.modules.core.model :as model]))

(defn domain-id
  []
  (:_id (actions.domain/create (factory :domain))))

(s/fdef domain-id :ret (s/fspec :ret string?))

(defn activity-id
  [] (:_id (actions.activity/create (factory :activity))))

(defn conversation-id
  [] (:_id (actions.conversation/create (factory :conversation))))

(defn source-id
  [] (:_id (actions.feed-source/create (factory :feed-source) {})))

(defn user-id
  []
  (:_id (actions.user/create (factory :local-user))))

(defseq :id [n] n)

(defseq :username
  [n]
  (str "user-" n))

(defseq :givenName
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

(defseq :name
  [n]
  (str (fseq :givenName) " " (fseq :surname)))

(defseq :album-name
  [n]
  (fseq :name))

(defseq :password
  [n]
  "hunter2")

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
    "The Secret Society for "
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
   :http true
   :local false})

(defrecordfactory :user model/map->User
  (let [password (fseq :password)
        first-name (fseq :givenName)
        last-name (fseq :surname)
        name (str first-name " " last-name)
        username (fseq :username)]
    {:username username
     :domain #'domain-id
     :email (fseq :email)
     :name name
     ;; :update-source #'source-id
     :first-name first-name
     :last-name last-name}))

(defrecordfactory :local-user model/map->User
  (assoc (factory :user {:domain (config :domain)})
         :local true))

(defrecordfactory :conversation model/map->Conversation
  {:url (fseq :uri)})

(defrecordfactory :resource model/map->Resource
  {:_id (fseq :uri)})

(defrecordfactory :album model/map->Album
  {:name (fseq :album-name)})

(defrecordfactory :activity model/map->Activity
  {:title (fseq :title)
   :content (fseq :content)
   ;; :published (time/now)
   ;; :url (fseq :uri)
   ;; :verb "post"
   :author #'user-id})

(defrecordfactory :full-activity model/map->Activity
  {:title (fseq :title)
   :content (fseq :content)
   :published (time/now)
   :url (fseq :uri)
   :author #'user-id
   :verb "post"})

(defrecordfactory :client model/map->Client
  {:_id (fseq :word)})

(defrecordfactory :request-token model/map->RequestToken
  {:_id (fseq :word)
   :callback (fseq :uri)})

(defrecordfactory :subscription model/map->Subscription
  {:to #'user-id
   :local true
   :from #'user-id
   :created #'time/now})

(defrecordfactory :feed-source model/map->FeedSource
  {:topic (fseq :uri)
   :hub (fseq :uri)})

(deffactory :feed-subscription
  {:url (fseq :uri)
   :callback (fseq :uri)})

(deffactory :group
  {:name (fseq :group-name)})

(deffactory :like
  {:user #'user-id
   :activity #'activity-id})

(deffactory :service
  {})

(defrecordfactory :stream model/map->Stream
  {:name (fseq :word)
   :user #'user-id
   :public true})

(deffactory :notification
  {})
