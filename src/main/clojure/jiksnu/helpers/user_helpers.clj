(ns jiksnu.helpers.user-helpers
  (:use ciste.config
        ciste.debug
        ciste.sections
        ciste.sections.default
        [clj-gravatar.core :only (gravatar-image)]
        clj-tigase.core
        jiksnu.abdera
        jiksnu.model
        jiksnu.session
        jiksnu.view)
  (:require [clojure.string :as string]
            [hiccup.form-helpers :as f]
            [jiksnu.actions.webfinger-actions :as actions.webfinger]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.subscription :as model.subscription])
  (:import java.net.URI
           javax.xml.namespace.QName
           jiksnu.model.User
           org.apache.abdera.model.Entry
           tigase.xml.Element))

(defn get-uri
  [^User user]
  (str (:username user) "@" (:domain user)))

(defn author-uri
  [^Entry entry]
  (let [author (.getAuthor entry)]
    (let [uri (.getUri author)]
      (URI. (.toString uri)))))

(defn avatar-img
  [user]
  (let [{:keys [avatar-url title email domain name]} user]
    (let [jid (str (:username user) "@" domain)]
      [:a.url {:href (uri user)
               :title title}
       [:img.avatar.photo
        {:width "48"
         :height "48"
         :src (or avatar-url
                  (and email (gravatar-image email))
                  (gravatar-image jid))}]])))

(defn subscribe-form
  [user]
  (f/form-to [:post "/main/subscribe"]
             (f/hidden-field :subscribeto (:_id user))
             (f/submit-button "Subscribe")))

(defn unsubscribe-form
  [user]
  (f/form-to [:post "/main/unsubscribe"]
             (f/hidden-field :unsubscribeto (:_id user))
             (f/submit-button "Unsubscribe")))

(defn update-user-button
  [user]
  (f/form-to
   [:post (str (uri user) "/update")]
   (f/submit-button "Update")))

(defn discover-button
  [user]
  (f/form-to
   [:post (str (uri user) "/discover")]
   (f/submit-button "Discover")))

(defn update-hub-button
  [user]
  (f/form-to
   [:post (str (uri user) "/update-hub")]
   (f/submit-button "Update Hub")))

(defn user-actions
  [user]
  (let [actor-id (current-user-id)]
    (if (= (:_id user) actor-id)
      [:p "This is you!"]
      [:ul
       [:li (discover-button user)]
       [:li (update-user-button user)]
       [:li (update-hub-button user)]
       [:li
        (if (model.subscription/subscribing? actor-id (:_id user))
          (unsubscribe-form user)
          (subscribe-form user))]])))

(defn remote-subscribe-form
  [user]
  (list
   [:a.entity_remote_subscribe
    {:href (str "/main/ostatus?nickname="
                (:username user))}
    "Subscribe"]
   (f/form-to
    [:post "/main/ostatus"]
    [:fieldset
     [:legend "Subscribe to " (:username user)]
     [:ul.form_data
      [:li.ostatus_nickname
       (f/label :nickname "User nickname")
       (f/hidden-field :nickname (:username user))]
      [:li.ostatus_profile
       (f/label :profile "Profile Account")
       (f/text-field :profile)]]
     (f/submit-button "Submit")])))

(defn following-section
  [actor user]
  (if actor
    (list
     (if (model.subscription/subscribed? actor (:_id user))
       [:p "This user follows you."])
     (if (model.subscription/subscribing? actor (:_id user))
       [:p "You follow this user."]))))

(defn links-list
  [user]
  [:ul
   (map
    (fn [link]
      [:li
       [:p (:href link)]
       [:p (:rel link)]])
    (:links user))])

(defn local?
  [user]
  (= (:domain user)
     (-> (config) :domain)))

(defn get-domain
  [user]
  (model.domain/show (:domain user)))

(defn user-meta-uri
  [^User user]
  (let [domain-object (get-domain user)]
    (if-let [lrdd-link (get-link domain-object "lrdd")]
      (let [template (:template lrdd-link)]
        (string/replace template "{uri}" (get-uri user))))))

(defn fetch-user-meta
  [^User user]
  (-> user
      user-meta-uri
      actions.webfinger/fetch))

(defn feed-link-uri
  [^User user]
  (:href
   (get-link
    user "http://schemas.google.com/g/2010#updates-from")))

(defn fetch-user-feed
  [user]
  (fetch-feed (feed-link-uri user)))

(defn get-activities
  [feed]
  (map
   #(jiksnu.helpers.activity-helpers/to-activity % feed)
   (.getEntries feed)))

(defn fetch-activities
  [user]
  (let [feed (fetch-user-feed user)]
    (get-activities feed)))

(defn get-hub-link
  [feed]
  (-> feed
      (rel-filter-feed "hub")
      first
      .getHref
      str))

(defn load-activities
  [user]
  (dorun
   (map
    model.activity/create-raw
    (fetch-activities user))))

(defn rule-element?
  [^Element element]
  (= (.getName element) "acl-rule"))

(defn rule-map
  [rule]
  (let [^Element action-element (.getChild rule "acl-action")
        ^Element subject-element (.getChild rule "acl-subject")]
    {:subject (.getAttribute subject-element "type")
     :permission (.getAttribute action-element "permission")
     :action (.getCData action-element)}))

(defn property-map
  [user property]
  (let [child-elements (children property)
        rule-elements (filter rule-element? child-elements)
        type-element (first (filter (comp not rule-element?) child-elements))]
    {:key (.getName property)
     :type (.getName type-element)
     :value (.getCData type-element)
     :rules (map rule-map rule-elements)
     :user user}))

(defn process-vcard-element
  [element]
  (fn [vcard-element]
    (map (partial property-map (current-user))
         (children vcard-element))))

(defn vcard-request
  [request user]
  (let [{:keys [to from]} request]
    {:from to
     :to from
     :type :get}))

(defn request-vcard!
  [user]
  (let [packet-map
        {:from (make-jid "" (:domain (config)))
         :to (make-jid user)
         :id "JIKSNU1"
         :type :get
         :body
         (make-element
          "query"
          {"xmlns" "http://onesocialweb.org/spec/1.0/vcard4#query"})}
        packet (make-packet packet-map)]
    (deliver-packet! packet)))

