(ns jiksnu.actions.user-actions-test
  (:use [ciste.config :only [config]]
        [ciste.core :only [with-context]]
        [ciste.sections.default :only [show-section]]
        [clj-factory.core :only [factory fseq]]
        [midje.sweet :only [=> =not=> anything throws contains]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        jiksnu.actions.user-actions)
  (:require [ciste.model :as cm]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.modules.atom.util :as abdera]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.factory :as factory]
            [jiksnu.model :as model]
            [jiksnu.model.authentication-mechanism :as model.auth-mechanism]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.namespace :as ns]
            [jiksnu.ops :as ops]
            [jiksnu.util :as util]
            [lamina.core :as l])
  (:import jiksnu.model.Domain
           jiksnu.model.User
           org.apache.abdera.model.Person))

(defn mock-user-meta
  [username domain-name uri source-link]
  (cm/string->document
   (h/html
    [:XRD {:xmlns ns/xrd}
     [:Subject uri]
     [:Link {:rel "http://webfinger.net/rel/profile-page"
             :type "text/html"
             :href uri}]
     [:Link {:rel "http://apinamespace.org/atom"
             :type "application/atomsvc+xml"
             :href (format "http://%s/api/statusnet/app/service/%s.xml" domain-name username)}
      [:Property {:type "http://apinamespace.org/atom/username"} username]]
     [:Link {:rel "http://apinamespace.org/twitter"
             :href (format "https://%s/api/" domain-name)}]
     [:Link {:rel "http://schemas.google.com/g/2010#updates-from"
             :href source-link
             :type "application/atom+xml"}]])))

