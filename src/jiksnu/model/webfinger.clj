(ns jiksnu.model.webfinger
  (:use (ciste [config :only [config]]
               [debug :only [spy]]
               sections)
        ciste.sections.default
        (clojure.core [incubator :only [-?>]]))
  (:require (jiksnu [model :as model]
                    [namespace :as namespace])
            (jiksnu.model [signature :as model.signature]
                          [user :as model.user]))
  (:import java.net.URI))

(defn fetch-host-meta
  [url]
  (if-let [doc (model/xml-doc url)]
    (if-let [host (seq (model/query  "//*[local-name() = 'Host']" doc))]
      doc)))

(defn host-meta
  [domain]
  ["XRD" {"xmlns" namespace/xrd
          "xmlns:hm" namespace/host-meta}
   ["hm:Host" domain]
   ["Link" {"rel" "lrdd"
            "template" (str "http://"
                            domain
                            "/main/xrd?uri={uri}")}
    ["Title" {} "Resource Descriptor"]]])

(defn user-meta
  [user]
  ["XRD" {"xmlns" namespace/xrd}
   ["Subject" {} (model.user/get-uri user)]
   ["Alias" {} (full-uri user)]

   ["Link" {"rel" "http://webfinger.net/rel/profile-page"
            "type" "text/html"
            "href" (full-uri user)}]

   ["Link" {"rel" "http://microformats.org/profile/hcard"
            "type" "text/html"
            "href" (full-uri user)}]

   ["Link" {"rel" "http://gmpg.org/xfn/11"
            "type" "text/html"
            "href" (full-uri user)}]

   ["Link" {"rel" "http://schemas.google.com/g/2010#updates-from"
            "type" "application/atom+xml"
            "href" (str "http://" (config :domain)
                        "/api/statuses/user_timeline/"
                        (:_id user)
                        ".atom")}]

   ["Link" {"rel" "describedby"
            "type" "application/rdf+xml"
            "href" (str (full-uri user) ".rdf")}]

   ["Link" {"rel" "salmon"
            "href" (model.user/salmon-link user)}]

   ["Link" {"rel" "http://salmon-protocol.org/ns/salmon-replies"
            "href" (model.user/salmon-link user)}]

   ["Link" {"rel" "http://salmon-protocol.org/ns/salmon-mention"
            "href" (model.user/salmon-link user)}]

   ["Link" {"rel" "magic-public-key"
            "href" (-> user
                       model.signature/get-key-for-user
                       model.signature/magic-key-string)}]

   ["Link" {"rel" "http://ostatus.org/schema/1.0/subscribe"
            "template" (str "http://"
                            (config :domain)
                            "/main/ostatussub?profile={uri}")}]

   ["Link" {"rel" "http://specs.openid.net/auth/2.0/provider"
            "href" (full-uri user)}]

   ["Link" {"rel" "http://onesocialweb.org/rel/service"
            "href" (str "xmpp:" (:username user) "@" (:domain user))}]])

(defn get-links
  [xrd]
  (let [links (model/force-coll (model/query "//xrd:Link" xrd))]
    (map
     (fn [link]
       {:rel (model/query "string(@rel)" link)
        :template (model/query "string(@template)" link)
        :href (model/query "string(@href)" link)
        :lang (model/query "string(@lang)" link)})
     links)))
