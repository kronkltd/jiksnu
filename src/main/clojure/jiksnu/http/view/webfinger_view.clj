(ns jiksnu.http.view.webfinger-view
  (:use ciste.core
        ciste.view
        jiksnu.config
        jiksnu.namespace
        jiksnu.http.controller.webfinger-controller))

(defview #'host-meta :html
  [request _]
  (let [domain (:domain (config))]
    {:template false
     :headers {"Content-Type" "application/xml"}
     :body
     ["XRD" {"xmlns" xrd-ns
             "xmlns:hm" host-meta-ns}
      ["hm:Host" domain]
      ["Link" {"rel" "lrdd"
               "template" (str "http://"
                               domain
                               "/main/xrd?uri={uri}")}
       ["Title" {} "Resource Descriptor"]]]}))

(defview #'user-meta :html
  [request user]
  {:template false
   :headers {"Content-Type" "application/xml"}
   :body
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

    ["Link" {"rel" "describedby"
             "type" "application/rdf+xml"
             "href" (str (full-uri user) "/foaf")}]

    ]})
