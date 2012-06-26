(ns jiksnu.actions.user-actions-test
  (:use [ciste.core :only [with-context]]
        [ciste.model :only [string->document]]
        [ciste.sections.default :only [show-section]]
        [clj-factory.core :only [factory fseq]]
        [midje.sweet :only [fact => anything throws contains every-checker]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        jiksnu.actions.user-actions)
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model :as model]
            [jiksnu.model.authentication-mechanism :as model.auth-mechanism]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger])
  (:import jiksnu.model.Domain
           jiksnu.model.User
           org.apache.abdera2.model.Person))

(test-environment-fixture

 (fact "#'get-domain-name"
   (fact "when given a http uri"
     (get-domain-name "http://example.com/users/1") => "example.com")

   (fact "when given an acct uri"
     (get-domain-name "acct:bob@example.com") => "example.com"))

 (fact "#'get-username-from-atom-property"
   (fact "when the property has an identifier"
     (let [username (fseq :username)
           user-meta (string->document
                      (str
                       "<XRD><Link><Property type=\"http://apinamespace.org/atom/username\">"
                       username
                       "</Property></Link></XRD>"))]
       (get-username-from-atom-property user-meta) => username)))

 (fact "#'get-username-from-user-meta"
   (fact "when the usermeta has an identifier"
     (get-username-from-user-meta .user-meta.) => .username.
     (provided
       (get-username-from-identifiers .user-meta.) => .username.
       (get-username-from-atom-property .user-meta.) => nil :times 0))
   (fact "when the usermeta does not have an identifier"
     (fact "and the atom link has an identifier"
       (get-username-from-user-meta .user-meta.) => .username.
       (provided
         (get-username-from-identifiers .user-meta.) => nil
       (get-username-from-atom-property .user-meta.) => .username.))
     (fact "and the atom link does not have an identifier"
       (get-username-from-user-meta .user-meta.) => nil
     (provided
       (get-username-from-identifiers .user-meta.) => nil
       (get-username-from-atom-property .user-meta.) => nil))))

 
 (fact "#'get-username"
   (fact "when given a http uri"
     (let [username (fseq :username)
           domain-name (fseq :domain)
           template (str "http://" domain-name "/xrd?uri={uri}")
           domain (actions.domain/find-or-create (factory Domain
                                                          {:_id domain-name
                                                           :links [{:rel "lrdd" :template template}]}))
           uri (str "http://" domain-name "/users/1")]
       (get-username uri) => username)
     (provided
       (cm/fetch-resource anything) =>
       (str "
 <XRD xmlns=\"http://docs.oasis-open.org/ns/xri/xrd-1.0\">
   <Subject>" uri "</Subject>
   <Alias>acct:" username "@" domain-name "</Alias>"
   "</XRD>")))

   (fact "when given an acct uri"
     (let [domain-name (fseq :domain)
           template (str "http://" domain-name "/xrd?uri={uri}")
           domain (actions.domain/find-or-create
                   (factory Domain
                            {:_id domain-name
                             :links [{:rel "lrdd" :template template}]}))
           uri (str "acct:bob@" domain-name)]
       (get-username uri) => "bob")))
 
 (fact "#'get-domain"
   (fact "when the domain already exists"

     (let [domain (actions.domain/find-or-create (factory Domain))]

       (fact "when the domain is specified"
         (let [response (get-domain {:domain (:_id domain)})]
           response => (partial instance? Domain)
           (:_id response) => (:_id domain)))

       (fact "when the domain is not specified"
         (fact "when there is an id"

           (fact "when it is a http url"
             (let [response (get-domain {:id (str "http://" (:_id domain)
                                                  "/users/1")})]
               response => (partial instance? Domain)
               (:_id response) => (:_id domain)))

           (fact "when it is an acct uri"
             (let [response (get-domain {:id (str "acct:" (fseq :username)
                                                  "@" (:_id domain))})]
               response => (partial instance? Domain)
               (:_id response) => (:_id domain))))))))

 (fact "#'create"
   (fact "when the params ar nil"
     (fact "should throw an exception"
       (create nil) => (throws RuntimeException))))


 (fact "#'person->user"
   (fact "when the user has an acct uri"
     (fact "when the domain is discovered"
       (fact "when given a Person generated by show-section"
         (model/drop-all!)
         (let [user (create (factory User))
               person (with-context [:http :atom] (show-section user))]
           (person->user person) =>
           (every-checker
            (partial instance? User)
            #(= (:username %) (:username user))
            #(= (:id %) (:id user))
            #(= (:domain %) (:domain user))))))

     (fact "when the domain is not discovered"
       (fact "when given a Person generated by show-section"
         (model/drop-all!)
         (let [user (model/map->User (factory User))
               person (with-context [:http :atom] (show-section user))]
           (person->user person) =>
           (every-checker
            (partial instance? User)
            #(= (:username %) (:username user))
            #(= (:id %)       (:id user))
            #(= (:domain %)   (:domain user)))))))

   (fact "when the user has an http uri"
     (fact "when the domain is not discovered"
       (fact "when given a Person generated by show-section"
         (model/drop-all!)
         (let [domain-name (fseq :domain)
               uri (str "http://" domain-name "/users/1")
               person (.newAuthor abdera/*abdera-factory*)]
           (doto person
             (.setUri uri))
           ;; (person->user person) => (partial instance? User)
           (person->user person) => (contains {:id uri
                                               :domain domain-name
                                               :username "bob"})
           (provided
             (get-username anything) => "bob"))))))
 
 (fact "#'find-or-create-by-remote-id"
   (let [username (fseq :username)
         domain-name (fseq :domain)
         template (str "http://" domain-name "/xrd?uri={uri}")]
     
     (fact "when given a http uri"
       (fact "when the domain is discovered"
         (model/drop-all!)
         (let [domain (actions.domain/find-or-create
                       (factory Domain
                                {:_id domain-name
                                 :links [{:rel "lrdd" :template template}]
                                 :discovered true}))
               uri (str "http://" domain-name "/user/1")]
           (find-or-create-by-remote-id {:id uri}) => (partial instance? User))
         (provided
           (get-username uri) => username)))
     
     (fact "when given an acct uri"
       (model/drop-all!)
       (let [domain (actions.domain/find-or-create
                     (factory Domain
                              {:links [{:rel "lrdd" :template template}]
                               :discovered true}))
             uri (str "acct:" username "@" (:_id domain))
             response (find-or-create-by-remote-id {:id uri})]
         response => (partial instance? User)))))

 (fact "#'register-page"
   (register-page) =>
   (every-checker
    (partial instance? User)))

 (fact "#register"
   (let [params {:username (fseq :username)
                 :email (fseq :email)
                 :display-name (fseq :display-name)
                 :bio (fseq :bio)
                 :location (fseq :location)
                 :password (fseq :password)}]
     (register params) =>
     (every-checker
      map?
      (partial instance? User)
      (fn [response]
        (fact
          (model.auth-mechanism/fetch-by-user response) =not=> empty?

          )
        )
      )

     )
   )
 )
