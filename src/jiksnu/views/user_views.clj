(ns jiksnu.views.user-views
  (:use [ciste.core :only [with-format]]
        [ciste.views :only [defview]]
        [ciste.sections.default :only [uri index-section show-section]]
        jiksnu.actions.user-actions
        plaza.rdf.vocabularies.foaf)
  (:require [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.namespace :as ns]
            [jiksnu.helpers.user-helpers :as helpers.user]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.sections.user-sections :as sections.user]
            [plaza.rdf.core :as rdf]))

(defview #'create :html
  [request user]
  {:status 303
   :flash "user has been created"
   :template false
   :headers {"Location" (uri user)}})

(defview #'delete :html
  [request _]
  {:status 303
   :flash "user has been deleted"
   :template false
   :headers {"Location" "/users"}})

(defview #'discover :html
  [request user]
  {:status 303
   :flash "discovering user"
   :template false
   :headers {"Location" (uri user)}})

(defview #'fetch-remote :xmpp
  [request user]
  (helpers.user/vcard-request request user))

(defview #'fetch-updates :html
  [request user]
  {:status 303
   :flash "fetching updates"
   :template false
   :headers {"Location" (uri user)}})

(defview #'index :html
  [request {:keys [items] :as options}]
  {:title "Users"
   :body (index-section items options)})

(defview #'index :json
  [request {:keys [items] :as options}]
  {:body
   {:items (index-section items options)}})

(defview #'profile :html
  [request user]
  {:title "Edit Profile"
   :body [:div (sections.user/edit-form user)]})

(defview #'register :html
  [request user]
  {:status 303,
   :template false
   :session {:id (:_id user)}
   :headers {"Location" (uri user)}})

(defview #'register-page :html
  [request user]
  {:title "Register"
   :body (sections.user/register-form user)})

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
            "query" {"xmlns" ns/vcard-query}
            (show-section user))
     :type :result
     :id id
     :from to
     :to from}))

(defview #'update :html
  [request user]
  {:status 302
   :template false
   :flash "User updated"
   :headers {"Location" (uri user)}})

(defview #'update-hub :html
  [request user]
  {:status 302
   :flash "updating hub"
   :template false
   :headers {"Location" (uri user)}})

(defview #'update-profile :html
  [request user]
  {:status 303
   :template false
   :flash "Profile updated"
   :headers {"Location" "/main/profile"}})

(defview #'user-meta :html
  [request user]
  {:template false
   :headers {"Content-Type" "application/xrds+xml"
             "Access-Control-Allow-Origin" "*"}
   :body (h/html (model.webfinger/user-meta user))})

















;; (defview #'remote-create :xmpp
;;   [request user]
;;   (let [{:keys [to from]} request]
;;     {:from to
;;      :to from
;;      :type :result}))

(defview #'xmpp-service-unavailable :xmpp
  [request _])

