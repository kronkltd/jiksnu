(ns jiksnu.actions.user-actions-test
  (:use [ciste.config :only [config]]
        [ciste.core :only [with-context]]
        [ciste.sections.default :only [show-section]]
        [clj-factory.core :only [factory fseq]]
        [midje.sweet :only [=> anything throws contains]]
        [jiksnu.test-helper :only [context future-context test-environment-fixture]]
        jiksnu.actions.user-actions)
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.factory :as factory]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.authentication-mechanism :as model.auth-mechanism]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.ops :as ops]
            [jiksnu.util :as util]
            [lamina.core :as l]
            [ring.util.codec :as codec])
  (:import jiksnu.model.Domain
           jiksnu.model.User
           org.apache.abdera.model.Person))

(defn mock-user-meta
  [username domain-name uri source-link]
  (cm/string->document
   (format "
<XRD xmlns=\"http://docs.oasis-open.org/ns/xri/xrd-1.0\">
  <Subject>%s</Subject>
  <Link rel=\"http://webfinger.net/rel/profile-page\" type=\"text/html\" href=\"%s\"></Link>
  <Link rel=\"http://apinamespace.org/atom\" type=\"application/atomsvc+xml\"
        href=\"http://%s/api/statusnet/app/service/%s.xml\">
    <Property type=\"http://apinamespace.org/atom/username\">%s</Property>
  </Link>
  <Link rel=\"http://apinamespace.org/twitter\" href=\"https://%s/api/\">
    <Property type=\"http://apinamespace.org/twitter/username\">%s</Property>
  </Link>
  <Link rel=\"http://schemas.google.com/g/2010#updates-from\"
        href=\"%s\" type=\"application/atom+xml\"></Link>
</XRD>"
           uri uri domain-name username username domain-name username source-link)))

(test-environment-fixture

 (context #'get-username
   (let [username (fseq :username)
         domain-name (fseq :domain)
         template (str "http://" domain-name "/xrd?uri={uri}")
         domain (-> (factory :domain {:_id domain-name
                                      :discovered true})
                    actions.domain/find-or-create
                    (actions.domain/add-link {:rel "lrdd" :template template}))]

     (context "when given a http uri"
       (future-context "and it has user info")

       (context "and it does not have user info"
         (context "and the jrd request returns info"
           (let [uri (factory/make-uri domain-name "/users/1")
                 params {:id uri}]
             (get-username params) => (contains {:username username})
             (provided
              (discover-user-jrd anything anything) => {:username username}
              (discover-user-xrd anything anything) => nil :times 0)))

         (context "and the xrd request returns info"
           (let [uri (factory/make-uri domain-name "/users/1")
                 params {:id uri}]
             (get-username params) => (contains {:username username})
             (provided
              (discover-user-xrd anything anything) => {:username username}
              (discover-user-jrd anything anything) => nil)))
         )
       )

     (context "when given an acct uri"
       (let [uri (str "acct:" username "@" domain-name)
             params {:id uri}]
         (get-username params) => (contains {:username username}))))
   )

 (context #'get-domain
   (context "when the domain already exists"

     (let [domain (mock/a-domain-exists {:discovered true})]

       (context "when the domain is specified"
         (let [response (get-domain {:domain (:_id domain)})]
           response => (partial instance? Domain)
           (:_id response) => (:_id domain)))

       (context "when the domain is not specified"
         (context "when there is an id"
           (context "when it is a http url"
             (let [response (get-domain {:id (str "http://" (:_id domain)
                                                  "/users/1")})]
               response => (partial instance? Domain)
               (:_id response) => (:_id domain)))

           (context "when it is an acct uri"
             (let [response (get-domain {:id (str "acct:" (fseq :username)
                                                  "@" (:_id domain))})]
               response => (partial instance? Domain)
               (:_id response) => (:_id domain))))))))

 (context #'create
   (context "when the params are nil"
     (let [params nil]
       (create params) => (throws RuntimeException)))
   (context "empty map"
     (let [params {}]
       (create params) => (throws RuntimeException)))
   (context "local user"
     (let [params {:username (fseq :username)
                   :domain (config :domain)}]
       (create params) => model/user?))
   (context "when the params contain links"
     (let [params {:username (fseq :username)
                   :domain (config :domain)
                   :links [{:href (fseq :uri) :rel "alternate"}]}]
       (create params) => model/user?)))

 (context #'index
   (index) => map?)

 (context #'person->user
   (context "when the user has an acct uri"

     (context "when the domain is discovered"
       (context "when given a Person generated by show-section"
         (db/drop-all!)
         (let [user (mock/a-user-exists)
               person (with-context [:http :atom] (show-section user))]
           (person->user person) =>
           (check [response]
             response => (partial instance? User)
             response => (contains (select-keys user #{:username :id :domain :url :name}))
             )
           )))

     (context "when the domain is not discovered"
       (context "when given a Person generated by show-section"
         (db/drop-all!)
         (let [user (mock/a-user-exists)
               person (with-context [:http :atom] (show-section user))]
           (person->user person) =>
           (check [response]
             response => (partial instance? User)
             response => (contains (select-keys user #{:username :id :domain :url :name})))))))

   (context "when the user has an http uri"
     (context "when the domain is not discovered"
       (context "when given a Person generated by show-section"
         (let [domain-name (fseq :domain)
               uri (str "http://" domain-name "/users/1")
               person (.newAuthor abdera/abdera-factory)]
           (doto person
             (.setUri uri))
           ;; (person->user person) => (partial instance? User)
           (person->user person) => (contains {:id uri
                                               :domain domain-name
                                               :username "bob"})
           (provided
             (actions.domain/get-discovered anything) => .domain.
             (get-username anything) => "bob"))))))

 (context #'find-or-create-by-remote-id
   (let [username (fseq :username)
         domain-name (fseq :domain)
         template (str "http://" domain-name "/xrd?uri={uri}")]

     (context "when given a http uri"
       (context "when the domain is discovered"
         (db/drop-all!)

         (let [username (fseq :username)
               template (format "http://%s/xrd?uri={uri}" domain-name)
               domain (actions.domain/find-or-create
                       (factory :domain
                                {:_id domain-name
                                 :jrdTemplate template
                                 :discovered true}))
               domain (actions.domain/add-link domain {:rel "lrdd" :template template})
               uri (str "http://" domain-name "/user/1")
               um-url (util/replace-template template uri)
               source-link (format "http://%s/api/statuses/user_timeline/1.atom" domain-name)
               mock-um (mock-user-meta username domain-name uri source-link)
               params {:id uri}
               res (l/result-channel)]
           (l/enqueue res mock-um)
           (find-or-create-by-remote-id params) => (partial instance? User)
           (provided
            (ops/update-resource um-url) => res
            ))))

     (future-context "when given an acct uri uri"
       (db/drop-all!)
       (let [domain (actions.domain/find-or-create
                     (factory :domain
                              {:links [{:rel "lrdd" :template template}]
                               :discovered true}))
             uri (str "acct:" username "@" (:_id domain))
             response (find-or-create-by-remote-id {:id uri})]
         response => (partial instance? User)))

     ))

 (context #'register-page
   (register-page) => (partial instance? User))

 (context #'register
   (let [params {:username (fseq :username)
                 :email (fseq :email)
                 :name (fseq :name)
                 :bio (fseq :bio)
                 :location (fseq :location)
                 :password (fseq :password)}]
     (register params) =>
     (check [response]
      response                                          => map?
      response                                          => (partial instance? User)
      (model.auth-mechanism/fetch-by-user response) =not=> empty?)))
 )
