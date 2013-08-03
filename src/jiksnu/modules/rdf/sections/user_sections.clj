(ns jiksnu.modules.rdf.sections.user-sections
  (:use  [ciste.config :only [config]]
         [ciste.sections :only [defsection]]
         [ciste.sections.default :only [title uri full-uri show-section add-form
                                        edit-button delete-button link-to index-line
                                        show-section-minimal update-button index-block
                                        index-section]]
         [clojure.core.incubator :only [-?>]]
         [jiksnu.ko :only [*dynamic*]]
         [jiksnu.session :only [current-user is-admin?]]
         [slingshot.slingshot :only [try+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns]
            [jiksnu.model.key :as model.key]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.rdf.util :as rdf]
            [lamina.trace :as trace]
            [plaza.rdf.core :as plaza])
  (:import jiksnu.model.User))

(defsection show-section [User :rdf]
  [user & _]
  (let [{:keys [url display-name avatar-url first-name
                last-name username name email]} user
                mkp (try+ (model.key/get-key-for-user user)
                          (catch Exception ex
                            (trace/trace "errors:handled" ex)))
                document-uri (str (full-uri user) ".rdf")
                user-uri (plaza/rdf-resource (str (full-uri user) "#me"))
                acct-uri (plaza/rdf-resource (model.user/get-uri user))]
    (plaza/with-rdf-ns ""
      (concat
       ;; TODO: describing the document should be the relm of the view
       (rdf/with-subject document-uri
         [[[ns/rdf  :type]                    [ns/foaf :PersonalProfileDocument]]
          [[ns/foaf :title]                   (plaza/l (str display-name "'s Profile"))]
          [[ns/foaf :maker]                   user-uri]
          [[ns/foaf :primaryTopic]            user-uri]])

       (rdf/with-subject user-uri
         (concat
          [[[ns/rdf  :type]                  [ns/foaf :Person]]
           [[ns/foaf :weblog]                (plaza/rdf-resource (full-uri user))]
           [[ns/foaf :holdsAccount]          acct-uri]]
          (when mkp          [[(plaza/rdf-resource     (str ns/cert "key"))
                               (plaza/rdf-resource     (str (full-uri user) "#key"))]])
          (when username     [[[ns/foaf :nick]       (plaza/l username)]])
          (when name         [[[ns/foaf :name]       (plaza/l name)]])
          (when url          [[[ns/foaf :homepage]   (plaza/rdf-resource url)]])
          (when avatar-url   [[[ns/foaf :img]        (plaza/rdf-resource avatar-url)]])
          (when email        [[[ns/foaf :mbox]       (plaza/rdf-resource (str "mailto:" email))]])
          (when display-name [[[ns/foaf :name]       (plaza/l display-name)]])
          (when first-name   [[[ns/foaf :givenName]  (plaza/l first-name)]])
          (when last-name    [[[ns/foaf :familyName] (plaza/l last-name)]])))

       (rdf/with-subject acct-uri
         [[[ns/rdf  :type]                    [ns/sioc "UserAccount"]]
          [[ns/foaf :accountServiceHomepage]  (plaza/rdf-resource (full-uri user))]
          [[ns/foaf :accountName]             (plaza/l (:username user))]
          [[ns/foaf :accountProfilePage]      (plaza/rdf-resource (full-uri user))]
          [[ns/sioc :account_of]              user-uri]])))))

