(ns jiksnu.views.user-views
  (:use (ciste config core html sections views)
        ciste.sections.default
        (jiksnu model namespace session view)
        jiksnu.actions.user-actions
        jiksnu.helpers.user-helpers
        jiksnu.sections.user-sections
        plaza.rdf.core
        plaza.rdf.vocabularies.foaf)
  (:require [clj-tigase.element :as element]
            [hiccup.form-helpers :as f]
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

(defview #'remote-profile :html
  [request user]
  (apply-view
   (-> request
       (assoc :format :html)
       (assoc :action #'show))
   user))

(defview #'remote-user :html
  [request user]
  (apply-view
   (-> request
       (assoc :format :html)
       (assoc :action #'show))
   user))

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








(defview #'fetch-remote :xmpp
  [request user]
  (vcard-request request user))

(defview #'remote-create :xmpp
  [request user]
  (let [{:keys [to from]} request]
    {:from to
     :to from
     :type :result}))

(defview #'show :xmpp
  [request user]
  (let [{:keys [id to from]} request]
    {:body
     (element/make-element
      "query" {"xmlns" query-uri} (show-section user))
     :type :result
     :id id
     :from to
     :to from}))

(defview #'xmpp-service-unavailable :xmpp
  [request _])







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

