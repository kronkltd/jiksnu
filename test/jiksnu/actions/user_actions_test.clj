(ns jiksnu.actions.user-actions-test
  (:require [ciste.config :refer [config]]
            [ciste.sections.default :refer [show-section]]
            [clj-factory.core :refer [factory fseq]]
            [clojure.data.json :as json]
            [hiccup.core :as h]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.factory :as factory]
            [jiksnu.model.authentication-mechanism :as model.auth-mechanism]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.namespace :as ns]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all])
  (:import jiksnu.model.Domain
           jiksnu.model.User))

(defn mock-user-meta
  [username domain-name uri source-link]
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
            :type "application/atom+xml"}]]))

(th/module-test ["jiksnu.modules.core"])

(fact "#'jiksnu.actions.user-actions/get-username-from-http-uri"
  (fact "when the uri does not have user info"
    (let [username (fseq :username)
          domain-name (fseq :domain)
          uri (factory/make-uri domain-name "/users/1")
          params {:_id uri}
          params2 (assoc params :domain domain-name)]
      (actions.user/get-username-from-http-uri params) =>
      (contains {:username username})
      (provided
       (actions.user/discover-user-xrd params2 nil) => {:username username}))))

(fact "#'jiksnu.actions.user-actions/get-username"
  (let [username (fseq :username)
        domain-name (fseq :domain)
        template (str "http://" domain-name "/xrd?uri={uri}")
        domain (-> (factory :domain {:_id domain-name
                                     :discovered true})
                   actions.domain/find-or-create
                   (actions.domain/add-link {:rel "lrdd" :template template}))]

    (fact "when given a http uri"
      (fact "and it does not have user info"
        (fact "and the xrd request returns info"
          (let [uri (factory/make-uri domain-name "/users/1")
                params {:_id uri}]
            (actions.user/get-username params) => (contains {:username username})
            (provided
              (actions.user/discover-user-xrd anything anything) =>
              {:username username})))))

    (fact "when given an acct uri"
      (let [uri (str "acct:" username "@" domain-name)
            params {:_id uri}]
        (actions.user/get-username params) =>
        (contains {:username username})))))

(fact "#'jiksnu.actions.user-actions/get-domain"
  (fact "when the domain already exists"
    (let [domain (mock/a-domain-exists {:discovered true})
          domain-name (:_id domain)]

      (fact "when the domain is specified"
        (actions.user/get-domain {:domain (:_id domain)}) =>
        (every-checker
         (partial instance? Domain)
         (contains {:_id (:_id domain)})))

      (fact "when the domain is not specified"
        (fact "when there is an id"

          (fact "when it is a http url"
            (let [uri (format "http://%s/users/1" domain-name)
                  params {:_id uri}]
              (actions.user/get-domain params) =>
              (every-checker
               (partial instance? Domain)
               (contains {:_id (:_id domain)}))))

          (fact "when it is an acct uri"
            (let [username (fseq :username)
                  uri (format "acct:%s@%s" username domain-name)
                  params {:_id uri}]
              (actions.user/get-domain params) =>
              (every-checker
               (partial instance? Domain)
               (contains {:_id (:_id domain)})))))))))

(fact "#'jiksnu.actions.user-actions/create"
  (fact "when the params are nil"
    (let [params nil]
      (actions.user/create params) => (throws RuntimeException)))
  (fact "empty map"
    (let [params {}]
      (actions.user/create params) => (throws RuntimeException)))
  (fact "local user"
    (let [params {:username (fseq :username)
                  :domain (config :domain)}]
      (actions.user/create params) =>
      (partial instance? User)))
  (fact "when the params contain links"
    (let [params {:username (fseq :username)
                  :domain (config :domain)
                  :links [{:href (fseq :uri) :rel "alternate"}]}]
      (actions.user/create params) =>
      (partial instance? User))))

(fact "#'jiksnu.actions.user-actions/index"
  (actions.user/index {}) => map?)

(fact "#'jiksnu.actions.user-actions/find-or-create"
  (let [username (fseq :username)
        domain-name (fseq :domain)
        source-link (format "http://%s/api/statuses/user_timeline/1.atom" domain-name)
        xrd-template (format "http://%s/xrd?uri={uri}" domain-name)
        jrd-template (format "http://%s/lrdd?uri={uri}" domain-name)]

    (future-fact "when given a http uri"
      (let [uri (str "http://" domain-name "/user/1")
            params {:_id uri}
            profile-url (format "https://%s/api/user/%s/profile" domain-name username)
            links [{:href profile-url :rel "self"}]
            mock-xrd (mock-user-meta username domain-name uri source-link)
            mock-jrd (json/json-str {:links links})
            mock-profile (json/json-str {:preferredUsername username})
            xrd-url (util/replace-template xrd-template uri)
            jrd-url (util/replace-template jrd-template uri)]

        (fact "when the domain has a jrd endpoint"
          (db/drop-all!)
          (let [domain-params (factory :domain
                                       {:_id domain-name
                                        :jrdTemplate jrd-template
                                        :discovered true})
                domain (actions.domain/find-or-create domain-params)]

            (actions.domain/add-link domain {:rel "jrd" :template jrd-template})

            (fact "when the username can be determined"
              (actions.user/find-or-create params) => (partial instance? User)
              (provided
                (actions.user/get-username params) => {:domain domain-name
                                                       :_id uri
                                                       :username username}))))

        (fact "when the domain has an xrd endpoint"
          (db/drop-all!)
          (let [domain (actions.domain/find-or-create
                        (factory :domain
                                 {:_id domain-name
                                  :xrdTemplate xrd-template
                                  :discovered true}))]
            (model.domain/set-field! domain :xrdTemplate xrd-template)
            (actions.domain/add-link domain {:rel "xrd" :template xrd-template})


            (fact "when the username can be determined"
              (actions.user/find-or-create params) => (partial instance? User)

              #_(provided
                 (ops/update-resource xrd-url anything) => (d/success-deferred {:body mock-xrd}))
              )))))

    (fact "when given an acct uri uri"
      (db/drop-all!)
      (let [domain (actions.domain/find-or-create (factory :domain))
            uri (str "acct:" username "@" (:_id domain))]
        (actions.domain/add-link domain {:rel "jrd" :template jrd-template})

        (let [domain (model.domain/fetch-by-id (:_id domain))]

          (actions.user/find-or-create {:_id uri}) => .user.

          (provided
           (actions.user/create anything) => .user.))))))

(fact "#'jiksnu.actions.user-actions/register"
  (let [params {:username (fseq :username)
                :email (fseq :email)
                :name (fseq :name)
                :bio (fseq :bio)
                :location (fseq :location)
                :password (fseq :password)}]
    (actions.user/register params) =>
    (every-checker
     (partial instance? User)
     (fn [response]
       (seq (model.auth-mechanism/fetch-by-user response))))))