(test-environment-fixture

 ;; (context #'actions.user/discover-user-jrd
 ;;   (let [username (fseq :username)
 ;;         domain-name (fseq :domain)
 ;;         uri (format "acct:%s@%s" username domain-name)
 ;;         jrd-template (str "http://" domain-name "/api/lrdd?resource={uri}")
 ;;         jrd-uri (util/replace-template jrd-template uri)
 ;;         profile-url (format "https://%s/api/user/%s/profile" domain-name username)
 ;;         links [{:href profile-url :rel "self"}]
 ;;         mock-jrd (json/json-str {:links links})
 ;;         mock-profile {:preferredUsername username}
 ;;         domain (actions.domain/create {:_id domain-name
 ;;                                        :jrdTemplate jrd-template})
 ;;         http-uri (format "http://%s/%s" domain-name username)
 ;;         params {:_id uri}]
 ;;     (:links (discover-user-jrd params)) => (contains {:href profile-url :rel "self"})
 ;;     (provided
 ;;       (ops/update-resource jrd-uri anything) => (l/success-result {:body mock-jrd}))))

 ;; (future-context #'actions.user/get-username-from-http-uri
 ;;   (context "when the uri does not have user info"
 ;;     (let [username (fseq :username)
 ;;           domain-name (fseq :domain)
 ;;           uri (factory/make-uri domain-name "/users/1")
 ;;           params {:_id uri}]
 ;;       (get-username-from-http-uri params) => (contains {:username username}))))

 ;; (context #'get-username
 ;;   (let [username (fseq :username)
 ;;         domain-name (fseq :domain)
 ;;         template (str "http://" domain-name "/xrd?uri={uri}")
 ;;         domain (-> (factory :domain {:_id domain-name
 ;;                                      :discovered true})
 ;;                    actions.domain/find-or-create
 ;;                    (actions.domain/add-link {:rel "lrdd" :template template}))]

 ;;     (context "when given a http uri"
 ;;       (future-context "and it has user info")

 ;;       (context "and it does not have user info"

 ;;         (context "and the jrd request returns info"
 ;;           (let [uri (factory/make-uri domain-name "/users/1")
 ;;                 params {:_id uri}]
 ;;             (get-username params) => (contains {:username username})
 ;;             (provided
 ;;               (discover-user-jrd anything anything) => {:username username}
 ;;               (discover-user-xrd anything anything) => nil :times 0)))

 ;;         (context "and the xrd request returns info"
 ;;           (let [uri (factory/make-uri domain-name "/users/1")
 ;;                 params {:_id uri}]
 ;;             (get-username params) => (contains {:username username})
 ;;             (provided
 ;;               (discover-user-xrd anything anything) => {:username username}
 ;;               (discover-user-jrd anything anything) => nil)))
 ;;         )
 ;;       )

 ;;     (context "when given an acct uri"
 ;;       (let [uri (str "acct:" username "@" domain-name)
 ;;             params {:_id uri}]
 ;;         (get-username params) => (contains {:username username})))
 ;;     ))

 ;; (context #'get-domain
 ;;   (context "when the domain already exists"
 ;;     (let [domain (mock/a-domain-exists {:discovered true})
 ;;           domain-name (:_id domain)]

 ;;       (context "when the domain is specified"
 ;;         (let [response (get-domain {:domain (:_id domain)})]
 ;;           response => (partial instance? Domain)
 ;;           (:_id response) => (:_id domain)))

 ;;       (context "when the domain is not specified"
 ;;         (context "when there is an id"

 ;;           (context "when it is a http url"
 ;;             (let [uri (format "http://%s/users/1" domain-name)
 ;;                   params {:_id uri}
 ;;                   response (get-domain params)]
 ;;               response => (partial instance? Domain)
 ;;               (:_id response) => (:_id domain)))

 ;;           (context "when it is an acct uri"
 ;;             (let [username (fseq :username)
 ;;                   uri (format "acct:%s@%s" username domain-name)
 ;;                   params {:_id uri}
 ;;                   response (get-domain params)]
 ;;               response => (partial instance? Domain)
 ;;               (:_id response) => (:_id domain)))
 ;;           )
 ;;         )
 ;;       ))
 ;;   )

 ;; (context #'create
 ;;   (context "when the params are nil"
 ;;     (let [params nil]
 ;;       (create params) => (throws RuntimeException)))
 ;;   (context "empty map"
 ;;     (let [params {}]
 ;;       (create params) => (throws RuntimeException)))
 ;;   (context "local user"
 ;;     (let [params {:username (fseq :username)
 ;;                   :domain (config :domain)}]
 ;;       (create params) => model/user?))
 ;;   (context "when the params contain links"
 ;;     (let [params {:username (fseq :username)
 ;;                   :domain (config :domain)
 ;;                   :links [{:href (fseq :uri) :rel "alternate"}]}]
 ;;       (create params) => model/user?)))

 ;; (context #'index
 ;;   (index) => map?)

 ;; (context #'person->user
 ;;   (context "when the user has an acct uri"

 ;;     (context "when the domain is discovered"
 ;;       (context "when given a Person generated by show-section"
 ;;         (db/drop-all!)
 ;;         (let [user (mock/a-user-exists)
 ;;               person (with-context [:http :atom] (show-section user))]
 ;;           (person->user person) =>
 ;;           (check [response]
 ;;             response => (partial instance? User)
 ;;             response => (contains (select-keys user #{:username :id :domain :url :name}))
 ;;             )
 ;;           )))

 ;;     (context "when the domain is not discovered"
 ;;       (context "when given a Person generated by show-section"
 ;;         (db/drop-all!)
 ;;         (let [user (mock/a-user-exists)
 ;;               person (with-context [:http :atom] (show-section user))]
 ;;           (person->user person) =>
 ;;           (check [response]
 ;;             response => (partial instance? User)
 ;;             response => (contains (select-keys user #{:username :id :domain :url :name})))))))

 ;;   (context "when the user has an http uri"
 ;;     (context "when the domain is not discovered"
 ;;       (context "when given a Person generated by show-section"
 ;;         (let [domain-name (fseq :domain)
 ;;               uri (str "http://" domain-name "/users/1")
 ;;               person (.newAuthor abdera/abdera-factory)]
 ;;           (doto person
 ;;             (.setUri uri))
 ;;           ;; (person->user person) => (partial instance? User)
 ;;           (person->user person) => (contains {:id uri
 ;;                                               :domain domain-name
 ;;                                               :username "bob"})
 ;;           (provided
 ;;             (actions.domain/get-discovered anything nil nil) => .domain.
 ;;             (get-username anything) => "bob")))))
 ;;   )

 (context #'find-or-create
   (let [username (fseq :username)
         domain-name (fseq :domain)
         source-link (format "http://%s/api/statuses/user_timeline/1.atom" domain-name)
         xrd-template (format "http://%s/xrd?uri={uri}" domain-name)
         jrd-template (format "http://%s/lrdd?uri={uri}" domain-name)]

     (context "when given a http uri"
       (println "when given a http uri")
       (let [uri (str "http://" domain-name "/user/1")
             params {:_id uri}
             profile-url (format "https://%s/api/user/%s/profile" domain-name username)
             links [{:href profile-url :rel "self"}]
             mock-xrd (mock-user-meta username domain-name uri source-link)
             mock-jrd (json/json-str {:links links})
             mock-profile (json/json-str {:preferredUsername username})
             xrd-url (util/replace-template xrd-template uri)
             jrd-url (util/replace-template jrd-template uri)]

         (context "when the domain has a jrd endpoint"
           (db/drop-all!)
           (let [domain-params (factory :domain
                                 {:_id domain-name
                                  :jrdTemplate jrd-template
                                  :discovered true})
                 domain (actions.domain/find-or-create domain-params)]

             (actions.domain/add-link domain {:rel "jrd" :template jrd-template})

             (context "when the username can be determined"
               (find-or-create params) => (partial instance? User)
               (provided
                 (ops/update-resource jrd-url anything) => (l/success-result
                                                            {:body mock-jrd})
                 (ops/update-resource profile-url anything) => (l/success-result
                                                                {:body mock-profile})
                 ))))

         (context "when the domain has an xrd endpoint"
           (println "when the domain has an xrd endpoint")
           (db/drop-all!)
           (let [domain (actions.domain/find-or-create
                         (factory :domain
                                  {:_id domain-name
                                   :xrdTemplate xrd-template
                                   :discovered true}))]

             (model.domain/set-field! domain :xrdTemplate xrd-template)
             (actions.domain/add-link domain {:rel "xrd" :template xrd-template})

             (context "when the username can be determined"
               (find-or-create params) => (partial instance? User)
               (provided
                 (ops/update-resource xrd-url anything) => (l/success-result
                                                            {:body mock-xrd})))))
         ))

     ;; (future-context "when given an acct uri uri"
     ;;   (db/drop-all!)
     ;;   (let [domain (actions.domain/find-or-create
     ;;                 (factory :domain
     ;;                          {:links [{:rel "lrdd" :template template}]
     ;;                           :discovered true}))
     ;;         uri (str "acct:" username "@" (:_id domain))
     ;;         response (find-or-create {:id uri})]
     ;;     response => (partial instance? User)))

     ))

 ;; (context #'register-page
 ;;   (register-page) => (partial instance? User))

 ;; (context #'register
 ;;   (let [params {:username (fseq :username)
 ;;                 :email (fseq :email)
 ;;                 :name (fseq :name)
 ;;                 :bio (fseq :bio)
 ;;                 :location (fseq :location)
 ;;                 :password (fseq :password)}]
 ;;     (register params) =>
 ;;     (check [response]
 ;;       response                                          => map?
 ;;       response                                          => (partial instance? User)
 ;;       (model.auth-mechanism/fetch-by-user response) =not=> empty?)))
 )
