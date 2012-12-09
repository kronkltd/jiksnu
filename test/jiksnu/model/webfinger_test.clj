(ns jiksnu.model.webfinger-test
  (:use [ciste.config :only [with-environment]]
        [clj-factory.core :only [factory fseq]]
        midje.sweet
        jiksnu.test-helper
        jiksnu.model
        jiksnu.model.webfinger)
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.ops :as ops])
  (:import jiksnu.model.User
           nu.xom.Document))

(test-environment-fixture

 (fact "#'fetch-host-meta"
   (let [resource (existance/a-resource-exists)
         url (:url resource)]
     (fact "when the url is nil"
       (fetch-host-meta nil) => (throws AssertionError))
     (fact "when the url points to a valid XRD document"
       (fetch-host-meta url) => (partial instance? Document)
       (provided
         (ops/update-resource resource) => {:status 200
                                            :body "<XRD/>"}))
     (fact "when the url does not point to a valid XRD document"
       (fetch-host-meta url) => (throws RuntimeException)
       (provided
         (ops/update-resource resource) => {:status 404
                                            :body "<html><body><p>Not Found</p></body></html>"}))))

 (fact "#'get-username-from-xrd"
   (fact "when the usermeta has an identifier"
     (get-username-from-xrd .user-meta.) => .username.
     (provided
      (get-username-from-identifiers .user-meta.) => .username.
      (get-username-from-atom-property .user-meta.) => nil :times 0))
   (fact "when the usermeta does not have an identifier"
     (fact "and the atom link has an identifier"
       (get-username-from-xrd .user-meta.) => .username.
       (provided
        (get-username-from-identifiers .user-meta.) => nil
        (get-username-from-atom-property .user-meta.) => .username.))
     (fact "and the atom link does not have an identifier"
       (get-username-from-xrd .user-meta.) => nil
       (provided
        (get-username-from-identifiers .user-meta.) => nil
        (get-username-from-atom-property .user-meta.) => nil))))

 (fact "#'get-username-from-atom-property"
   (fact "when the property has an identifier"
     (let [username (fseq :username)
           user-meta (cm/string->document
                      (str
                       "<XRD><Link><Property type=\"http://apinamespace.org/atom/username\">"
                       username
                       "</Property></Link></XRD>"))]
       (get-username-from-atom-property user-meta) => username)))

 (future-fact "#'get-links"
   (fact "When it has links"
     (let [xrd nil]
       (get-links xrd)) => seq?))
 )
