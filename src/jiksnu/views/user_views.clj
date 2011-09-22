(ns jiksnu.views.user-views
  (:use (ciste config core html sections views)
        ciste.sections.default
        (jiksnu model session view)
        jiksnu.actions.user-actions
        jiksnu.sections.user-sections
        plaza.rdf.core
        plaza.rdf.vocabularies.foaf)
  (:require (clj-tigase [element :as element])
            (jiksnu [namespace :as namespace])
            (jiksnu.helpers [user-helpers :as helpers.user])
            (jiksnu.model [activity :as model.activity]
                          [subscription :as model.subscription]
                          [user :as model.user])
            (jiksnu.templates [activity :as templates.activity]
                              [user :as templates.user]))
  (:import com.cliqset.abdera.ext.activity.object.Person
           java.net.URI
           javax.xml.namespace.QName
           jiksnu.model.Activity
           tigase.xml.Element
           org.apache.abdera.model.Entry))

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

(defview #'edit :html
  [request user]
  {:body (templates.user/edit-form user)})

(defview #'fetch-remote :xmpp
  [request user]
  (vcard-request request user))

(defview #'fetch-updates :html
  [request user]
  {:status 303
   :template false
   :headers {"Location" (uri user)}})

(defview #'index :html
  [request users]
  {:body (templates.user/index-section users)})

(defview #'profile :html
  [request user]
  {:body (templates.user/edit-form user)})

(defview #'register :html
  [request _]
  {:body (templates.user/register-section request)})

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
         (defmodel (model-add-triples
                    (with-format :rdf
                      (show-section user))))]
     (with-out-str (model-to-format rdf-model :n3)))
   :template :false})

(defview #'show :rdf
  [request user]
  {:body
   (let [rdf-model (defmodel (model-add-triples (show-section user)))]
     (with-out-str (model-to-format rdf-model :xml)))
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

(defview #'xmpp-service-unavailable :xmpp
  [request _])
