(ns jiksnu.helpers.user-helpers
  (:use ciste.config
        ciste.debug
        ciste.sections
        ciste.sections.default
        [clj-gravatar.core :only (gravatar-image)]
        (jiksnu abdera model session view))
  (:require (clj-tigase [core :as tigase]
                        [element :as element]
                        [packet :as packet])
            [clojure.string :as string]
            [hiccup.form-helpers :as f]
            [jiksnu.actions.webfinger-actions :as actions.webfinger]
            jiksnu.helpers.activity-helpers
            (jiksnu.model [activity :as model.activity]
                          [domain :as model.domain]
                          [user :as model.user]
                          [subscription :as model.subscription])
            [karras.sugar :as sugar])
  (:import javax.xml.namespace.QName
           jiksnu.model.User
           org.apache.abdera.model.Entry
           tigase.xml.Element))

(defn avatar-img
  [user]
  (let [{:keys [avatar-url title email domain name]} user
        jid (str (:username user) "@" domain)]
    [:a.url {:href (uri user)
             :title title}
     [:img.avatar.photo
      {:width "48"
       :height "48"
       :src (or avatar-url
                (and email (gravatar-image email))
                (gravatar-image jid))}]]))

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

(defn fetch-user-meta
  [^User user]
  (-> user
      model.user/user-meta-uri
      actions.webfinger/fetch))

(defn feed-link-uri
  [^User user]
  (:href
   (model.user/get-link
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
      (model.user/rel-filter-feed "hub")
      first
      .getHref
      str))

(defn load-activities
  [user]
  (dorun
   (map model.activity/create
        (fetch-activities user))))

(defn rule-map
  [rule]
  (let [^Element action-element (.getChild rule "acl-action")
        ^Element subject-element (.getChild rule "acl-subject")]
    {:subject (.getAttribute subject-element "type")
     :permission (.getAttribute action-element "permission")
     :action (.getCData action-element)}))

(defn property-map
  [user property]
  (let [child-elements (element/children property)
        rule-elements (filter model.user/rule-element? child-elements)
        type-element (first (filter (comp not model.user/rule-element?)
                                    child-elements))]
    {:key (.getName property)
     :type (.getName type-element)
     :value (.getCData type-element)
     :rules (map rule-map rule-elements)
     :user user}))

(defn process-vcard-element
  [element]
  (fn [vcard-element]
    (map (partial property-map (current-user))
         (element/children vcard-element))))

(defn vcard-request
  [request user]
  (let [{:keys [to from]} request]
    {:from to
     :to from
     :type :get}))

(defn request-vcard!
  [user]
  (let [packet-map
        {:from (tigase/make-jid "" (config :domain))
         :to (tigase/make-jid user)
         :id "JIKSNU1"
         :type :get
         :body
         (element/make-element
          "query"
          {"xmlns" "http://onesocialweb.org/spec/1.0/vcard4#query"})}
        packet (tigase/make-packet packet-map)]
    (tigase/deliver-packet! packet)))
