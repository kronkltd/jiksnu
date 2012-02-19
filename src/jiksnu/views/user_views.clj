(ns jiksnu.views.user-views
  (:use (ciste config
               [core :only [with-format]]
               [views :only [defview]])
        (ciste.sections [default :only [uri index-section show-section]])
        jiksnu.actions.user-actions
        plaza.rdf.vocabularies.foaf)
  (:require (clj-tigase [element :as element])
            (hiccup [core :as h])
            (jiksnu [model :as model]
                    [namespace :as namespace])
            (jiksnu.helpers [user-helpers :as helpers.user])
            (jiksnu.model [activity :as model.activity]
                          [subscription :as model.subscription]
                          [user :as model.user]
                          [webfinger :as model.webfinger])
            (jiksnu.sections [user-sections :as sections.user])
            (plaza.rdf [core :as rdf]))
  (:import java.net.URI
           javax.xml.namespace.QName
           jiksnu.model.Activity
           tigase.xml.Element
           org.apache.abdera2.model.Entry))

(defview #'create :html
  [request user]
  {:status 303,
   :template false
   :headers {"Location" (uri user)}})

(defview #'delete :html
  [request _]
  {:status 303
   :template false
   :headers {"Location" "/admin/users"}})

(defview #'discover :html
  [request user]
  {:status 303
   :template false
   :headers {"Location" (uri user)}})

(defview #'fetch-remote :xmpp
  [request user]
  (helpers.user/vcard-request request user))

(defview #'fetch-updates :html
  [request user]
  {:status 303
   :template false
   :headers {"Location" (uri user)}})

(defview #'index :html
  [request users]
  {:title "Users"
   :body (index-section users)})

(defview #'index :json
  [request users]
  {:body
   {:items [(index-section users)]}})

(defview #'profile :html
  [request user]
  {:body [:div (sections.user/edit-form user)]})

(defview #'register :html
  [request user]
  {:status 303,
   :template false
   :session {:id (:_id user)}
   :headers {"Location" (uri user)}})

(defview #'register-page :html
  [request user]
  {:body
   [:section (sections.user/register-form user)]})

(defview #'remote-create :xmpp
  [request user]
  (let [{:keys [to from]} request]
    {:from to
     :to from
     :type :result}))

(defview #'show :n3
  [request user]
  {:body
   (let [rdf-model
         (rdf/defmodel (rdf/model-add-triples
                        (with-format :rdf
                          (show-section user))))]
     (with-out-str (rdf/model-to-format rdf-model :n3)))
   :template :false})

(defview #'show :rdf
  [request user]
  {:body
   (let [rdf-model (rdf/defmodel (rdf/model-add-triples (show-section user)))]
     (with-out-str (rdf/model-to-format rdf-model :xml-abbrev)))
   :template :false})

(defview #'show :xmpp
  [request user]
  (let [{:keys [id to from]} request]
    {:body (element/make-element
            "query" {"xmlns" namespace/vcard-query}
            (show-section user))
     :type :result
     :id id
     :from to
     :to from}))

(defview #'update :html
  [request user]
  {:status 302
   :template false
   :headers {"Location" (uri user)}})

(defview #'update-hub :html
  [request user]
  {:status 302
   :template false
   :headers {"Location" (uri user)}})

(defview #'update-profile :html
  [request user]
  {:status 303
   :template false
   :flash "Profile updated"
   :headers {"Location" "/settings/profile"}})

(defview #'xmpp-service-unavailable :xmpp
  [request _])

(defview #'user-meta :html
  [request user]
  {:template false
   :headers {"Content-Type" "application/xrds+xml"
             "Access-Control-Allow-Origin" "*"}
   :body (h/html (model.webfinger/user-meta user))})
