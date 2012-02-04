(ns jiksnu.views.user-views
  (:use (ciste config core sections views)
        (ciste.sections [default :only [uri index-section show-section]])
        (jiksnu session view)
        jiksnu.actions.user-actions
        plaza.rdf.vocabularies.foaf)
  (:require (clj-tigase [element :as element])
            (hiccup [core :as h])
            (jiksnu [namespace :as namespace])
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
  {:body (sections.user/index-section users)})

(defview #'index :json
  [request users]
  {:body
   {:items [(index-section users)]}})

(defview #'admin-index :html
  [request users]
  {:body
   [:div
    [:table.users
     [:thead
      [:tr
       [:th]
       [:th "User"]
       [:th "Domain"]
       [:th "Discover"]
       [:th "Update"]
       [:th "Edit"]
       [:th "Delete"]]]
     [:tbody
      (map sections.user/admin-index-line users)]]]})

(defview #'profile :html
  [request user]
  {:body
   [:div
    [:form {:method "post" :action "/settings/profile"}
     [:fieldset
      [:legend "Edit User"]

      [:div.clearfix
       [:label {:for "username"} "Username"]
       [:div.input
        [:input {:type "text" :name "username" :value (:username user)}]]]

      [:div.clearfix
       [:label {:for "domain"} "Domain"]
       [:div.input
        [:input {:type "text" :name "domain" :value (:domain user)}]]]
      
      [:div.clearfix
       [:label {:for "display-name"} "Display Name"]
       [:div.input
        [:input {:type "text" :name "display-name" :value (:display-name user)}]]]
      
      [:div.clearfix
       [:label {:for "first-name"} "First Name:"]
       [:div.input
        [:input {:type "text" :name "first-name" :value (:first-name user) }]]]
      
      [:div.clearfix
       [:label {:for "last-name"} "Last Name"]
       [:div.input
        [:input {:type "text" :name "last-name" :vaue (:last-name user)}]]]

      [:div.clearfix
       [:label {:for "email"} "Email"]
       [:div.input
        [:input {:type "email" :name "email" :value (:email user)}]]]

      [:div.clearfix
       [:label {:for "bio"} "Bio"]
       [:div.input
        [:textarea {:name "bio"}
         (:bio user)]]]

      [:div.clearfix
       [:label {:for "location"} "Location"]
       [:div.input
        [:input {:type "text" :name "location" :value (:location user)}]]]

      [:div.clearfix
       [:label {:for "url"} "Url"]
       [:div.input
        [:input {:type "text" :name "url" :value (:url user)}]]]

      [:div.actions
       [:input.btn.primary {:type "submit" :value "submit"}]]]]]})

(defview #'register :html
  [request user]
  {:status 303,
   :template false
   :session {:id (:_id user)}
   :headers {"Location" (uri user)}})

(defview #'register-page :html
  [request _]
  {:body
   [:section
    [:form {:method "post" :action "/main/register"}
     [:fieldset
      [:legend "Register"]

      [:div.clearfix
       [:label {:for "username"} "Username"]
       [:div.input
        [:input {:type "text" :name "username"}]]]

      [:div.clearfix
       [:label {:for "password"} "Password"]
       [:div.input
        [:input {:type "password" :name "password"}]]]

      [:div.clearfix
       [:label {:for "confirm-password"} "Confirm Password"]
       [:div.input
        [:input {:type "password" :name "confirm-password"}]]]

      [:div.clearfix
       [:label {:for "email"} "Email"]
       [:div.input
        [:input {:type "email" :name "email"}]]]

      [:div.clearfix
       [:label {:for "display-name"} "Display Name"]
       [:div.input
        [:input {:type "text" :name "display-name"}]]]

      [:div.clearfix
       [:label {:for "location"} "Location"]
       [:div.input
        [:input {:type "text" :name "location"}]]]

      [:div.clearfix
       [:label {:for "accepted"} "I have checked the box"]
       [:div.input
        [:input {:type "checkbox" :name "accepted"}]]]


      [:div.actions
       [:input.btn.primary {:type "submit" :value "Register"}]]
      ]]]})

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
