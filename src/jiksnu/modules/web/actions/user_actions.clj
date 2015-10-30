(ns jiksnu.modules.web.actions.user-actions
  (:require [ciste.config :refer [config]]
            [ciste.core :refer [defaction]]
            [ciste.initializer :refer [definitializer]]
            [clojure.data.json :as json]
            [jiksnu.actions :refer [invoke-action]]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.key-actions :as actions.key]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.actions.webfinger-actions :as actions.webfinger]
            [jiksnu.channels :as ch]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.key :as model.key]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.namespace :as ns]
            [jiksnu.ops :as ops]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.user-transforms :as transforms.user]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]])
  (:import java.net.URI
           jiksnu.model.User))

(defaction user-meta
  "returns a user matching the uri"
  [user]
  (if (model.user/local? user)
    (let [full-uri (model.user/full-uri user)
          salmon-link ""]
      {:subject (model.user/get-uri user)
       :alias full-uri
       :links
       [
        {:rel ns/wf-profile
         :type "text/html"
         :href full-uri}

        {:rel ns/hcard
         :type "text/html"
         :href full-uri}

        {:rel ns/xfn
         :type "text/html"
         :href full-uri}

        {:rel ns/updates-from
         :type "application/atom+xml"
         ;; TODO: use formatted-uri
         :href (str "https://" (config :domain) "/api/statuses/user_timeline/" (:_id user) ".atom")}

        {:rel ns/updates-from
         :type "application/json"
         :href (str "https://" (config :domain) "/api/statuses/user_timeline/" (:_id user) ".json")}

        {:rel "describedby"
         :type "application/rdf+xml"
         :href (str full-uri ".rdf")}

        {:rel "salmon"          :href salmon-link}
        {:rel ns/salmon-replies :href salmon-link}
        {:rel ns/salmon-mention :href salmon-link}
        {:rel ns/oid-provider   :href full-uri}
        {:rel ns/osw-service    :href (str "xmpp:" (:username user) "@" (:domain user))}


        {:rel "magic-public-key"
         :href (-> user
                   model.key/get-key-for-user
                   model.key/magic-key-string)}

        {:rel ns/ostatus-subscribe
         :template (str "https://" (config :domain) "/main/ostatussub?profile={uri}")}


        {:rel ns/twitter-username
         :href (str "https://" (config :domain) "/api/")
         :property [{:type "http://apinamespace.org/twitter/username"
                     :value (:username user)}]}]})
    (throw+ "Not authorative for this resource")))
