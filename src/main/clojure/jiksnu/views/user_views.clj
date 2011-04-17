(ns jiksnu.views.user-views
  (:use ciste.config
        ciste.core
        ciste.html
        ciste.sections
        ciste.sections.default
        ciste.views
        clj-tigase.core
        jiksnu.actions.user-actions
        jiksnu.helpers.user-helpers
        jiksnu.model
        jiksnu.namespace
        jiksnu.sections.user-sections
        jiksnu.session
        jiksnu.view
        plaza.rdf.core
        plaza.rdf.vocabularies.foaf)
  (:require [hiccup.form-helpers :as f]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import com.cliqset.abdera.ext.activity.object.Person
           java.net.URI
           javax.xml.namespace.QName
           jiksnu.model.Activity
           tigase.xml.Element
           org.apache.abdera.model.Entry))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; create
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'create :html
  [request user]
  {:status 303,
   :template false
   :headers {"Location" (uri user)}})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; delete
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'delete :html
  [request _]
  {:status 303
   :template false
   :headers {"Location" "/admin/users"}})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; discover
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'discover :html
  [request user]
  {:status 303
   :template false
   :headers {"Location" (uri user)}})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; edit
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'edit :html
  [request user]
  {:body (edit-form user)})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fetch-remote
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'fetch-remote :xmpp
  [request user]
  (vcard-request request user))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fetch-remote
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'fetch-updates :html
  [request [user activities]]
  {:status 303
   :template false
   :headers {"Location" (uri user)}})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'index :html
  [request users]
  {:body (index-section users)})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; profile
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'profile :html
  [request user]
  {:body (edit-form user)})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; register
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'register :html
  [request _]
  {:body
   [:div
    (if (:registration-enabled (config))
      (list
       [:h1 "Register"]
       (f/form-to
        [:post "/main/register"]
        [:p
         (f/label :username "Username:")
         (f/text-field :username)]
        [:p (f/label :password "Password:")
         (f/password-field :password)]
        [:p (f/label :confirm_password "Confirm Password")
         (f/password-field :confirm_password)]
        [:p (f/submit-button "Register")]))
      [:div "Registration is disabled at this time"])]})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; remote-create
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'remote-create :xmpp
  [request user]
  (let [{:keys [to from]} request]
    {:from to
     :to from
     :type :result}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; remote-profile
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'remote-profile :html
  [request user]
  {:body (show-section user)})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; show
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'show :html
  [request user]
  {:body (show-section user)
   :formats
   [{:label "FOAF"
     :href (str (uri user) ".rdf")
     :type "application/rdf+xml"}
    {:label "N3"
     :href (str (uri user) ".n3")
     :type "text/n3"}
    {:label "Atom"
     :href (str "http://" (:domain user)
                     "/api/statuses/user_timeline/" (:_id user) ".atom")
     :type "application/atom+xml"}
    {:label "JSON"
     :href (str "http://" (:domain user)
                     "/api/statuses/user_timeline/" (:_id user) ".json")
     :type "application/json"}
    {:label "XML"
     :href (str "http://" (:domain user)
                     "/api/statuses/user_timeline/" (:_id user) ".xml")
     :type "application/xml"}]})

(defview #'show :rdf
  [request user]
  {:body
   (let [rdf-model (defmodel (model-add-triples (show-section user)))]
     (with-out-str (model-to-format rdf-model :xml)))
   :template :false})

(defview #'show :n3
  [request user]
  {:body
   (let [rdf-model
         (defmodel (model-add-triples
                    (with-format :rdf
                      (show-section user))))]
     (with-out-str (model-to-format rdf-model :n3)))
   :template :false})

(defview #'show :xmpp
  [request user]
  (let [{:keys [id to from]} request]
    {:body
     (make-element
      "query" {"xmlns" query-uri} (show-section user))
     :type :result
     :id id
     :from to
     :to from}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; update
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'update :html
  [request user]
  {:status 302
   :template false
   :headers {"Location" (uri user)}})
