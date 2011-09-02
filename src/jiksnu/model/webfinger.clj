(ns jiksnu.model.webfinger
  (:use (ciste config sections)
        ciste.sections.default
        jiksnu.namespace)
  (:require (jiksnu.model [signature :as model.signature])))

(defn salmon-link
  [user]
  (str
   "http://"
   (config :domain)
   "/main/salmon/user/"
   (:_id user)))

(defn host-meta
  [domain]
  ["XRD" {"xmlns" xrd-ns
              "xmlns:hm" host-meta-ns}
   ["hm:Host" domain]
   ["Link" {"rel" "lrdd"
            "template" (str "http://"
                            domain
                            "/main/xrd?uri={uri}")}
    ["Title" {} "Resource Descriptor"]]])

(defn user-meta
  [user]
  ["XRD" {"xmlns" xrd-ns}
   ["Subject" {} (str "acct:" (:username user) "@" (:domain user))]
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
            "href" (salmon-link user)}]

   ["Link" {"rel" "http://salmon-protocol.org/ns/salmon-replies"
            "href" (salmon-link user)}]

   ["Link" {"rel" "http://salmon-protocol.org/ns/salmon-mention"
            "href" (salmon-link user)}]

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
