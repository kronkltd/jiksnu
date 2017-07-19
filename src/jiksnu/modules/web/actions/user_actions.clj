(ns jiksnu.modules.web.actions.user-actions
  (:require [ciste.config :refer [config]]
            [ciste.core :refer [defaction]]
            [jiksnu.modules.core.model.key :as model.key]
            [jiksnu.modules.core.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [slingshot.slingshot :refer [throw+]]))

(defaction user-meta
  "returns a user matching the uri"
  [user]
  (if (model.user/local? user)
    (let [full-uri (model.user/full-uri user)
          salmon-link ""]
      {:subject (model.user/get-uri user)
       :alias full-uri
       :links
       [{:rel ns/wf-profile
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
